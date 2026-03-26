package io.spring.boot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.boot.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByUserIdOrderByPaymentDateDesc(Long userId);
	
	List<Payment> findBySubscriptionIdOrderByPaymentDateDesc(Long subscriptionId);
}