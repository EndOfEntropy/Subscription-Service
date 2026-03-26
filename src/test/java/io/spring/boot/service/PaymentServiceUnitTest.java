package io.spring.boot.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.User;
import io.spring.boot.repository.PaymentRepository;
import io.spring.boot.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PaymentService paymentService;
    

    @Test
    void recordPayment_shouldCreateAndSavePayment() {
        // given
        String stripeInvoiceId = "inv_123";
        String stripePaymentIntentId = "pi_123";
        String stripeSubscriptionId = "sub_123";
        BigDecimal amount = BigDecimal.valueOf(29.99);
        PaymentStatus status = PaymentStatus.SUCCEEDED;
        
        User user = new User(1L, "test@test.com", "password");
        Subscription subscription = mock(Subscription.class);

        when(subscription.getUser()).thenReturn(user);
        when(subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)).thenReturn(Optional.of(subscription));

        Payment savedPayment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // when
        Payment result = paymentService.recordPayment(stripeInvoiceId, stripePaymentIntentId, stripeSubscriptionId, amount, status);

        // then
        assertNotNull(result);
        verify(subscriptionRepository).findByStripeSubscriptionId(stripeSubscriptionId);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void recordPayment_shouldFailIfSubscriptionNotFound() {
        // given
        when(subscriptionRepository.findByStripeSubscriptionId("sub_missing")).thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () ->
                paymentService.recordPayment("inv_123", "pi_123", "sub_missing", BigDecimal.TEN, PaymentStatus.FAILED));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getUserPaymentHistory_shouldReturnPayments() {
        // given
        Long userId = 1L;
        Payment payment1 = mock(Payment.class);
        Payment payment2 = mock(Payment.class);

        List<Payment> payments = List.of(payment1, payment2);

        when(paymentRepository.findByUserIdOrderByPaymentDateDesc(userId)).thenReturn(payments);

        // when
        List<Payment> result = paymentService.getUserPaymentHistory(userId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentRepository).findByUserIdOrderByPaymentDateDesc(userId);
    }

    @Test
    void getUserPaymentHistory_shouldReturnEmptyListIfNoneExist() {
        // given
        Long userId = 99L;
        when(paymentRepository.findByUserIdOrderByPaymentDateDesc(userId)).thenReturn(List.of());

        // when
        List<Payment> result = paymentService.getUserPaymentHistory(userId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentRepository).findByUserIdOrderByPaymentDateDesc(userId);
    }
}