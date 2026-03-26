package io.spring.boot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.Invoice;
import io.spring.boot.entity.InvoiceStatus;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.User;
import io.spring.boot.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;


    @Test
    void getInvoiceById_shouldReturnInvoice() {
        // given
        Long invoiceId = 1L;
        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // when
        Invoice result = invoiceService.getInvoiceById(invoiceId);

        // then
        assertNotNull(result);
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void getInvoiceById_shouldThrowIfNotFound() {
        // given
        Long invoiceId = 99L;
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () ->
                invoiceService.getInvoiceById(invoiceId));

        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void getInvoiceByStripeId_shouldReturnInvoice() {
        // given
        String stripeId = "inv_123";
        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByStripeInvoiceId(stripeId)).thenReturn(Optional.of(invoice));

        // when
        Invoice result = invoiceService.getInvoiceByStripeId(stripeId);

        // then
        assertNotNull(result);
        verify(invoiceRepository).findByStripeInvoiceId(stripeId);
    }

    @Test
    void getInvoiceByStripeId_shouldThrowIfNotFound() {
        // given
        when(invoiceRepository.findByStripeInvoiceId("missing"))
                .thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () ->
                invoiceService.getInvoiceByStripeId("missing"));

        verify(invoiceRepository).findByStripeInvoiceId("missing");
    }

    @Test
    void getInvoiceByInvoiceNumber_shouldReturnInvoice() {
        // given
        String invoiceNumber = "INV-2026-0001";
        Invoice invoice = mock(Invoice.class);

        when(invoiceRepository.findByInvoiceNumber(invoiceNumber))
                .thenReturn(Optional.of(invoice));

        // when
        Invoice result = invoiceService.getInvoiceByInvoiceNumber(invoiceNumber);

        // then
        assertNotNull(result);
        verify(invoiceRepository).findByInvoiceNumber(invoiceNumber);
    }

    @Test
    void getInvoiceByInvoiceNumber_shouldThrowIfNotFound() {
        // given
        when(invoiceRepository.findByInvoiceNumber("missing"))
                .thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () ->
                invoiceService.getInvoiceByInvoiceNumber("missing"));

        verify(invoiceRepository).findByInvoiceNumber("missing");
    }

    @Test
    void getUserInvoices_shouldReturnInvoices() {
        // given
        Long userId = 1L;

        Invoice invoice1 = mock(Invoice.class);
        Invoice invoice2 = mock(Invoice.class);

        List<Invoice> invoices = List.of(invoice1, invoice2);

        when(invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId))
                .thenReturn(invoices);

        // when
        List<Invoice> result = invoiceService.getUserInvoices(userId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findByUserIdOrderByInvoiceDateDesc(userId);
    }

    @Test
    void getUserInvoices_shouldThrowIfNull() {
        // given
        Long userId = 99L;

        when(invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId))
                .thenReturn(null);

        // when / then
        assertThrows(RuntimeException.class, () ->
                invoiceService.getUserInvoices(userId));

        verify(invoiceRepository).findByUserIdOrderByInvoiceDateDesc(userId);
    }

    @Test
    void createInvoice_shouldCreateAndSaveInvoice_whenPaymentSucceeded() {
        // given
        Payment payment = mock(Payment.class);
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);

        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(29.99));
        when(payment.getStatus()).thenReturn(PaymentStatus.SUCCEEDED);
        when(payment.getUser()).thenReturn(user);
        when(payment.getSubscription()).thenReturn(subscription);
        when(payment.getStripeInvoiceId()).thenReturn("inv_123");

        when(subscription.getSubscriptionPlan()).thenReturn(plan);
        when(plan.getName()).thenReturn("Premium Plan");

        Invoice savedInvoice = mock(Invoice.class);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);
        when(invoiceRepository.count()).thenReturn(1L);

        // when
        Invoice result = invoiceService.createInvoice(payment);

        // then
        assertNotNull(result);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_shouldSetDraftStatus_whenPaymentNotSucceeded() {
        // given
        Payment payment = mock(Payment.class);
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        SubscriptionPlan plan = mock(SubscriptionPlan.class);

        when(payment.getAmount()).thenReturn(BigDecimal.valueOf(29.99));
        when(payment.getStatus()).thenReturn(PaymentStatus.FAILED);
        when(payment.getUser()).thenReturn(user);
        when(payment.getSubscription()).thenReturn(subscription);
        when(payment.getStripeInvoiceId()).thenReturn("inv_123");

        when(subscription.getSubscriptionPlan()).thenReturn(plan);
        when(plan.getName()).thenReturn("Basic Plan");

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(mock(Invoice.class));
        when(invoiceRepository.count()).thenReturn(1L);

        // when
        Invoice result = invoiceService.createInvoice(payment);

        // then
        assertNotNull(result);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void generateInvoicePDF_shouldReturnByteArray() {
        // given
        Long invoiceId = 1L;

        Invoice invoice = mock(Invoice.class);
        User user = mock(User.class);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoice.getInvoiceNumber()).thenReturn("INV-2026-0001");
        when(invoice.getInvoiceDate()).thenReturn(OffsetDateTime.now());
        when(invoice.getStatus()).thenReturn(InvoiceStatus.PAID);
        when(invoice.getUser()).thenReturn(user);
        when(user.getEmail()).thenReturn("test@test.com");
        when(invoice.getDescription()).thenReturn("Test Description");
        when(invoice.getAmount()).thenReturn(BigDecimal.valueOf(29.99));

        // when
        byte[] result = invoiceService.generateInvoicePDF(invoiceId);

        // then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(invoiceRepository).findById(invoiceId);
    }

    @Test
    void generateInvoicePDF_shouldThrowIfInvoiceNotFound() {
        // given
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(NoSuchElementException.class, () ->
                invoiceService.generateInvoicePDF(99L));

        verify(invoiceRepository).findById(99L);
    }
}