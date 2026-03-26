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
@Table(name = "invoice")
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "invoice_number", nullable = false)
	private String invoiceNumber;
	
	@Column(name = "amount", nullable = false)
	private BigDecimal amount;
	
	@Column(name = "description", nullable = false)
	private String description;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private InvoiceStatus status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_id", nullable = false)
	private Subscription subscription;
	
    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
	
	@Column(name = "stripe_invoice_id")			// Stripe's invoice ID (inv_xxx)
	private String stripeInvoiceId;
	
	@Column(name = "invoice_date", nullable = false)
	private OffsetDateTime invoiceDate;
	
	@CreationTimestamp
	private OffsetDateTime createdAt;
    
	public Invoice() {
	}

	public Invoice(String invoiceNumber, BigDecimal amount, String description, InvoiceStatus status, User user,
			Subscription subscription, Payment payment, String stripeInvoiceId, OffsetDateTime invoiceDate) {
		this.invoiceNumber = invoiceNumber;
		this.amount = amount;
		this.description = description;
		this.status = status;
		this.user = user;
		this.subscription = subscription;
		this.stripeInvoiceId = stripeInvoiceId;
		this.invoiceDate = invoiceDate;
		setPayment(payment);
	}

	public Invoice(Long id, String invoiceNumber, BigDecimal amount, String description, InvoiceStatus status,
			User user, Subscription subscription, Payment payment, String stripeInvoiceId, OffsetDateTime invoiceDate) {
		this.id = id;
		this.invoiceNumber = invoiceNumber;
		this.amount = amount;
		this.description = description;
		this.status = status;
		this.user = user;
		this.subscription = subscription;
		this.stripeInvoiceId = stripeInvoiceId;
		this.invoiceDate = invoiceDate;
		setPayment(payment);
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

	public String getDescription() {
		return description;
	}

	public InvoiceStatus getStatus() {
		return status;
	}

	public User getUser() {
		return user;
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public Payment getPayment() {
		return payment;
	}

	public String getStripeInvoiceId() {
		return stripeInvoiceId;
	}

	public OffsetDateTime getInvoiceDate() {
		return invoiceDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStatus(InvoiceStatus status) {
		this.status = status;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}

	public void setStripeInvoiceId(String stripeInvoiceId) {
		this.stripeInvoiceId = stripeInvoiceId;
	}

	public void setInvoiceDate(OffsetDateTime invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	
	public void setPayment(Payment payment) {
	    this.payment = payment;
	    if (payment != null && payment.getInvoice() != this) {
	        payment.setInvoice(this);
	    }
	}

	@Override
	public String toString() {
		return "Invoice [id=" + id + ", invoiceNumber=" + invoiceNumber + ", amount=" + amount + ", description="
				+ description + ", status=" + status + ", user=" + user + ", subscription=" + subscription
				+ ", payment=" + payment + ", stripeInvoiceId=" + stripeInvoiceId + ", invoiceDate=" + invoiceDate
				+ "]";
	}
	
}
