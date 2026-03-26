package io.spring.boot.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AdminPaymentResponse {

    private Long id;
    private String email;
    private BigDecimal amount;
    private String status;
    private OffsetDateTime paymentDate;
    private String planName;
    
	public AdminPaymentResponse(Long id, String email, BigDecimal amount, String status, OffsetDateTime paymentDate,
			String planName) {
		this.id = id;
		this.email = email;
		this.amount = amount;
		this.status = status;
		this.paymentDate = paymentDate;
		this.planName = planName;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
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

}
