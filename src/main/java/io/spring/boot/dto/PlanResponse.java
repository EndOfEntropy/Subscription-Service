package io.spring.boot.dto;

import java.math.BigDecimal;

import io.spring.boot.entity.BillingCycle;

public class PlanResponse {

	private Long id;
	private String name;
	private BigDecimal price;
	private String billingCycle;
	private String features;
	
	
	public PlanResponse(Long id, String name, BigDecimal price, String billingCycle, String features) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.billingCycle = billingCycle;
		this.features = features;
	}
	
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public String getBillingCycle() {
		return billingCycle;
	}
	public String getFeatures() {
		return features;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public void setBillingCycle(String billingCycle) {
		this.billingCycle = billingCycle;
	}
	public void setFeatures(String features) {
		this.features = features;
	}
}
