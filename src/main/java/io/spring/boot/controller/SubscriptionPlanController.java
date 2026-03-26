package io.spring.boot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.boot.dto.PlanResponse;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.service.SubscriptionPlanService;

@RestController
@RequestMapping("/api/plans")
public class SubscriptionPlanController {

	private SubscriptionPlanService planService;
	
	public SubscriptionPlanController(SubscriptionPlanService planService) {
		this.planService = planService;
	}
	
	// GET /api/plans/{id} - Get specific plan details
	@GetMapping("/{id}")
	public ResponseEntity<PlanResponse> getPlanById(@PathVariable Long id){
		SubscriptionPlan plan = planService.findById(id);
		return ResponseEntity.ok(toPlanResponse(plan));
	}
	// GET /api/plans - List all available plans
	@GetMapping
	public ResponseEntity<List<PlanResponse>> getAllPlans(){
		List<SubscriptionPlan> plans = planService.findAll();
		List<PlanResponse> response = plans.stream().map(this::toPlanResponse).toList();
		return ResponseEntity.ok(response);
	}
	
	// helper method
	private PlanResponse toPlanResponse(SubscriptionPlan plan) {
		return new PlanResponse(
			plan.getId(), 
			plan.getName(), 
			plan.getPrice(), 
			plan.getBillingCycle().toString(), 
			plan.getFeatures()
		);
	}
}