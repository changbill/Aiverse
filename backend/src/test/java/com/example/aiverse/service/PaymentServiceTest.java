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

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CreditProductRepository creditProductRepository;

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

    @Mock
    private UserRepository userRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, creditProductRepository, creditTransactionRepository, userRepository);
    }

    @Test
    void 활성_상품으로_결제하면_크레딧을_지급하고_거래_이력을_남긴다() {
        User user = User.register("charge@example.com", "encoded-password", "충전유저");
        setField(user, "id", 5L);
        CreditProduct product = product(2L, "PLUS", 1000, 100, 10000, CreditProductStatus.ACTIVE);
        given(paymentRepository.findByUserIdAndIdempotencyKey(5L, "idem-1")).willReturn(Optional.empty());
        given(creditProductRepository.findById(2L)).willReturn(Optional.of(product));
        given(userRepository.findByIdForUpdate(5L)).willReturn(Optional.of(user));
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.charge(5L, new PaymentRequest(2L), "idem-1");

        assertThat(response.grantedCredit()).isEqualTo(1100);
        assertThat(response.creditBalance()).isEqualTo(1100);
        assertThat(user.getCreditBalance()).isEqualTo(1100);
        verify(creditTransactionRepository).save(any(CreditTransaction.class));
        verify(userRepository).save(user);
    }

    @Test
    void 같은_Idempotency_Key로_재요청하면_기존_결제_결과를_그대로_반환한다() {
        User user = User.register("replay@example.com", "encoded-password", "재요청유저");
        setField(user, "id", 5L);
        CreditProduct product = product(2L, "PLUS", 1000, 100, 10000, CreditProductStatus.ACTIVE);
        Payment existingPayment = Payment.mockSuccess(user, product, "idem-1");
        setField(existingPayment, "id", 99L);
        CreditTransaction existingTransaction = CreditTransaction.of(
                user, existingPayment, CreditTransactionType.CHARGE, 1100, 1100, "credit_product:PLUS"
        );
        given(paymentRepository.findByUserIdAndIdempotencyKey(5L, "idem-1")).willReturn(Optional.of(existingPayment));
        given(creditTransactionRepository.findByPaymentId(99L)).willReturn(Optional.of(existingTransaction));

        PaymentResponse response = paymentService.charge(5L, new PaymentRequest(2L), "idem-1");

        assertThat(response.paymentId()).isEqualTo(99L);
        assertThat(response.grantedCredit()).isEqualTo(1100);
        assertThat(response.creditBalance()).isEqualTo(1100);
        verify(creditProductRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_상품이면_예외를_던진다() {
        given(paymentRepository.findByUserIdAndIdempotencyKey(5L, "idem-1")).willReturn(Optional.empty());
        given(creditProductRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.charge(5L, new PaymentRequest(999L), "idem-1"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(CreditErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 비활성_상품이면_예외를_던진다() {
        CreditProduct inactive = product(3L, "PRO", 3000, 500, 30000, CreditProductStatus.INACTIVE);
        given(paymentRepository.findByUserIdAndIdempotencyKey(5L, "idem-1")).willReturn(Optional.empty());
        given(creditProductRepository.findById(3L)).willReturn(Optional.of(inactive));

        assertThatThrownBy(() -> paymentService.charge(5L, new PaymentRequest(3L), "idem-1"))
                .isInstanceOf(ApplicationException.class)
                .extracting(exception -> ((ApplicationException) exception).getErrorCode())
                .isEqualTo(CreditErrorCode.PRODUCT_INACTIVE);
    }

    private CreditProduct product(Long id, String code, int creditAmount, int bonusCredit, int price, CreditProductStatus status) {
        try {
            var constructor = CreditProduct.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            CreditProduct product = constructor.newInstance();
            setField(product, "id", id);
            setField(product, "code", code);
            setField(product, "creditAmount", creditAmount);
            setField(product, "bonusCredit", bonusCredit);
            setField(product, "price", price);
            setField(product, "status", status);
            return product;
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
