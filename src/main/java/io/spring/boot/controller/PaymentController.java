package io.spring.boot.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.boot.dto.PaymentResponse;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.User;
import io.spring.boot.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	private final PaymentService paymentService;
	
	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}


	@GetMapping("/history")
	public ResponseEntity<List<PaymentResponse>> getUserPaymentHistory(@AuthenticationPrincipal User currentUser){
		
		List<Payment> payments = paymentService.getUserPaymentHistory(currentUser.getId());
		
		List<PaymentResponse> response = payments.stream()
				.map(p -> new PaymentResponse(
					p.getId(), 
					p.getAmount(), 
					p.getStatus().toString(), 
					p.getPaymentDate(), 
				    p.getSubscription() != null ? p.getSubscription().getSubscriptionPlan().getName(): null
				))
				.collect(Collectors.toList());
		
		return ResponseEntity.ok(response);
	}
	
}