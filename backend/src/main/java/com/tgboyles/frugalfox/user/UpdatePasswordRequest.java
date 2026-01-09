package com.tgboyles.frugalfox.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user password.
 */
public class UpdatePasswordRequest {

	@NotBlank(message = "Current password is required")
	private String currentPassword;

	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "New password must be at least 8 characters")
	private String newPassword;

	public UpdatePasswordRequest() {}

	public UpdatePasswordRequest(String currentPassword, String newPassword) {
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
