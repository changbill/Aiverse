package com.example.aiverse.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aiverse.common.error.ApplicationException;
import com.example.aiverse.common.error.AuthErrorCode;
import com.example.aiverse.common.error.CreditErrorCode;
import com.example.aiverse.dto.PaymentRequest;
import com.example.aiverse.dto.PaymentResponse;
import com.example.aiverse.entity.CreditProduct;
import com.example.aiverse.entity.CreditProductStatus;
import com.example.aiverse.entity.CreditTransaction;
import com.example.aiverse.entity.CreditTransactionType;
import com.example.aiverse.entity.Payment;
import com.example.aiverse.entity.User;
import com.example.aiverse.repository.CreditProductRepository;
import com.example.aiverse.repository.CreditTransactionRepository;
import com.example.aiverse.repository.PaymentRepository;
import com.example.aiverse.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CreditProductRepository creditProductRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse charge(Long userId, PaymentRequest request, String idempotencyKey) {
        Optional<Payment> existingPayment = paymentRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existingPayment.isPresent()) {
            return toReplayResponse(existingPayment.get());
        }

        CreditProduct product = creditProductRepository.findById(request.creditProductId())
                .orElseThrow(() -> new ApplicationException(CreditErrorCode.PRODUCT_NOT_FOUND));
        if (product.getStatus() != CreditProductStatus.ACTIVE) {
            throw new ApplicationException(CreditErrorCode.PRODUCT_INACTIVE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(AuthErrorCode.AUTHENTICATION_REQUIRED));

        Payment payment = paymentRepository.save(Payment.mockSuccess(user, product, idempotencyKey));

        int grantedCredit = product.totalCredit();
        user.increaseCredit(grantedCredit);
        userRepository.save(user);
        creditTransactionRepository.save(CreditTransaction.of(
                user, payment, CreditTransactionType.CHARGE, grantedCredit, user.getCreditBalance(),
                "credit_product:" + product.getCode()
        ));

        return new PaymentResponse(
                payment.getId(), product.getId(), payment.getAmount(), payment.getMethod(), payment.getStatus(),
                grantedCredit, user.getCreditBalance(), payment.getPaidAt()
        );
    }

    private PaymentResponse toReplayResponse(Payment payment) {
        CreditTransaction transaction = creditTransactionRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new IllegalStateException("결제에 대응하는 크레딧 거래 이력이 없습니다. paymentId=" + payment.getId()));

        return new PaymentResponse(
                payment.getId(), payment.getCreditProduct().getId(), payment.getAmount(),
                payment.getMethod(), payment.getStatus(), transaction.getAmount(), transaction.getBalanceAfter(), payment.getPaidAt()
        );
    }
}
