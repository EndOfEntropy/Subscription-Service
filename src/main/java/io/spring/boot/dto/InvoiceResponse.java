package io.spring.boot.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class InvoiceResponse {

	private Long id;
	private String invoiceNumber;
	private BigDecimal amount;
	private String status;
	private OffsetDateTime invoiceDate;
    private String description;
    
	public InvoiceResponse(Long id, String invoiceNumber, BigDecimal amount, String status, OffsetDateTime invoiceDate,
			String description) {
		this.id = id;
		this.invoiceNumber = invoiceNumber;
		this.amount = amount;
		this.status = status;
		this.invoiceDate = invoiceDate;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getStatus() {
		return status;
	}

	public OffsetDateTime getInvoiceDate() {
		return invoiceDate;
	}

	public String getDescription() {
		return description;
	}
    
}
