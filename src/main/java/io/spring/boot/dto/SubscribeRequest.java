package io.spring.boot.dto;

public class SubscribeRequest {

	private Long planId;
	
	public SubscribeRequest() {
	}

	public SubscribeRequest(Long planId) {
		this.planId = planId;
	}

	public Long getPlanId() {
		return planId;
	}

	public void setPlanId(Long planId) {
		this.planId = planId;
	}

}
