package io.spring.boot.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.spring.boot.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// For every request: Browser → Filter → Spring Security → Controller
// Filter's job: If the request has a JWT → authenticate the user, If not → do nothing and let Spring Security handle it
// Dependencies: JwtService → to parse token & UserDetailsService → to load user from DB

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Get the Authorization header from the request (e.g., "Bearer <jwt>")
		final String authHeader = request.getHeader("Authorization");
		
		// check for public or unauthenticated requests
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);		// call the next filter layer in the chain (controller)
			return;
		}
		// Extract the JWT by removing "Bearer " & Extract the username (email) from the JWT using JwtService
		final String jwt = authHeader.substring(7);
		final String email = jwtService.extractUsername(jwt);
		
		// Proceed only if email is valid and no user is already authenticated
		if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			// Load UserDetails (our User entity) by email using UserService
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);
			// Validate the JWT (checks signature and expiration) using JwtService
			if(jwtService.isTokenValid(jwt, userDetails)) {
				// Create an authentication token with user details, no credentials (JWT-based), and authorities
				UsernamePasswordAuthenticationToken authToken = 
						new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				// Add request metadata (e.g., IP, session) to the token for auditing
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// Set the authenticated user in Spring Security's context for use in controllers/services
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		
		filterChain.doFilter(request, response); // call the next filter layer in the chain (controller)
	}

}