package io.spring.boot.dto;

public class SubscriptionResponse {
	
	private Long id;
	private String planName;
	private String status;
	private String clientSecret;
	
	public SubscriptionResponse(Long id, String planName, String status, String clientSecret) {
		this.id = id;
		this.planName = planName;
		this.status = status;
		this.clientSecret = clientSecret;
	}

	public Long getId() {
		return id;
	}

	public String getPlanName() {
		return planName;
	}

	public String getStatus() {
		return status;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
