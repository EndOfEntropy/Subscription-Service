package io.spring.boot.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.boot.dto.UserUpdateRequest;
import io.spring.boot.entity.Role;
import io.spring.boot.entity.Subscription;
import io.spring.boot.entity.SubscriptionStatus;
import io.spring.boot.entity.User;
import io.spring.boot.repository.UserRepository;
import io.spring.boot.security.JwtService;

@Service
public class UserService {
	
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	private UserDetailsService userDetailsService;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			UserDetailsService userDetailsService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Transactional(readOnly = false)
	public User register(String email, String password) {
		if(userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email already exists");
		}
		
		User newUser = new User(email, passwordEncoder.encode(password));
		
		return userRepository.save(newUser);
	}
	
	@Transactional(readOnly = true)
	public User login(String email, String password) {
		// verify password
		User user = (User) userDetailsService.loadUserByUsername(email);
		
		if(!passwordEncoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("Invalid Credentials"); //AuthenticationException
		}
		
		return user;
	}
	
	public String generateToken(User user) {
		return jwtService.generateToken(user);
	}

	@Transactional(readOnly = true)
	public User findById(Long id){
		
		return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
	}
	
	@Transactional(readOnly = true)
	public User findByEmail(String email){
		return userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("User not found"));
	}
	
	@Transactional(readOnly = true)
	public boolean existByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public Subscription getCurrentSubscription(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + userId));
		
		return user.getSubscriptions().stream()
								.filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
								.findFirst()
								.orElse(null);
	}

	@Transactional
	public User updateProfile(Long id, UserUpdateRequest request) {
		User existingUser = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + id));
		
		// Business logic validation
	    if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
	        throw new IllegalArgumentException("Email has already been taken");
	    }
	    // Update fields
	    if (request.getEmail() != null) {
	        existingUser.setEmail(request.getEmail());
	    }
	    if (request.getPassword() != null) {
	        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
	    }
		
		return userRepository.save(existingUser);
	}
	
	@Transactional(readOnly = true)
	public List<User> findAllUsers(){
		return userRepository.findAll();
	}
	
	@Transactional
	public void updateUserRole(Long userId, Role role) {
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new NoSuchElementException("No existing user with given id: " + userId));
		
		existingUser.setRole(role);
		userRepository.save(existingUser);
	}
}
