package io.spring.boot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.boot.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
	
		Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
