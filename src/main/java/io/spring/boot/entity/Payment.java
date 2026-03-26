package io.spring.boot.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "amount", nullable = false)
	private BigDecimal amount;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
	
	@Column(name = "payment_date", nullable = false)
	private OffsetDateTime paymentDate;
	
	@CreationTimestamp
	private OffsetDateTime createdAt;
	
	@Column(name = "stripe_invoice_id")
	private String stripeInvoiceId;
	
	@Column(name = "stripe_payment_intent_id")
	private String stripePaymentIntentId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_id", nullable = false)
	private Subscription subscription;
	
	@OneToOne(mappedBy = "payment")
	private Invoice invoice;
	
	protected Payment () {
		
	}

	public Payment(BigDecimal amount, PaymentStatus status, OffsetDateTime paymentDate, String stripeInvoiceId, 
			String stripePaymentIntentId, User user, Subscription subscription) {
		this.amount = amount;
		this.status = status;
		this.paymentDate = paymentDate;
		this.stripeInvoiceId = stripeInvoiceId;
		this.stripePaymentIntentId = stripePaymentIntentId;
		this.user = user;
		this.subscription = subscription;
	}

	public Payment(Long id, BigDecimal amount, PaymentStatus status, OffsetDateTime paymentDate, String stripeInvoiceId, 
			String stripePaymentIntentId, User user, Subscription subscription) {
		this.id = id;
		this.amount = amount;
		this.status = status;
		this.paymentDate = paymentDate;
		this.stripeInvoiceId = stripeInvoiceId;
		this.stripePaymentIntentId = stripePaymentIntentId;
		this.user = user;
		this.subscription = subscription;
	}

	public Long getId() {
		return id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public OffsetDateTime getPaymentDate() {
		return paymentDate;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public String getStripeInvoiceId() {
		return stripeInvoiceId;
	}

	public String getStripePaymentIntentId() {
		return stripePaymentIntentId;
	}

	public User getUser() {
		return user;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public void setPaymentDate(OffsetDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setStripeInvoiceId(String stripeInvoiceId) {
		this.stripeInvoiceId = stripeInvoiceId;
	}

	public void setStripePaymentIntentId(String stripePaymentIntentId) {
		this.stripePaymentIntentId = stripePaymentIntentId;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	public void setInvoice(Invoice invoice) {
	    this.invoice = invoice;
	    if (invoice != null && invoice.getPayment() != this) {
	        invoice.setPayment(this);
	    }
	}

	@Override
	public String toString() {
		return "Payment [id=" + id + ", amount=" + amount + ", status=" + status + ", paymentDate=" + paymentDate
				+ ", createdAt=" + createdAt + ", stripeInvoiceId=" + stripeInvoiceId + ", stripePaymentIntentId="
				+ stripePaymentIntentId + ", user=" + user + ", subscription=" + subscription + ", invoice=" + invoice
				+ "]";
	}

}
