package com.tgboyles.frugalfox.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for UserService. */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock private UserRepository userRepository;

	@Mock private PasswordEncoder passwordEncoder;

	private UserService userService;

	@BeforeEach
	public void setup() {
		userService = new UserService(userRepository, passwordEncoder);
	}

	@Test
	public void updateEmail_NewEmail_UpdatesEmailSuccessfully() {
		// Arrange
		User user = new User("testuser", "hashedPassword", "old@example.com");
		user.setId(1L);

		when(userRepository.existsByEmailAndIdNot("new@example.com", 1L)).thenReturn(false);
		when(userRepository.save(any(User.class))).thenReturn(user);

		// Act
		User result = userService.updateEmail(user, "new@example.com");

		// Assert
		assertEquals("new@example.com", result.getEmail());
		verify(userRepository).existsByEmailAndIdNot("new@example.com", 1L);
		verify(userRepository).save(user);
	}

	@Test
	public void updateEmail_DuplicateEmail_ThrowsIllegalArgumentException() {
		// Arrange
		User user = new User("testuser", "hashedPassword", "old@example.com");
		user.setId(1L);

		when(userRepository.existsByEmailAndIdNot("existing@example.com", 1L)).thenReturn(true);

		// Act & Assert
		IllegalArgumentException exception =
			assertThrows(
				IllegalArgumentException.class,
				() -> userService.updateEmail(user, "existing@example.com"));

		assertEquals("Email already in use: existing@example.com", exception.getMessage());
		verify(userRepository).existsByEmailAndIdNot("existing@example.com", 1L);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	public void updatePassword_CorrectCurrentPassword_UpdatesPasswordSuccessfully() {
		// Arrange
		User user = new User("testuser", "oldHashedPassword", "test@example.com");
		user.setId(1L);

		when(passwordEncoder.matches("oldPassword", "oldHashedPassword")).thenReturn(true);
		when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
		when(userRepository.save(any(User.class))).thenReturn(user);

		// Act
		User result = userService.updatePassword(user, "oldPassword", "newPassword");

		// Assert
		assertEquals("newHashedPassword", result.getPassword());
		verify(passwordEncoder).matches("oldPassword", "oldHashedPassword");
		verify(passwordEncoder).encode("newPassword");
		verify(userRepository).save(user);
	}

	@Test
	public void updatePassword_IncorrectCurrentPassword_ThrowsIllegalArgumentException() {
		// Arrange
		User user = new User("testuser", "hashedPassword", "test@example.com");
		user.setId(1L);

		when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

		// Act & Assert
		IllegalArgumentException exception =
			assertThrows(
				IllegalArgumentException.class,
				() -> userService.updatePassword(user, "wrongPassword", "newPassword"));

		assertEquals("Current password is incorrect", exception.getMessage());
		verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}
}
