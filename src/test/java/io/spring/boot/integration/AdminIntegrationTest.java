package io.spring.boot.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import io.spring.boot.AbstractIntegrationTest;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.service.UserService;

@AutoConfigureMockMvc
public class AdminIntegrationTest extends AbstractIntegrationTest {
    
	@Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;
    
    private String adminToken;
    private String userToken;
    
    
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        
        // Create admin user
        User admin = new User("admin@test.com", passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);
        adminToken = userService.generateToken(admin);
        
        // Create regular user
        User user = new User("user@test.com", passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        user = userRepository.save(user);
        userToken = userService.generateToken(user);
    }
    
    
    @Test
    void testAdminCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2));
    }
    
    @Test
    void testRegularUserCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void testGetRevenueStats() throws Exception {
        mockMvc.perform(get("/api/admin/revenue")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRevenue").exists())
            .andExpect(jsonPath("$.totalCustomers").value(2));
    }
    
}
