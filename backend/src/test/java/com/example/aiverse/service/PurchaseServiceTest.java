package com.example.aiverse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AssetErrorCode;
import com.example.aiverse.common.error.PurchaseErrorCode;
import com.example.aiverse.dto.PurchaseRequest;
import com.example.aiverse.dto.PurchaseResponse;
import com.example.aiverse.entity.Asset;
import com.example.aiverse.entity.AssetType;
import com.example.aiverse.entity.Category;
import com.example.aiverse.entity.CategoryName;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.LicenseType;
import com.example.aiverse.entity.Purchase;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.AssetRepository;
import com.example.aiverse.repository.CreatorSettlementRepository;
import com.example.aiverse.repository.CreditTransactionRepository;
import com.example.aiverse.repository.PurchaseRepository;
import com.example.aiverse.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

    @Mock
    private CreatorSettlementRepository creatorSettlementRepository;

    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseService = new PurchaseService(
                purchaseRepository, assetRepository, userRepository, creditTransactionRepository, creatorSettlementRepository
        );
    }

    @Test
    void 구매에_성공하면_구매자_차감_창작자_지급_구매기록을_남긴다() {
        User creator = user(1L, "creator@example.com", "창작자", 0);
        User buyer = user(2L, "buyer@example.com", "구매자", 500);
        Asset asset = asset(10L, creator, 120);
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-1")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(10L)).willReturn(Optional.of(asset));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(creator));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(buyer));
        given(purchaseRepository.existsByUserIdAndAssetId(2L, 10L)).willReturn(false);
        given(purchaseRepository.save(any(Purchase.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(creditTransactionRepository.save(any(CreditTransaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        PurchaseResponse response = purchaseService.purchase(2L, new PurchaseRequest(10L), "idem-1");

        assertThat(buyer.getCreditBalance()).isEqualTo(380);
        assertThat(creator.getCreditBalance()).isEqualTo(96);
        assertThat(response.purchasePriceCredit()).isEqualTo(120);
        assertThat(response.creditBalance()).isEqualTo(380);
        verify(creatorSettlementRepository).save(any());
    }

    @Test
    void 잠금은_사용자_ID_오름차순으로_수행한다() {
        User creator = user(5L, "creator5@example.com", "창작자5", 0);
        User buyer = user(2L, "buyer2@example.com", "구매자2", 500);
        Asset asset = asset(10L, creator, 100);
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-order")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(10L)).willReturn(Optional.of(asset));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(buyer));
        given(userRepository.findByIdForUpdate(5L)).willReturn(Optional.of(creator));
        given(purchaseRepository.existsByUserIdAndAssetId(2L, 10L)).willReturn(false);
        given(purchaseRepository.save(any(Purchase.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(creditTransactionRepository.save(any(CreditTransaction.class))).willAnswer(invocation -> invocation.getArgument(0));

        purchaseService.purchase(2L, new PurchaseRequest(10L), "idem-order");

        var inOrder = org.mockito.Mockito.inOrder(userRepository);
        inOrder.verify(userRepository).findByIdForUpdate(2L);
        inOrder.verify(userRepository).findByIdForUpdate(5L);
    }

    @Test
    void 본인_콘텐츠는_구매할_수_없다() {
        User creator = user(1L, "self@example.com", "본인", 500);
        Asset asset = asset(10L, creator, 100);
        given(purchaseRepository.findByUserIdAndIdempotencyKey(1L, "idem-self")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(10L)).willReturn(Optional.of(asset));

        assertThatThrownBy(() -> purchaseService.purchase(1L, new PurchaseRequest(10L), "idem-self"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(PurchaseErrorCode.SELF_PURCHASE_NOT_ALLOWED);
        verify(userRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void 이미_구매한_콘텐츠는_다시_구매할_수_없다() {
        User creator = user(1L, "creator2@example.com", "창작자2", 0);
        User buyer = user(2L, "buyer3@example.com", "구매자3", 500);
        Asset asset = asset(10L, creator, 100);
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-dup")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(10L)).willReturn(Optional.of(asset));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(creator));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(buyer));
        given(purchaseRepository.existsByUserIdAndAssetId(2L, 10L)).willReturn(true);

        assertThatThrownBy(() -> purchaseService.purchase(2L, new PurchaseRequest(10L), "idem-dup"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(PurchaseErrorCode.ALREADY_PURCHASED);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 잔액이_부족하면_구매할_수_없다() {
        User creator = user(1L, "creator3@example.com", "창작자3", 0);
        User buyer = user(2L, "buyer4@example.com", "구매자4", 50);
        Asset asset = asset(10L, creator, 100);
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-poor")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(10L)).willReturn(Optional.of(asset));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(creator));
        given(userRepository.findByIdForUpdate(2L)).willReturn(Optional.of(buyer));
        given(purchaseRepository.existsByUserIdAndAssetId(2L, 10L)).willReturn(false);

        assertThatThrownBy(() -> purchaseService.purchase(2L, new PurchaseRequest(10L), "idem-poor"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(PurchaseErrorCode.INSUFFICIENT_BALANCE);
        verify(userRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_콘텐츠는_구매할_수_없다() {
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-missing")).willReturn(Optional.empty());
        given(assetRepository.findPurchasableById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.purchase(2L, new PurchaseRequest(999L), "idem-missing"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(AssetErrorCode.ASSET_NOT_FOUND);
    }

    @Test
    void 같은_Idempotency_Key로_재요청하면_기존_구매_결과를_반환한다() {
        User creator = user(1L, "creator4@example.com", "창작자4", 0);
        User buyer = user(2L, "buyer5@example.com", "구매자5", 500);
        Asset asset = asset(10L, creator, 100);
        Purchase existing = Purchase.of(buyer, asset, 77L, "idem-replay", 100, LicenseType.PERSONAL);
        setField(existing, "id", 55L);
        CreditTransaction transaction = CreditTransaction.of(buyer, null, CreditTransactionType.PURCHASE, -100, 400, "asset:10");
        given(purchaseRepository.findByUserIdAndIdempotencyKey(2L, "idem-replay")).willReturn(Optional.of(existing));
        given(creditTransactionRepository.findById(77L)).willReturn(Optional.of(transaction));

        PurchaseResponse response = purchaseService.purchase(2L, new PurchaseRequest(10L), "idem-replay");

        assertThat(response.purchaseId()).isEqualTo(55L);
        assertThat(response.creditBalance()).isEqualTo(400);
        verify(assetRepository, never()).findPurchasableById(any());
        verify(userRepository, never()).save(any());
    }

    private User user(Long id, String email, String nickname, int creditBalance) {
        User user = User.register(email, "encoded-password", nickname);
        setField(user, "id", id);
        setField(user, "creditBalance", creditBalance);
        return user;
    }

    private Asset asset(Long id, User creator, int priceCredit) {
        Category category = category(1L, CategoryName.NATURE, "nature", 1);
        Asset asset = Asset.register(
                creator, "구매 대상", "설명", AssetType.IMAGE, category,
                null, "original/purchase.png", "file.png", "image/png",
                1000L, priceCredit, null, LicenseType.PERSONAL
        );
        setField(asset, "id", id);
        return asset;
    }

    private Category category(Long id, CategoryName name, String slug, int displayOrder) {
        try {
            var constructor = Category.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Category category = constructor.newInstance();
            setField(category, "id", id);
            setField(category, "name", name);
            setField(category, "slug", slug);
            setField(category, "displayOrder", displayOrder);
            setField(category, "active", true);
            return category;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
