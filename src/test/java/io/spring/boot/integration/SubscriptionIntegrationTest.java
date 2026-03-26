package io.spring.boot.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.AbstractIntegrationTest;
import io.spring.boot.dto.SubscribeRequest;
import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.repository.SubscriptionPlanRepository;
import io.spring.boot.repository.SubscriptionRepository;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.service.UserService;

@AutoConfigureMockMvc
public class SubscriptionIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private SubscriptionPlanRepository planRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;
    
    private String userToken;
    private User testUser;
    private SubscriptionPlan premiumPlan;
    
    
    @BeforeEach
    void setup() {
        // Clean database
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        planRepository.deleteAll();
    	
        // Create test user
        testUser = new User("test@example.com", passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
        
        // Generate JWT token
        userToken = userService.generateToken(testUser);
        
        // Create subscription plans
        SubscriptionPlan free = new SubscriptionPlan("FREE", BigDecimal.ZERO, BillingCycle.MONTHLY, "Basic features");
        planRepository.save(free);
        
        premiumPlan = new SubscriptionPlan("PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "All features");
        premiumPlan.setStripePriceId("price_test_premium");
        premiumPlan = planRepository.save(premiumPlan);
    }
    
    @Test
    void testAllPlans() throws Exception {
    	mockMvc.perform(get("/api/plans")
    				.header("Authorization", "Bearer " + userToken))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("FREE"))
                .andExpect(jsonPath("$[1].name").value("PREMIUM"));
    }
    
    @Test
    void testGetCurrentSubscription_NoSubscription() throws Exception {
        mockMvc.perform(get("/api/subscriptions/current")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planName").value("FREE"))
            .andExpect(jsonPath("$.status").value("NONE"));
    }
    
    @Test
    @Disabled("Requires Stripe API - enable for real integration testing")
    void testCreateSubscription() throws Exception {
        SubscribeRequest request = new SubscribeRequest();
        request.setPlanId(premiumPlan.getId());
        
        mockMvc.perform(post("/api/subscriptions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.planName").value("PREMIUM"))
            .andExpect(jsonPath("$.status").value("PENDING"));
        
        // Verify subscription created in database
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions.get(0).getSubscriptionPlan().getName()).isEqualTo("PREMIUM");
        assertThat(subscriptions.get(0).getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }
    
}
