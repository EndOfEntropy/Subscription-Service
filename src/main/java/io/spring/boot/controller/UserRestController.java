package io.spring.boot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.boot.dto.AuthResponse;
import io.spring.boot.dto.LoginRequest;
import io.spring.boot.dto.RegisterRequest;
import io.spring.boot.dto.UserProfileResponse;
import io.spring.boot.dto.UserUpdateRequest;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.User;
import io.spring.boot.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserRestController {

	private UserService userService;
	
	public UserRestController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/auth/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
		// Verify brand new email and register new user
		User newUser = userService.register(request.getEmail(), request.getPassword());
		// Generate token and create authentication response
		AuthResponse authResponse = new AuthResponse(newUser.getUsername(), userService.generateToken(newUser));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
	}
	
	@PostMapping("/auth/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		// Validate password and login
		User loggedInUser = userService.login(request.getEmail(), request.getPassword());
		// Generate token and create authentication response
		AuthResponse authResponse = new AuthResponse(loggedInUser.getUsername(), userService.generateToken(loggedInUser));
		 
		return ResponseEntity.ok(authResponse);
	}
	
	@GetMapping("/user/profile")
	public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal User currentUser){
		Subscription currentSub = userService.getCurrentSubscription(currentUser.getId());
		
		UserProfileResponse response = new UserProfileResponse(
				currentUser.getEmail(), 
				currentSub != null ? currentSub.getSubscriptionPlan().getName() : "FREE",
				currentSub != null ? currentSub.getStatus().toString(): "NONE"
		);
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/user/profile")
	public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal User currentUser, @Valid@RequestBody UserUpdateRequest request){
		User updatedUser = userService.updateProfile(currentUser.getId(), request);
		Subscription currentSub = userService.getCurrentSubscription(currentUser.getId());
		
		UserProfileResponse response = new UserProfileResponse(
				updatedUser.getEmail(), 
				currentSub != null ? currentSub.getSubscriptionPlan().getName() : "FREE",
				currentSub != null ? currentSub.getStatus().toString(): "NONE"
		);
		
		return ResponseEntity.ok(response);
	}
	
}
