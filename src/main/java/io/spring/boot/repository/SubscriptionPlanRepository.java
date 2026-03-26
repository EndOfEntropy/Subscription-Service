package io.spring.boot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.boot.entity.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
	
	Optional<SubscriptionPlan> findByName(String name);
}
