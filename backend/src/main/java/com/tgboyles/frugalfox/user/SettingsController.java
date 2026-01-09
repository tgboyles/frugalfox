package com.tgboyles.frugalfox.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Controller for user settings operations.
 *
 * <p>All endpoints require authentication and operate on the authenticated user.
 */
@RestController
@RequestMapping("/settings")
public class SettingsController {

	private final UserService userService;

	public SettingsController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Updates the authenticated user's email address.
	 *
	 * @param request the update email request
	 * @param user the authenticated user
	 * @return the updated user details
	 */
	@PutMapping("/email")
	public ResponseEntity<UserResponse> updateEmail(
		@Valid @RequestBody UpdateEmailRequest request, @AuthenticationPrincipal User user) {
		User updatedUser = userService.updateEmail(user, request.getEmail());
		UserResponse response = new UserResponse(updatedUser);
		return ResponseEntity.ok(response);
	}

	/**
	 * Updates the authenticated user's password.
	 *
	 * @param request the update password request
	 * @param user the authenticated user
	 * @return success message
	 */
	@PutMapping("/password")
	public ResponseEntity<MessageResponse> updatePassword(
		@Valid @RequestBody UpdatePasswordRequest request, @AuthenticationPrincipal User user) {
		userService.updatePassword(user, request.getCurrentPassword(), request.getNewPassword());
		MessageResponse response = new MessageResponse("Password updated successfully");
		return ResponseEntity.ok(response);
	}
}
