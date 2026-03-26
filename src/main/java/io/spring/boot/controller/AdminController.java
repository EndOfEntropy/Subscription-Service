package io.spring.boot.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.boot.dto.AdminPaymentResponse;
import io.spring.boot.dto.AdminUserResponse;
import io.spring.boot.dto.RevenueStatsResponse;
import io.spring.boot.dto.UpdateRoleRequest;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.service.PaymentService;
import io.spring.boot.service.SubscriptionService;
import io.spring.boot.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private UserService userService;
	private PaymentService paymentService;
	private SubscriptionService subscriptionService;
	
	public AdminController(UserService userService, PaymentService paymentService,
			SubscriptionService subscriptionService) {
		this.userService = userService;
		this.paymentService = paymentService;
		this.subscriptionService = subscriptionService;
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')") // Only admins can access
	public ResponseEntity<List<AdminUserResponse>> getAllUsers(){
		List<User> users = userService.findAllUsers();
		
		List<AdminUserResponse> response = users.stream()
				.map(u -> {
					Subscription currentSub = userService.getCurrentSubscription(u.getId());
					
					return new AdminUserResponse(
						u.getId(), u.getEmail(), u.getRole().toString(),
						currentSub != null ? currentSub.getSubscriptionPlan().getName() : "NONE",
						currentSub != null ? currentSub.getStatus().toString() : "NONE",
						u.getStripeCustomerId()
					);
				})
				.toList();
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/revenue")
	@PreAuthorize("hasRole('ADMIN')") // Only admins can access
	public ResponseEntity<RevenueStatsResponse> getRevenueStats(){
		
		List<Payment> payments = paymentService.getAllPayments();
		
		BigDecimal totalRevenue = payments.stream()
				.filter(p -> p.getStatus() == PaymentStatus.SUCCEEDED)
				.map(Payment::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		Long totalCustomers = Long.valueOf(userService.findAllUsers().size());

		Long activeSubscriptions = payments.stream()
				.map(Payment::getSubscription)
				.filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
				.distinct()
				.count();
		
		return ResponseEntity.ok(new RevenueStatsResponse(totalRevenue, totalCustomers, activeSubscriptions, payments.size()));
	}
	
	@GetMapping("/payments")
	@PreAuthorize("hasRole('ADMIN')") // Only admins can access
	public ResponseEntity<List<AdminPaymentResponse>> getAllPayments(){
		
		List<Payment> payments = paymentService.getAllPayments();
		
		List<AdminPaymentResponse> response = payments.stream()
				.map(p -> new AdminPaymentResponse(
					p.getId(),p.getUser().getEmail(), p.getAmount(), p.getStatus().toString(),
					p.getPaymentDate(), p.getSubscription().getSubscriptionPlan().getName().toString()
					)
				).toList();
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/subscriptions/{subscriptionId}/cancel")
	@PreAuthorize("hasRole('ADMIN')") // Only admins can access
	public ResponseEntity<Void> cancelUserSubscription(@PathVariable Long subscriptionId){
		subscriptionService.cancelSubscription(subscriptionId);
		return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/users/{userId}/role")
	@PreAuthorize("hasRole('ADMIN')") // Only admins can access
	public ResponseEntity<Void> updateUserRole(@PathVariable Long userId, @RequestBody UpdateRoleRequest request){
		
		userService.updateUserRole(userId, Role.valueOf(request.getRole()));
		return ResponseEntity.noContent().build();
	}
}
