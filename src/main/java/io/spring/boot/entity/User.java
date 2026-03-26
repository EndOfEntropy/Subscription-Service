package io.spring.boot.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "email", nullable = false)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "stripe_customer_id")			// Stripe's customer ID (cus_xxx)
	private String stripeCustomerId;
	
	@OneToMany(mappedBy = "user") // No cascade, no orphanRemoval
	private List<Subscription> subscriptions = new ArrayList<Subscription>();
	
	@OneToMany(mappedBy = "user")
	private List<Payment> payments = new ArrayList<Payment>();
	
	@OneToMany(mappedBy = "user")
	private List<Invoice> invoices = new ArrayList<Invoice>();
	
	@Column(name = "role")
	private Role role = Role.USER;

	protected User() {
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	public User(Long id, String email, String password) {
		this.id = id;
		this.email = email;
		this.password = password;
	}
	
	// Implement UserDetails methods
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getUsername() {
		return this.email; // Use email as username for authentication
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getStripeCustomerId() {
		return stripeCustomerId;
	}

	public List<Subscription> getSubscriptions() {
		return subscriptions;
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

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setStripeCustomerId(String stripeCustomerId) {
		this.stripeCustomerId = stripeCustomerId;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public void setInvoices(List<Invoice> invoices) {
		this.invoices = invoices;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", password=" + password + ", stripeCustomerId="
				+ stripeCustomerId + ", subscriptions=" + subscriptions + ", payments=" + payments + ", invoices="
				+ invoices + "]";
	}
	
}
