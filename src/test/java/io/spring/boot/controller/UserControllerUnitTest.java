package io.spring.boot.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.dto.UserUpdateRequest;
import io.spring.boot.entity.User;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.UserService;

@WebMvcTest(UserRestController.class)
//@AutoConfigureMockMvc(addFilters = false) 	// disable security filters
@Import(SecurityConfig.class)					// disable security config
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;
    // Needed since @Import(SecurityConfig.class) is enabled. SecurityConfig -> JwtAuthenticationFilter -> (JwtService & UserDetailsService)
    @MockBean
    private JwtService jwtService;
    @MockBean
    UserDetailsService userDetailsService;
    

    private User testUser;
    
    private void testSetup() {
        testUser = new User(1L, "test@test.com", "password123");
    }
    
    @Test
    public void register_shouldCreateUserAndReturnToken() throws Exception {
    	// Setup
    	testSetup();
        given(userService.register(anyString(), anyString())).willReturn(testUser);
        given(userService.generateToken(any(User.class))).willReturn("fake-jwt-token");
        
        // Action
        ResultActions response = mockMvc.perform(post("/api/auth/register")
						                .contentType(MediaType.APPLICATION_JSON)
						                .content(objectMapper.writeValueAsString(testUser)));

        // Verify
        response.andDo(print())
	        .andExpect(status().isCreated())
	        .andExpect(jsonPath("$.token").value("fake-jwt-token"))
	        .andExpect(jsonPath("$.email").value("test@test.com"));
        	
    }

    @Test
    public void login_shouldReturnToken() throws Exception {
    	// Setup
    	testSetup();
        given(userService.login(anyString(), anyString())).willReturn(testUser);
        given(userService.generateToken(any(User.class))).willReturn("fake-jwt-token");
        
     	// Action
        ResultActions response = mockMvc.perform(post("/api/auth/login")
						                .contentType(MediaType.APPLICATION_JSON)
						                .content(objectMapper.writeValueAsString(testUser)));
        
        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void login_shouldFailWithWrongPassword() throws Exception {
    	// Setup
    	User unauthUser = new User("test@test.com", "wrongpassword");
    	given(userService.login(anyString(), anyString())).willThrow(new BadCredentialsException("Bad credentials"));
    	
    	// Action
    	ResultActions response = mockMvc.perform(post("/api/auth/login")
						                .contentType(MediaType.APPLICATION_JSON)
						                .content(objectMapper.writeValueAsString(unauthUser)));

        // Verify
        response.andDo(print())                
        	.andExpect(status().isUnauthorized());
    }
    
    @Test
    public void getProfile_shouldReturnProfile() throws Exception {
        // Setup
        testSetup();
        given(userService.getCurrentSubscription(1L)).willReturn(null); // no subscription

        // Action
        ResultActions response = mockMvc.perform(get("/api/user/profile")
        		.with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("test@test.com"))
            .andExpect(jsonPath("$.currentPlan").value("FREE"))
            .andExpect(jsonPath("$.subscriptionStatus").value("NONE"));
    }
    
    @Test
    public void updateProfile_shouldUpdateAndReturnProfile() throws Exception {
        // Setup
        testSetup();

        User updatedUser = new User("newemail@test.com", "password123");
        UserUpdateRequest updateRequest = new UserUpdateRequest("newemail@test.com", "password123");
        
        given(userService.updateProfile(eq(1L), any(UserUpdateRequest.class))).willReturn(updatedUser);
        given(userService.getCurrentSubscription(1L)).willReturn(null);		// no subscription

        // Action
        ResultActions response = mockMvc.perform(put("/api/user/profile")
        		.with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // Verify
        response.andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("newemail@test.com"))
            .andExpect(jsonPath("$.currentPlan").value("FREE"))
            .andExpect(jsonPath("$.subscriptionStatus").value("NONE"));
    }

}
