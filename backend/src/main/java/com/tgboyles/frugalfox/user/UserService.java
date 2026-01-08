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
}
