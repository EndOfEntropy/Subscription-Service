package io.spring.boot.entity;

public enum SubscriptionStatus {
    PENDING,    // Payment initiated but not confirmed
    ACTIVE,     // Subscription is active
    CANCELLED,  // User cancelled
    EXPIRED,    // Subscription period ended
    PAST_DUE    // Payment failed
}