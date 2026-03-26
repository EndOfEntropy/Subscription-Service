package io.spring.boot.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import io.spring.boot.entity.Payment;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.repository.SubscriptionPlanRepository;
import io.spring.boot.repository.SubscriptionRepository;
import io.spring.boot.repository.UserRepository;

@Service
public class SubscriptionService {

	private final SubscriptionRepository subRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
	private final StripeService stripeService;

	public SubscriptionService(SubscriptionRepository subRepository, UserRepository userRepository,
			SubscriptionPlanRepository planRepository, StripeService stripeService) {
		this.subRepository = subRepository;
		this.userRepository = userRepository;
		this.planRepository = planRepository;
		this.stripeService = stripeService;
	}

	@Transactional
	public Subscription createSubscription(Long userId, Long planId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
		SubscriptionPlan plan = planRepository.findById(planId).orElseThrow(() -> new NoSuchElementException("Plan not found"));
		Subscription existing = getCurrentSubscription(userId);
		
		// Check if user already has an active subscription
		if(existing != null && existing.getStatus() == SubscriptionStatus.ACTIVE) {
			throw new IllegalArgumentException("User already has an active subscription");
		}
		
		// For FREE plan, no Stripe interaction needed
		if(plan.getPrice().compareTo(BigDecimal.ZERO) == 0) {
			return createFreeSubscription(user, plan);
		}
		
        // For paid plans, interact with Stripe
        // Create Stripe customer if doesn't exist
		String customerId = user.getStripeCustomerId();
		if(customerId == null) {
			customerId = stripeService.createCustomer(user.getEmail(), user.getEmail());
			user.setStripeCustomerId(customerId);
			userRepository.save(user);
		}
		
		// Create Stripe subscription and extract payment intent id
		String stripePriceId = plan.getStripePriceId();
		com.stripe.model.Subscription stripeSubscription = stripeService.createSubscription(customerId, stripePriceId);
		String paymentIntentId = stripeSubscription.getLatestInvoiceObject().getPaymentIntent();
		
		// Create our subscription record
		Subscription subscription = new Subscription(SubscriptionStatus.PENDING, OffsetDateTime.now(), user, plan, 
														customerId, stripeSubscription.getId(), paymentIntentId);
		
		return subRepository.save(subscription);
	}

	@Transactional
	private Subscription createFreeSubscription(User user, SubscriptionPlan plan) {
		Subscription subscription = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), user, plan);
		return subRepository.save(subscription);
	}
	
	@Transactional(readOnly = true)
	public Subscription getCurrentSubscription(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
		
		return user.getSubscriptions().stream()
								.filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
								.findFirst()
								.orElse(null);
	}
	
	@Transactional
	public void cancelSubscription(Long subscriptionId) {
		Subscription subscription = subRepository.findById(subscriptionId)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + subscriptionId));
		// Cancel in Stripe
		if(subscription.getStripeSubscriptionId() != null) {
			stripeService.cancelSubscription(subscription.getStripeSubscriptionId());
		}
		// Update in local DB
		subscription.setStatus(SubscriptionStatus.CANCELLED);
		subscription.setEndDate(OffsetDateTime.now());
		subRepository.save(subscription);
	}
	
	@Transactional(readOnly = true)
	public Subscription getCancelledSubscription(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
		
		return user.getSubscriptions().stream()
								.filter(s -> s.getStatus() == SubscriptionStatus.CANCELLED)
								.findFirst()
								.orElse(null);
	}
	
	@Transactional
	public void reactivateSubscription(Long subscriptionId) {
	    Subscription subscription = subRepository.findById(subscriptionId)
	            .orElseThrow(() -> new NoSuchElementException("Subscription not found: " + subscriptionId));

	    if (subscription.getStatus() != SubscriptionStatus.CANCELLED) {
	        throw new IllegalArgumentException("Only cancelled subscriptions can be reactivated");
	    }

	    // Reactivate in Stripe if applicable
	    if (subscription.getStripeSubscriptionId() != null) {
	        stripeService.reactivateSubscription(subscription.getStripeSubscriptionId());
	    }

	    // Update local DB
	    subscription.setStatus(SubscriptionStatus.ACTIVE);
	    subscription.setEndDate(null);

	    subRepository.save(subscription);
	}
	
	@Transactional(readOnly = true)
	public List<Subscription> getAllSubscriptions(){
		return subRepository.findAll();
	}
	
	// Methods for STRIPE webhooks -----------------------------------------------------------------------------------------------------------
	// update subscription
	public void updateSubscriptionFromStripe(String id, String stripeStatus) {
		Subscription subscription = subRepository.findByStripeSubscriptionId(id)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + id));
		SubscriptionStatus newStatus = mapStripeStatus(stripeStatus);
		subscription.setStatus(newStatus);
		subRepository.save(subscription);
	}
	// cancel subscription
	@Transactional
	public void cancelSubscriptionByStripeId(String id) {
		Subscription subscription = subRepository.findByStripeSubscriptionId(id)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + id));
		
		subscription.setStatus(SubscriptionStatus.CANCELLED);
		subscription.setEndDate(OffsetDateTime.now());
		subRepository.save(subscription);
	}
	// activate subscription
	@Transactional
	public void activateSubscription(String id) {
		Subscription subscription = subRepository.findByStripeSubscriptionId(id)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + id));
		
		subscription.setStatus(SubscriptionStatus.ACTIVE);
		subRepository.save(subscription);
	}
	// mark subscription as past-due when payment fails
	@Transactional
	public void markSubscriptionPastDue(String id) {
		Subscription subscription = subRepository.findByStripeSubscriptionId(id)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + id));
		
		subscription.setStatus(SubscriptionStatus.PAST_DUE);
		subRepository.save(subscription);
	}
	
	// map Stripe status to our SubscriptionStatus enum
	private SubscriptionStatus mapStripeStatus(String stripeStatus) {
		switch (stripeStatus.toLowerCase()) {
        case "active":
            return SubscriptionStatus.ACTIVE;
        case "canceled":
            return SubscriptionStatus.CANCELLED;
        case "past_due":
            return SubscriptionStatus.PAST_DUE;
        case "incomplete":
        case "incomplete_expired":
            return SubscriptionStatus.PENDING;
		
        default:
            return SubscriptionStatus.PENDING;
		}
	}
	
}
