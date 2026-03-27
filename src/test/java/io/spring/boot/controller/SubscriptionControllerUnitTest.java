package io.spring.boot.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;

import io.spring.boot.dto.SubscribeRequest;
import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.StripeService;
import io.spring.boot.service.SubscriptionService;

@WebMvcTest(SubscriptionController.class)
@Import(SecurityConfig.class)
class SubscriptionControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;
    @MockBean
    private StripeService stripeService;
    // Needed because SecurityConfig -> JwtAuthenticationFilter -> JwtService & UserDetailsService
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private User testUser;

    private void testSetup() {
        testUser = new User(1L, "test@test.com", "password");
    }

    @Test
    void subscribe_freePlan_shouldReturnCreatedSubscription() throws Exception {
    	// Setup
    	testSetup();
        SubscribeRequest request = new SubscribeRequest(1L);
        SubscriptionPlan freePlan = new SubscriptionPlan(1L, "FREE", BigDecimal.ZERO, BillingCycle.MONTHLY,"Basic features");
        Subscription subscription = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), testUser, freePlan);
        subscription.setId(100L);
        given(subscriptionService.createSubscription(eq(1L), eq(1L))).willReturn(subscription);
        // Action
        ResultActions response = mockMvc.perform(post("/api/subscriptions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        // Verify
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.planName").value("FREE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientSecret").doesNotExist());
    }

    @Test
    void subscribe_paidPlan_shouldReturnClientSecret() throws Exception {
        // Setup
    	testSetup();
        SubscribeRequest request = new SubscribeRequest(2L);
        SubscriptionPlan paidPlan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY,"All features");
        Subscription subscription = new Subscription(SubscriptionStatus.PENDING, OffsetDateTime.now(), testUser, paidPlan, "cus_123", "sub_123", null);
        subscription.setId(200L);

        Invoice invoice = mock(Invoice.class);
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("pi_client_secret");
        when(invoice.getPaymentIntentObject()).thenReturn(mockPaymentIntent);

        com.stripe.model.Subscription stripeSubscription = mock(com.stripe.model.Subscription.class);
        when(stripeSubscription.getLatestInvoiceObject()).thenReturn(invoice);

        given(subscriptionService.createSubscription(eq(1L), eq(2L))).willReturn(subscription);
        given(stripeService.getSubscription("sub_123")).willReturn(stripeSubscription);
        // Action
        ResultActions response = mockMvc.perform(post("/api/subscriptions")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        // Verify
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200L))
                .andExpect(jsonPath("$.planName").value("PREMIUM"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.clientSecret").value("pi_client_secret"));
    }

    @Test
    void getCurrentSubscription_shouldReturnFreeWhenNoneExists() throws Exception {
        // Setup
    	testSetup();
        given(subscriptionService.getCurrentSubscription(1L)).willReturn(null);
        // Action
        ResultActions response = mockMvc.perform(get("/api/subscriptions/current")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));
        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isEmpty())
                .andExpect(jsonPath("$.planName").value("FREE"))
                .andExpect(jsonPath("$.status").value("NONE"))
                .andExpect(jsonPath("$.clientSecret").doesNotExist());
    }

    @Test
    void getCurrentSubscription_shouldReturnActiveSubscription() throws Exception {
        // Setup
    	testSetup();
        SubscriptionPlan plan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY,"All features");
        Subscription subscription = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), testUser, plan);
        subscription.setId(300L);

        given(subscriptionService.getCurrentSubscription(1L)).willReturn(subscription);
        // Action
        ResultActions response = mockMvc.perform(get("/api/subscriptions/current")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));
        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300L))
                .andExpect(jsonPath("$.planName").value("PREMIUM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientSecret").doesNotExist());
    }
}