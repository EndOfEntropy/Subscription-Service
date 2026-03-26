package io.spring.boot.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;

import io.spring.boot.controller.PaymentProviderException;

@Service
public class StripeService {

	private static final Logger log = LoggerFactory.getLogger(StripeService.class);
	
    /**
     * Create a Stripe customer for the user
     */
    public String createCustomer(String email, String name) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name != null ? name : email)
                .build();
            
            Customer customer = Customer.create(params);
            log.info("Created Stripe customer: {}", customer.getId());
            
            return customer.getId();
            
        } catch (StripeException e) {
            log.error("Failed to create Stripe customer", e);
            throw new PaymentProviderException("Failed to create customer: " + e.getMessage());
        }
    }
    
    /**
     * Create a Stripe subscription for the customer
     */
    public com.stripe.model.Subscription createSubscription(String customerId, String stripePriceId) {
        try {
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(
                    SubscriptionCreateParams.Item.builder()
                        .setPrice(stripePriceId)
                        .build()
                )
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(List.of("latest_invoice.payment_intent"))
                .build();
            
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.create(params);
            
            log.info("Created Stripe subscription: {}", subscription.getId());
            // Debug: Print the client secret
            Invoice invoice = subscription.getLatestInvoiceObject();
            if (invoice != null && invoice.getPaymentIntentObject() != null) {
                String clientSecret = invoice.getPaymentIntentObject().getClientSecret();
                log.info("Payment client secret: {}", clientSecret);
            }
            return subscription;
            
        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription", e);
            throw new PaymentProviderException("Failed to create subscription: " + e.getMessage());
        }
    }
    
    /**
     * Cancel a Stripe subscription
     */
    public com.stripe.model.Subscription cancelSubscription(String stripeSubId) {
        try {
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(stripeSubId);
            
            return subscription.cancel();
            
        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription", e);
            throw new PaymentProviderException("Failed to cancel subscription: " + e.getMessage());
        }
    }
    
    /**
     * Get subscription details from Stripe
     */
    public com.stripe.model.Subscription getSubscription(String stripeSubId) {
        try {
            // Expand latest_invoice and payment_intent when retrieving
            Map<String, Object> params = new HashMap<>();
            params.put("expand", Arrays.asList("latest_invoice.payment_intent"));
            
            return com.stripe.model.Subscription.retrieve(stripeSubId, params, null);
            
        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe invoice: {}", stripeSubId, e);
            throw new PaymentProviderException("Failed to get invoice: " + e.getMessage());
        }
    }
    
    /**
     * Reactivate a Stripe subscription - "subscription.cancel();" in cancelSubscription() is irreversible in Stripe
     */
    public com.stripe.model.Subscription reactivateSubscription(String stripeSubId) {
        try {
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(stripeSubId);
            
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                            .setCancelAtPeriodEnd(false)
                            .build();
            
            return subscription.update(params);
            
        } catch (StripeException e) {
            log.error("Failed to reactivate Stripe subscription: {}", stripeSubId, e);
            throw new PaymentProviderException("Failed to reactivate subscription: " + e.getMessage());
        }
    }
    
    
    /**
     * Get invoice details from Stripe
     */
    public com.stripe.model.Invoice getInvoice(String invoiceId) {
        try {
            // Common expansion for invoices to see the underlying payment details
            Map<String, Object> params = new HashMap<>();
            params.put("expand", Arrays.asList("payment_intent", "charge"));

            return com.stripe.model.Invoice.retrieve(invoiceId, params, null);

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe invoice: {}", invoiceId, e);
            throw new PaymentProviderException("Failed to get invoice: " + e.getMessage());
        }
    }
    
}
