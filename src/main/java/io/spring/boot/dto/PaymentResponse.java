package io.spring.boot.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentResponse {

	private Long id;
	private BigDecimal amount;
	private String status;
	private OffsetDateTime paymentDate;
	private String planName;
	
	
	public PaymentResponse(Long id, BigDecimal amount, String status, OffsetDateTime paymentDate, String planName) {
		this.id = id;
		this.amount = amount;
		this.status = status;
		this.paymentDate = paymentDate;
		this.planName = planName;
	}


	public Long getId() {
		return id;
	}


	public BigDecimal getAmount() {
		return amount;
	}


	public String getStatus() {
		return status;
	}


	public OffsetDateTime getPaymentDate() {
		return paymentDate;
	}


	public String getPlanName() {
		return planName;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public void setPaymentDate(OffsetDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}


	public void setPlanName(String planName) {
		this.planName = planName;
	}
	
}
