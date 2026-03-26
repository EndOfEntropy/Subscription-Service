package io.spring.boot.dto;

import java.util.List;

public class ErrorResponse {
	
	private List<String> body;

	public ErrorResponse(List<String> body) {
		this.body = body;
	}

	public List<String> getBody() {
		return body;
	}

	public void setBody(List<String> body) {
		this.body = body;
	}
    
}
