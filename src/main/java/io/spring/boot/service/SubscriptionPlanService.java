package io.spring.boot.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.repository.SubscriptionPlanRepository;

@Service
public class SubscriptionPlanService {

	private final SubscriptionPlanRepository planRepository;
	
	public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
		this.planRepository = subscriptionPlanRepository;
	}


	@Transactional(readOnly = true)
	public SubscriptionPlan findById(Long id){
		return planRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Plan not found"));
	}
	
	@Transactional(readOnly = true)
	public Optional<SubscriptionPlan> findByName(String name){
		return planRepository.findByName(name);
	}
	
	@Transactional(readOnly = true)
	public List<SubscriptionPlan> findAll(){
		return planRepository.findAll();
	}
	
}
