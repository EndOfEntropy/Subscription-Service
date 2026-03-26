package io.spring.boot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

	@Email
	private final String email;
	@NotBlank
	private final String password;
	
	public RegisterRequest(@Email String email, @NotBlank String password) {
		this.email = email;
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	
}
