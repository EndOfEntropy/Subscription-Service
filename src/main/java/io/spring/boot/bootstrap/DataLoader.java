package io.spring.boot.bootstrap;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.spring.boot.entity.BillingCycle;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.SubscriptionPlan;
import io.spring.boot.entity.User;
import io.spring.boot.repository.SubscriptionPlanRepository;
import io.spring.boot.repository.UserRepository;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {
    
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
	public DataLoader(SubscriptionPlanRepository planRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.planRepository = planRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}


	@Override
    public void run(String... args) throws Exception {
        // Only seed if plans don't exist
        if (planRepository.count() == 0) {
        	// No Stripe Price Id for the free plan
            SubscriptionPlan free = new SubscriptionPlan("FREE", BigDecimal.ZERO,BillingCycle.MONTHLY, "Basic features only", null); 

            SubscriptionPlan premium = new SubscriptionPlan("PREMIUM", new BigDecimal("9.99"), BillingCycle.MONTHLY, 
            		"All features + priority support", "price_1StweY56qHAseVDpd4umSktc");

            SubscriptionPlan enterprise = new SubscriptionPlan("ENTERPRISE", new BigDecimal("49.99"), BillingCycle.MONTHLY,
            		"Everything + dedicated account manager", "price_1StwgD56qHAseVDp8Hvk0hlW");
            
            planRepository.saveAll(List.of(free, premium, enterprise));
        }
     // Create admin user if doesn't exist
        if(!userRepository.findByEmail("user@example.com").isPresent()) {
        	User user = new User("user@example.com", passwordEncoder.encode("password123"));
        	user.setRole(Role.USER);
        	
        	userRepository.save(user);
        	
        	System.out.println("User created: user@example.com / password");
        }
        
        // Create admin user if doesn't exist
        if(!userRepository.findByEmail("admin@company.com").isPresent()) {
        	User admin = new User("admin@company.com", passwordEncoder.encode("admin123"));
        	admin.setRole(Role.ADMIN);
        	
        	userRepository.save(admin);
        	
        	System.out.println("Admin user created: admin@company.com / admin123");
        }
    }
}