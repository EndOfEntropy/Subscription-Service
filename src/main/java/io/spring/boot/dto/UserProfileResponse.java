package io.spring.boot.dto;

public class UserProfileResponse {
	
	private String email;
	private String currentPlan;
	private String subscriptionStatus;
	
	public UserProfileResponse(String email, String currentPlan, String subscriptionStatus) {
		this.email = email;
		this.currentPlan = currentPlan;
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getEmail() {
		return email;
	}

	public String getCurrentPlan() {
		return currentPlan;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setCurrentPlan(String currentPlan) {
		this.currentPlan = currentPlan;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}
	
}
