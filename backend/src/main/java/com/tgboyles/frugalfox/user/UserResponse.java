package com.tgboyles.frugalfox.user;

import java.time.LocalDateTime;

/**
 * Response DTO for user information.
 */
public class UserResponse {

	private Long id;
	private String username;
	private String email;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public UserResponse() {}

	public UserResponse(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.createdAt = user.getCreatedAt();
		this.updatedAt = user.getUpdatedAt();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
