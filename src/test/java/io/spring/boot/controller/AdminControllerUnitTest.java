package io.spring.boot.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import io.spring.boot.dto.UpdateRoleRequest;
import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.PaymentService;
import io.spring.boot.service.SubscriptionService;
import io.spring.boot.service.UserService;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User adminUser;

    private void testSetup() {
        adminUser = new User(1L, "admin@test.com", "password");
        adminUser.setRole(Role.ADMIN);
    }

    @Test
    void getAllUsers_shouldReturnUsersWithSubscriptions() throws Exception {
        // Setup
        testSetup();

        User user1 = new User(2L, "user1@test.com", "pass");
        user1.setRole(Role.USER);

        SubscriptionPlan plan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "desc");

        Subscription sub = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), user1, plan);

        given(userService.findAllUsers()).willReturn(List.of(user1));
        given(userService.getCurrentSubscription(2L)).willReturn(sub);

        // Action
        ResultActions response = mockMvc.perform(get("/api/admin/users")
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("user1@test.com"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].currentPlan").value("PREMIUM"))
                .andExpect(jsonPath("$[0].subscriptionStatus").value("ACTIVE"));
    }

    @Test
    void getRevenueStats_shouldReturnStats() throws Exception {
        // Setup
        testSetup();

        SubscriptionPlan plan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "desc");

        Subscription activeSub = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), adminUser, plan);

        Payment p1 = new Payment(1L, new BigDecimal("10.00"), PaymentStatus.SUCCEEDED,
                OffsetDateTime.now(), "inv1", "pi1", adminUser, activeSub);

        Payment p2 = new Payment(2L, new BigDecimal("5.00"), PaymentStatus.FAILED,
                OffsetDateTime.now(), "inv2", "pi2", adminUser, activeSub);

        given(paymentService.getAllPayments()).willReturn(List.of(p1, p2));
        given(userService.findAllUsers()).willReturn(List.of(adminUser));

        // Action
        ResultActions response = mockMvc.perform(get("/api/admin/revenue")
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(10.00))
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.totalPayments").value(2))
                .andExpect(jsonPath("$.activeSubscriptions").value(1));
    }

    @Test
    void getAllPayments_shouldReturnPayments() throws Exception {
        // Setup
        testSetup();

        SubscriptionPlan plan = new SubscriptionPlan(1L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "desc");

        Subscription sub = new Subscription(SubscriptionStatus.ACTIVE, OffsetDateTime.now(), adminUser, plan);

        Payment payment = new Payment(1L, new BigDecimal("9.99"), PaymentStatus.SUCCEEDED,
                OffsetDateTime.now(), "inv_123", "pi_123", adminUser, sub);

        given(paymentService.getAllPayments()).willReturn(List.of(payment));

        // Action
        ResultActions response = mockMvc.perform(get("/api/admin/payments")
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$[0].amount").value(9.99))
                .andExpect(jsonPath("$[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$[0].planName").value("PREMIUM"));
    }

    @Test
    void cancelUserSubscription_shouldReturnNoContent() throws Exception {
        // Setup
        testSetup();

        Long subscriptionId = 10L;

        // Action
        ResultActions response = mockMvc.perform(post("/api/admin/subscriptions/{id}/cancel", subscriptionId)
                .with(user(adminUser)));

        // Verify
        response.andDo(print())
                .andExpect(status().isNoContent());

        verify(subscriptionService).cancelSubscription(subscriptionId);
    }

    @Test
    void updateUserRole_shouldReturnNoContent() throws Exception {
        // Setup
        testSetup();

        Long userId = 2L;

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("ADMIN");

        // Action
        ResultActions response = mockMvc.perform(put("/api/admin/users/{id}/role", userId)
                .with(user(adminUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Verify
        response.andDo(print())
                .andExpect(status().isNoContent());

        verify(userService).updateUserRole(userId, Role.ADMIN);
    }
}