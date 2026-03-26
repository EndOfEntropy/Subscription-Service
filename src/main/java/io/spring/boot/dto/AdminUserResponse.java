package io.spring.boot.dto;

public class AdminUserResponse {

    private Long id;
    private String email;
    private String role;
    private String currentPlan;
    private String subscriptionStatus;
    private String stripeCustomerId;
    
	public AdminUserResponse(Long id, String email, String role, String currentPlan, String subscriptionStatus,
			String stripeCustomerId) {
		this.id = id;
		this.email = email;
		this.role = role;
		this.currentPlan = currentPlan;
		this.subscriptionStatus = subscriptionStatus;
		this.stripeCustomerId = stripeCustomerId;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getRole() {
		return role;
	}

	public String getCurrentPlan() {
		return currentPlan;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}
    
}
