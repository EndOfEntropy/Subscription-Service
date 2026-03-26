package io.spring.boot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

	@Email(message = "Invalid email format")
	private String email;
	
	@Size(min=6, message = "Password must be at least 6 characters")
	private String password;

	public UserUpdateRequest(@Email(message = "Invalid email format") String email,
			@Size(min = 6, message = "Password must be at least 6 characters") String password) {
		this.email = email;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
