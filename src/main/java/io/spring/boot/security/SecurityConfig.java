package io.spring.boot.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity //Turns on Spring Security’s filter system
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthFilter;
	
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		
		http
			.csrf(csrf -> csrf.disable())		//JWT is not vulnerable to CSRF (no cookies).
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless session management (no server-side session, relies on JWT)
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.authorizeHttpRequests(auth -> auth		// Configure authorization rules for HTTP requests
					// Public endpoints (no authentication required)
					.requestMatchers("/api/auth/**", "/actuator/health", "/api/webhooks/**").permitAll()
		            // Swagger UI
		            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
		            // Admin endpoints - secured by @PreAuthorize in controller
		            .requestMatchers("/api/admin/**").authenticated()
		            // All other endpoints require authentication
					.anyRequest().authenticated()	// All other requests require authentication
			)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add JwtAuthFilter before Spring's Security filter
		
		return http.build();
	}
	
}
