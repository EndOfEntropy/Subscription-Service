package io.spring.boot.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscriptionplan")
public class SubscriptionPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "price", nullable = false)
	private BigDecimal price;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "billing_cycle", nullable = false)
	private BillingCycle billingCycle;
	
	@Column(name = "features", nullable = true)
	private String features;
	
	@Column(name = "stripe_price_id")
	private String stripePriceId;				// Stripe's price ID (price_xxx)

	@OneToMany(mappedBy = "subscriptionPlan") // No cascade, no orphanRemoval
	private Set<Subscription> subscriptions = new HashSet<Subscription>();

	protected SubscriptionPlan() {
	}
	
	public SubscriptionPlan(String name, BigDecimal price, BillingCycle billingCycle, String features) {
		this.name = name;
		this.price = price;
		this.billingCycle = billingCycle;
		this.features = features;
	}
	
	public SubscriptionPlan(Long id, String name, BigDecimal price, BillingCycle billingCycle, String features) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.billingCycle = billingCycle;
		this.features = features;
	}
	
	public SubscriptionPlan(String name, BigDecimal price, BillingCycle billingCycle, String features, String stripePriceId) {
		this.name = name;
		this.price = price;
		this.billingCycle = billingCycle;
		this.features = features;
		this.stripePriceId = stripePriceId;
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

	public BillingCycle getBillingCycle() {
		return billingCycle;
	}

	public String getFeatures() {
		return features;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
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

	public void setBillingCycle(BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
	
	public String getStripePriceId() {
		return stripePriceId;
	}

	public void setStripePriceId(String stripePriceId) {
		this.stripePriceId = stripePriceId;
	}

	@Override
	public String toString() {
		return "SubscriptionPlan [id=" + id + ", name=" + name + ", price=" + price + ", billingCycle=" + billingCycle
				+ ", features=" + features + ", subscriptions=" + subscriptions + "]";
	}
	
}
