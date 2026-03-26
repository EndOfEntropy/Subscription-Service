package io.spring.boot.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "subscriptions")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private SubscriptionStatus status;
	
	@Column(name = "start_date", nullable = false)
	private OffsetDateTime startDate;

	@Column(name = "end_date")
	private OffsetDateTime endDate;
	
	@Column(name = "stripe_customer_id")			// Stripe's customer ID (cus_xxx)
	private String stripeCustomerId;

	@Column(name = "stripe_subscription_id")		// Stripe's subscription ID (sub_xxx)
	private String stripeSubscriptionId;
	
	@Column(name = "stripe_payment_intent_id")		// For tracking payments
	private String stripePaymentIntentId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id", nullable = false)
	private SubscriptionPlan subscriptionPlan;
	
	@OneToMany(mappedBy = "subscription")
	private List<Payment> payments = new ArrayList<Payment>();
	
	@OneToMany(mappedBy = "subscription")
	private List<Invoice> invoices = new ArrayList<Invoice>();
	
//    @CreatedDate
//    private LocalDateTime createdAt;
//    
//    @LastModifiedDate
//    private LocalDateTime updatedAt;
	
	protected Subscription() {
	}
	
	public Subscription(SubscriptionStatus status, OffsetDateTime startDate, OffsetDateTime endDate) {
		this.status = status;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Subscription(SubscriptionStatus status, OffsetDateTime startDate, User user, SubscriptionPlan subscriptionPlan) {
		this.status = status;
		this.startDate = startDate;
		this.user = user;
		this.subscriptionPlan = subscriptionPlan;
	}


	public Subscription(SubscriptionStatus status, OffsetDateTime startDate, User user, SubscriptionPlan subscriptionPlan, 
			String stripeCustomerId, String stripeSubscriptionId, String stripePaymentIntentId) {
		this.status = status;
		this.startDate = startDate;
		this.user = user;
		this.subscriptionPlan = subscriptionPlan;
		this.stripeCustomerId = stripeCustomerId;
		this.stripeSubscriptionId = stripeSubscriptionId;
		this.stripePaymentIntentId = stripePaymentIntentId;
	}

	public Long getId() {
		return id;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public OffsetDateTime getStartDate() {
		return startDate;
	}

	public OffsetDateTime getEndDate() {
		return endDate;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public String getStripeSubscriptionId() {
		return stripeSubscriptionId;
	}

	public String getStripePaymentIntentId() {
		return stripePaymentIntentId;
	}

	public User getUser() {
		return user;
	}

	public SubscriptionPlan getSubscriptionPlan() {
		return subscriptionPlan;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public List<Invoice> getInvoices() {
		return invoices;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public void setStartDate(OffsetDateTime startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(OffsetDateTime endDate) {
		this.endDate = endDate;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public void setStripeSubscriptionId(String stripeSubscriptionId) {
		this.stripeSubscriptionId = stripeSubscriptionId;
	}

	public void setStripePaymentIntentId(String stripePaymentIntentId) {
		this.stripePaymentIntentId = stripePaymentIntentId;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
		this.subscriptionPlan = subscriptionPlan;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public void setInvoices(List<Invoice> invoices) {
		this.invoices = invoices;
	}

	@Override
	public String toString() {
		return "Subscription [id=" + id + ", status=" + status + ", startDate=" + startDate + ", endDate=" + endDate
				+ ", stripeCustomerId=" + stripeCustomerId + ", stripeSubscriptionId=" + stripeSubscriptionId
				+ ", stripePaymentIntentId=" + stripePaymentIntentId + ", user=" + user + ", subscriptionPlan="
				+ subscriptionPlan + ", payments=" + payments + ", invoices=" + invoices + "]";
	}

}
