package com.example.aiverse.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.common.error.PurchaseErrorCode;
import com.example.aiverse.dto.PurchaseRequest;
import com.example.aiverse.dto.PurchaseResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.CreatorSettlement;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.CreatorSettlementRepository;
import com.example.aiverse.repository.CreditTransactionRepository;
import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final int PLATFORM_FEE_PERCENT = 20;

    private final PurchaseRepository purchaseRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final CreatorSettlementRepository creatorSettlementRepository;

    // READ_COMMITTED: PaymentService.charge와 동일한 이유 — 잠금 후 재확인이 최신 커밋을 봐야 한다.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PurchaseResponse purchase(Long buyerId, PurchaseRequest request, String idempotencyKey) {
        Optional<Purchase> existingPurchase = purchaseRepository.findByUserIdAndIdempotencyKey(buyerId, idempotencyKey);
        if (existingPurchase.isPresent()) {
            return toReplayResponse(existingPurchase.get());
        }

        // creator를 fetch join하지 않는 조회를 쓴다 — asset.getCreator()는 미초기화 프록시로 남아
        // ID만 즉시 반환하고, 세션에 잠기지 않은 User 엔티티가 캐시되어 이후 잠금 조회를
        // 무력화하는 문제(AssetJpaRepository.findPurchasableById 주석 참조)를 피한다.
        Asset asset = assetRepository.findPurchasableById(request.assetId())
                .orElseThrow(() -> new ApplicationException(AssetErrorCode.ASSET_NOT_FOUND));
        Long creatorId = asset.getCreator().getId();
        if (creatorId.equals(buyerId)) {
            throw new ApplicationException(PurchaseErrorCode.SELF_PURCHASE_NOT_ALLOWED);
        }

        // 구매자·창작자 행을 사용자 ID 오름차순으로 잠가 서로 다른 순서로 잠그며 발생하는 교착 상태를 막는다.
        Long firstLockId = Math.min(buyerId, creatorId);
        Long secondLockId = Math.max(buyerId, creatorId);
        User firstLocked = lockUser(firstLockId);
        User secondLocked = lockUser(secondLockId);
        User buyer = buyerId.equals(firstLockId) ? firstLocked : secondLocked;
        User creator = creatorId.equals(firstLockId) ? firstLocked : secondLocked;

        Optional<Purchase> purchaseAfterLock = purchaseRepository.findByUserIdAndIdempotencyKey(buyerId, idempotencyKey);
        if (purchaseAfterLock.isPresent()) {
            return toReplayResponse(purchaseAfterLock.get());
        }
        if (purchaseRepository.existsByUserIdAndAssetId(buyerId, request.assetId())) {
            throw new ApplicationException(PurchaseErrorCode.ALREADY_PURCHASED);
        }

        int price = asset.getPriceCredit();
        if (buyer.getCreditBalance() < price) {
            throw new ApplicationException(PurchaseErrorCode.INSUFFICIENT_BALANCE);
        }

        int platformFee = price * PLATFORM_FEE_PERCENT / 100;
        int creatorRevenue = price - platformFee;

        buyer.decreaseCredit(price);
        creator.increaseCredit(creatorRevenue);
        userRepository.save(buyer);
        userRepository.save(creator);

        CreditTransaction buyerTransaction = creditTransactionRepository.save(CreditTransaction.of(
                buyer, null, CreditTransactionType.PURCHASE, -price, buyer.getCreditBalance(), "asset:" + asset.getId()
        ));
        creditTransactionRepository.save(CreditTransaction.of(
                creator, null, CreditTransactionType.SALE, creatorRevenue, creator.getCreditBalance(), "asset:" + asset.getId()
        ));

        Purchase purchase = purchaseRepository.save(Purchase.of(
                buyer, asset, buyerTransaction.getId(), idempotencyKey, price, asset.getLicenseType()
        ));
        creatorSettlementRepository.save(
                CreatorSettlement.settle(creator, purchase, asset, price, platformFee, creatorRevenue)
        );

        return new PurchaseResponse(
                purchase.getId(), asset.getId(), price, asset.getLicenseType(), buyer.getCreditBalance(), purchase.getPurchasedAt()
        );
    }

    private User lockUser(Long userId) {
        return userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.AUTHENTICATION_REQUIRED));
    }

    private PurchaseResponse toReplayResponse(Purchase purchase) {
        CreditTransaction transaction = creditTransactionRepository.findById(purchase.getCreditTransactionId())
                .orElseThrow(() -> new IllegalStateException(
                        "구매에 대응하는 크레딧 거래 이력이 없습니다. purchaseId=" + purchase.getId()
                ));

        return new PurchaseResponse(
                purchase.getId(), purchase.getAsset().getId(), purchase.getPurchasePriceCredit(),
                purchase.getLicenseType(), transaction.getBalanceAfter(), purchase.getPurchasedAt()
        );
    }
}
