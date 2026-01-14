package com.tgboyles.frugalfox.user;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations.
 *
 * <p>Implements UserDetailsService for Spring Security integration.
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;

public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
	this.userRepository = userRepository;
	this.passwordEncoder = passwordEncoder;
}

@Override
@Transactional(readOnly = true)
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	return userRepository
		.findByUsername(username)
		.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}

/**
* Registers a new user.
*
* @param username the username
* @param password the plain-text password (will be hashed)
* @param email the email address
* @return the created user
* @throws IllegalArgumentException if username or email already exists
*/
public User registerUser(String username, String password, String email) {
	if (userRepository.existsByUsername(username)) {
	throw new IllegalArgumentException("Username already exists: " + username);
	}
	if (userRepository.existsByEmail(email)) {
	throw new IllegalArgumentException("Email already exists: " + email);
	}

	User user = new User();
	user.setUsername(username);
	user.setPassword(passwordEncoder.encode(password));
	user.setEmail(email);
	user.setEnabled(true);

	return userRepository.save(user);
}

/**
* Finds a user by username.
*
* @param username the username
* @return the user
* @throws UsernameNotFoundException if user not found
*/
@Transactional(readOnly = true)
public User findByUsername(String username) {
	return userRepository
		.findByUsername(username)
		.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}

/**
* Updates a user's email address.
*
* @param user the user to update
* @param newEmail the new email address
* @return the updated user
* @throws IllegalArgumentException if email is already in use by another user
*/
public User updateEmail(User user, String newEmail) {
	if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
		throw new IllegalArgumentException("Email already in use: " + newEmail);
	}
	user.setEmail(newEmail);
	return userRepository.save(user);
}

/**
* Updates a user's password.
*
* @param user the user to update
* @param currentPassword the current password (for verification)
* @param newPassword the new password (will be hashed)
* @return the updated user
* @throws IllegalArgumentException if current password is incorrect
*/
public User updatePassword(User user, String currentPassword, String newPassword) {
	if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
		throw new IllegalArgumentException("Current password is incorrect");
	}
	user.setPassword(passwordEncoder.encode(newPassword));
	return userRepository.save(user);
}

/**
* Deletes a user account.
*
* <p>This will cascade delete all associated expenses due to database foreign key constraints.
*
* @param user the user to delete
*/
public void deleteUser(User user) {
	userRepository.delete(user);
}
}
