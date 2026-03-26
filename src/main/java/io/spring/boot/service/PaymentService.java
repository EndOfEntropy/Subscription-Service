package io.spring.boot.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.User;
import io.spring.boot.repository.PaymentRepository;
import io.spring.boot.repository.SubscriptionRepository;

@Service
public class PaymentService {

	private PaymentRepository paymentRepository;
	private SubscriptionRepository subscriptionRepository;
	
	public PaymentService(PaymentRepository paymentRepository, SubscriptionRepository subscriptionRepository) {
		this.paymentRepository = paymentRepository;
		this.subscriptionRepository = subscriptionRepository;
	}

	@Transactional
	public Payment recordPayment(String stripeInvoiceId, String stripePaymentIntentId,  String subscriptionId, 
			BigDecimal amount, PaymentStatus status) {
		Subscription subscription = subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
				.orElseThrow(() -> new NoSuchElementException("Subscription not found: " + subscriptionId));
		
		Payment payment = new Payment(amount, status, OffsetDateTime.now(), stripeInvoiceId, 
				stripePaymentIntentId, subscription.getUser(), subscription);
		
		return paymentRepository.save(payment);
	}
	
	
	@Transactional(readOnly = true)
	public List<Payment> getUserPaymentHistory(Long userId){
		return paymentRepository.findByUserIdOrderByPaymentDateDesc(userId);
	}
	
	@Transactional(readOnly = true)
	public List<Payment> getAllPayments(){
		return paymentRepository.findAll();
	}
}
