package io.spring.boot.dto;

import java.math.BigDecimal;

public class RevenueStatsResponse {

    private BigDecimal totalRevenue;
    private Long totalCustomers;
    private Long activeSubscriptions;
    private Integer totalPayments;
    
	public RevenueStatsResponse(BigDecimal totalRevenue, Long totalCustomers, Long activeSubscriptions,
			Integer totalPayments) {
		this.totalRevenue = totalRevenue;
		this.totalCustomers = totalCustomers;
		this.activeSubscriptions = activeSubscriptions;
		this.totalPayments = totalPayments;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public Long getTotalCustomers() {
		return totalCustomers;
	}

	public Long getActiveSubscriptions() {
		return activeSubscriptions;
	}

	public Integer getTotalPayments() {
		return totalPayments;
	}

	public void setTotalRevenue(BigDecimal totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public void setTotalCustomers(Long totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public void setActiveSubscriptions(Long activeSubscriptions) {
		this.activeSubscriptions = activeSubscriptions;
	}

	public void setTotalPayments(Integer totalPayments) {
		this.totalPayments = totalPayments;
	}
	
}
