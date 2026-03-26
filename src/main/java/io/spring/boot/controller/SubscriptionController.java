package io.spring.boot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;

import io.spring.boot.dto.SubscribeRequest;
import io.spring.boot.dto.SubscriptionResponse;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.User;
import io.spring.boot.service.StripeService;
import io.spring.boot.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

	private final SubscriptionService subService;
	private final StripeService stripeService;

	public SubscriptionController(SubscriptionService subService, StripeService stripeService) {
		this.subService = subService;
		this.stripeService = stripeService;
	}

	// POST /api/subscriptions - Subscribe to a plan
	@PostMapping
	public ResponseEntity<SubscriptionResponse> subscribe(@AuthenticationPrincipal User currentUser, @RequestBody SubscribeRequest request) {
		Subscription subscription = subService.createSubscription(currentUser.getId(), request.getPlanId());
		
		// Get the client secret for payment (if paid plan)
        String clientSecret = null;
        if (subscription.getStripeSubscriptionId() != null) {
            com.stripe.model.Subscription stripeSubscription = stripeService.getSubscription(subscription.getStripeSubscriptionId());
            
            Invoice invoice = stripeSubscription.getLatestInvoiceObject();
            if (invoice != null && invoice.getPaymentIntentObject() != null) {
                clientSecret = invoice.getPaymentIntentObject().getClientSecret();
            }
        }
		
		SubscriptionResponse response = new SubscriptionResponse(subscription.getId(), subscription.getSubscriptionPlan().getName(),
								subscription.getStatus().toString(), clientSecret);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	// GET /api/subscriptions/current - get current subscription
	@GetMapping("/current")
	public ResponseEntity<SubscriptionResponse> getCurrentSubscription(@AuthenticationPrincipal User currentUser){
		Subscription subscription = subService.getCurrentSubscription(currentUser.getId());
		// TODO - when we create a free sub in createFreeSubscription(), this also creates an id, why are we returning this then?
		if(subscription == null) {
			return ResponseEntity.ok(new SubscriptionResponse(null, "FREE", "NONE", null));
		}
		// TODO - we currently DO NOT save the clientSecret at Subscription creation in subscribe()
		SubscriptionResponse response = new SubscriptionResponse(subscription.getId(), subscription.getSubscriptionPlan().getName(),
				subscription.getStatus().toString(), null);
		
		return ResponseEntity.ok(response);
	}
	
	// POST /api/subscriptions/cancel - cancel current subscription
	@PostMapping("/cancel")
	public ResponseEntity<Void> cancelSubscription(@AuthenticationPrincipal User currentUser){
		Subscription subscription = subService.getCurrentSubscription(currentUser.getId());
		
		if(subscription == null) {
			throw new RuntimeException("No active subscription found");
		}
		// cancel subscription in DB and in Stripe
		subService.cancelSubscription(subscription.getId());

		return ResponseEntity.noContent().build();
	}
	
	// Edge case functionality - Most subscription services don't allow reactivation; they require creating a new subscription
	// "subscription.cancel();" in cancelSubscription() is irreversible in Stripe
	// POST /api/subscriptions/reactivate - get current subscription
	@PostMapping("/reactivate")
	public ResponseEntity<SubscriptionResponse> reactivateSubscription(@AuthenticationPrincipal User currentUser){
		Subscription subscription = subService.getCancelledSubscription(currentUser.getId());
		
		if(subscription == null) {
			throw new RuntimeException("No cancelled subscription found");
		}
		// cancel subscription in DB and make Stripe API call to reactivate
	    subService.reactivateSubscription(subscription.getId());

	    return ResponseEntity.noContent().build();
	}
}
