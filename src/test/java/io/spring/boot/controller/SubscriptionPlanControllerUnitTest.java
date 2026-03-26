package io.spring.boot.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.security.JwtService;
import io.spring.boot.service.SubscriptionPlanService;
import io.spring.boot.service.UserService;

@WebMvcTest(SubscriptionPlanController.class)
@AutoConfigureMockMvc(addFilters = false) 	// disable security filters
//@Import(SecurityConfig.class)				// disable security config
public class SubscriptionPlanControllerUnitTest {
    
    @Autowired
    private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
    
    @MockBean
    private SubscriptionPlanService planService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    
    private SubscriptionPlan free;
    private SubscriptionPlan premium;
    
    private void testSetup() {
        free = new SubscriptionPlan(1L, "FREE", BigDecimal.ZERO, BillingCycle.MONTHLY, "Basic");
        premium = new SubscriptionPlan(2L, "PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, "All features");
    }
    
    @Test
    public void testGetAllPlans() throws Exception {
        // Setup
    	testSetup();
        given(planService.findAll()).willReturn(List.of(free, premium));
        
        // Action
        ResultActions response = mockMvc.perform(get("/api/plans"));
        
        // Verify
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].name").value("FREE"))
            .andExpect(jsonPath("$[1].price").value(9.99));
    }
    
    @Test
    public void testGetPlanById() throws Exception {
        // Setup
        testSetup();
        given(planService.findById(1L)).willReturn(free);

        // Action
        ResultActions response = mockMvc.perform(get("/api/plans/{id}", 1L));
        
        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("FREE"))
                .andExpect(jsonPath("$.price").value(0))
                .andExpect(jsonPath("$.billingCycle").value("MONTHLY"));
    }
}
