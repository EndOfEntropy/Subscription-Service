package io.spring.boot.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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

import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.PaymentService;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

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
    void getUserPaymentHistory_shouldReturnPaymentHistory() throws Exception {
        // Setup
        testSetup();

        SubscriptionPlan plan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "All features");

        Subscription subscription = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), testUser, plan);

        Payment payment1 = new Payment(10L, new BigDecimal("9.99"), PaymentStatus.SUCCEEDED, OffsetDateTime.now(), 
                "inv_123", "pi_123", testUser, subscription);

        Payment payment2 = new Payment(11L, new BigDecimal("9.99"), PaymentStatus.SUCCEEDED, OffsetDateTime.now().minusMonths(1),
                "inv_456", "pi_456", testUser, subscription);

        given(paymentService.getUserPaymentHistory(1L)).willReturn(List.of(payment1, payment2));

        // Action
        ResultActions response = mockMvc.perform(get("/api/payments/history")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                // First payment
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].amount").value(9.99))
                .andExpect(jsonPath("$[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$[0].planName").value("PREMIUM"))

                // Second payment
                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].amount").value(9.99))
                .andExpect(jsonPath("$[1].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$[1].planName").value("PREMIUM"));
    }

    @Test
    void getUserPaymentHistory_shouldReturnEmptyListWhenNoPayments() throws Exception {
        // Setup
        testSetup();

        given(paymentService.getUserPaymentHistory(1L)).willReturn(List.of());

        // Action
        ResultActions response = mockMvc.perform(get("/api/payments/history")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}