package io.spring.boot.controller;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.HasId;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;

import io.spring.boot.entity.Invoice;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.service.InvoiceService;
import io.spring.boot.service.PaymentService;
import io.spring.boot.service.StripeService;
import io.spring.boot.service.SubscriptionService;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

	private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
	
	@Value("${stripe.webhook.secret}")
	private String webhookSecret;
	
	private final SubscriptionService subscriptionService;
	private final StripeService stripeService;
	private final PaymentService paymentService;
	private final InvoiceService invoiceService;

	public WebhookController(SubscriptionService subscriptionService, StripeService stripeService,
			PaymentService paymentService, InvoiceService invoiceService) {
		this.subscriptionService = subscriptionService;
		this.stripeService = stripeService;
		this.paymentService = paymentService;
		this.invoiceService = invoiceService;
	}

	@PostMapping("/stripe")
	public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader ("Stripe-Signature") String sigHeader){
		
		log.debug("Received Stripe webhook, signature present: {}", sigHeader != null);
		
		Event event = null;
		
		try {
			// Verify webhook signature for security
			event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
			log.info("Webhook signature verified for event: {}", event.getType());
			
		} catch (SignatureVerificationException e) {
			log.error("Webhook signature verification failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
		}
		log.info("Processing Stripe event: {} (id: {})", event.getType(), event.getId());
		
        // Handle different event types
        switch (event.getType()) {
            case "customer.subscription.created":
                handleSubscriptionCreated(event);
                break;
                
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
                
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
                
            case "invoice.payment_succeeded":
                handlePaymentSucceeded(event);
                break;
                
            case "invoice.payment_failed":
                handlePaymentFailed(event);
                break;
            
            case "invoice.created":
            case "invoice.finalized":
                log.debug("Ignoring invoice lifecycle event");
                break;
                
            default:
                log.info("Unhandled event type: {}", event.getType());
		}
		
        return ResponseEntity.ok("Webhook received");
	}
	
	// Subscription is already created in our DB. This webhook is just informational
	private void handleSubscriptionCreated(Event event) {
	    log.info("Subscription created - already handled in API, skipping webhook processing");
	    
	    String stripeSubId = extractObjectId(event);
	    com.stripe.model.Subscription stripeSubscription = stripeService.getSubscription(stripeSubId);
//	    log.info("Stripe subscription object: {}", stripeSubscription);
	    
	}

	// Update subscription status based on Stripe status
	private void handleSubscriptionUpdated(Event event) {
		String stripeSubId = extractObjectId(event);
		com.stripe.model.Subscription stripeSubscription = stripeService.getSubscription(stripeSubId);
        
        log.info("Subscription updated: {}", stripeSubscription.getId());
        subscriptionService.updateSubscriptionFromStripe(stripeSubscription.getId(), stripeSubscription.getStatus());
	}
	
	private void handleSubscriptionDeleted(Event event) {
		String stripeSubId = extractObjectId(event);
		com.stripe.model.Subscription stripeSubscription = stripeService.getSubscription(stripeSubId);
		
        log.info("Subscription deleted: {}", stripeSubscription.getId());
        subscriptionService.cancelSubscriptionByStripeId(stripeSubscription.getId());
	}
	
	// Activate subscription and create payment when first payment succeeds
	private void handlePaymentSucceeded(Event event) {
		String invoiceId = extractObjectId(event);
		com.stripe.model.Invoice stripeInvoice = stripeService.getInvoice(invoiceId);
		log.info("Payment succeeded for invoice: {}", stripeInvoice.getId());
		
		if(stripeInvoice.getSubscription() != null) {
			// Activate subscription
			subscriptionService.activateSubscription(stripeInvoice.getSubscription());
			// Record payment
			BigDecimal amountPaid = new BigDecimal(stripeInvoice.getAmountPaid()).divide(new BigDecimal(100)); // Stripe uses cents
			Payment payment = paymentService.recordPayment(stripeInvoice.getId(), stripeInvoice.getPaymentIntent(), stripeInvoice.getSubscription(), 
					amountPaid, PaymentStatus.SUCCEEDED);
			// Generate Invoice
			invoiceService.createInvoice(payment);
		}
	}
	
	// Mark subscription as past due
	private void handlePaymentFailed(Event event) {
		String invoiceId = extractObjectId(event);
		com.stripe.model.Invoice stripeInvoice = stripeService.getInvoice(invoiceId);
		log.info("Payment failed for invoice: {}", stripeInvoice.getId());
		
		if(stripeInvoice.getSubscription() != null) {
			// Mark subscription as past due
			subscriptionService.markSubscriptionPastDue(stripeInvoice.getSubscription());
			// Record payment
			BigDecimal amountDue = new BigDecimal(stripeInvoice.getAmountDue()).divide(new BigDecimal(100)); // Stripe uses cents
			Payment payment = paymentService.recordPayment(stripeInvoice.getId(), stripeInvoice.getPaymentIntent(), stripeInvoice.getSubscription(), 
					amountDue, PaymentStatus.FAILED);
		}
	}
	
	private String extractObjectId(Event event) {
	    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

	    // If SDK managed to deserialize
	    if (deserializer.getObject().isPresent()) {
	        StripeObject stripeObject = deserializer.getObject().get();
	        
	        // Check if the object implements the HasId interface
	        if (stripeObject instanceof HasId) {
	            return ((HasId) stripeObject).getId();
	        }
	    }

	    // Fallback: raw JSON (always present)
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode node = mapper.readTree(deserializer.getRawJson());
	        return node.get("id").asText();
	    } catch (Exception e) {
	        throw new RuntimeException("Unable to extract Stripe object id", e);
	    }
	}
}
