package io.spring.boot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.repository.SubscriptionPlanRepository;
import io.spring.boot.repository.SubscriptionRepository;
import io.spring.boot.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceUnitTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private StripeService stripeService;
    @InjectMocks
    private SubscriptionService subscriptionService;

    private User user;
    
    void setup() {
        user = new User(1L, "test@test.com", "pwd");
        user.setSubscriptions(List.of());
    }

    @Test
    void createSubscription_freePlan_shouldCreateActiveSubscription() {
    	// given
    	setup();
    	SubscriptionPlan freePlan = new SubscriptionPlan(1L, "FREE", BigDecimal.ZERO, BillingCycle.MONTHLY,"Basic features");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(planRepository.findById(freePlan.getId())).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // when
        Subscription result = subscriptionService.createSubscription(user.getId(), freePlan.getId());
        // then
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(freePlan, result.getSubscriptionPlan());

        verify(stripeService, never()).createCustomer(any(), any());
        verify(stripeService, never()).createSubscription(any(), any());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void createSubscription_paidPlan_shouldCreatePendingSubscription() {
    	// given
    	setup();
    	SubscriptionPlan paidPlan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY,"All features");
        paidPlan.setStripePriceId("price_123");
        com.stripe.model.Invoice mockInvoice = mock(com.stripe.model.Invoice.class);

        com.stripe.model.Subscription stripeSubscription = mock(com.stripe.model.Subscription.class);
        when(stripeSubscription.getId()).thenReturn("sub_123");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(planRepository.findById(paidPlan.getId())).thenReturn(Optional.of(paidPlan));
        when(stripeService.createCustomer(any(), any())).thenReturn("cus_123");
        when(stripeService.createSubscription("cus_123", "price_123")).thenReturn(stripeSubscription);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockInvoice.getPaymentIntent()).thenReturn("pi_123");
        when(stripeSubscription.getLatestInvoiceObject()).thenReturn(mockInvoice);
        
        // when
        Subscription result = subscriptionService.createSubscription(user.getId(), paidPlan.getId());
        // then
        assertEquals(SubscriptionStatus.PENDING, result.getStatus());
        assertEquals("cus_123", result.getStripeCustomerId());
        assertEquals("sub_123", result.getStripeSubscriptionId());

        verify(stripeService).createCustomer(any(), any());
        verify(stripeService).createSubscription("cus_123", "price_123");
        verify(userRepository).save(user);
    }

    @Test
    void createSubscription_shouldFailIfActiveSubscriptionExists() {
    	// given
    	setup();
        Subscription activeSubscription = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), user, null);
        user.setSubscriptions(List.of(activeSubscription));
        SubscriptionPlan plan = new SubscriptionPlan(1L, "FREE", BigDecimal.ZERO, BillingCycle.MONTHLY, "Basic");
        
        // when
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(planRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        // then
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.createSubscription(user.getId(), plan.getId()));

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void createSubscription_shouldFailIfUserNotFound() {
        // when
    	when(userRepository.findById(1L)).thenReturn(Optional.empty());
    	// then
        assertThrows(NoSuchElementException.class, () -> subscriptionService.createSubscription(1L, 1L));
    }

    @Test
    void createSubscription_shouldFailIfPlanNotFound() {
    	// given
    	setup();
    	// when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(planRepository.findById(1L)).thenReturn(Optional.empty());
        // then
        assertThrows(NoSuchElementException.class, () -> subscriptionService.createSubscription(1L, 1L));
    }
}
