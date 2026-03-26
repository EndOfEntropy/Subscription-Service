package io.spring.boot.dto;

public class UpdateRoleRequest {

	private String role;
	
	// Jackson for instantiation
	public UpdateRoleRequest() {
	}

	public UpdateRoleRequest(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}
	// Jackson for population
	public void setRole(String role) {
		this.role = role;
	}
	
}
