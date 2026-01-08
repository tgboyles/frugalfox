package com.tgboyles.frugalfox.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User entity for authentication and authorization.
 *
 * <p>Implements Spring Security's UserDetails interface to integrate with the security framework.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@NotBlank(message = "Username is required")
@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
@Column(nullable = false, unique = true, length = 50)
private String username;

@NotBlank(message = "Password is required")
@Size(min = 8, message = "Password must be at least 8 characters")
@Column(nullable = false)
private String password;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
@Column(nullable = false)
private String email;

@Column(nullable = false)
private boolean enabled = true;

@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

/** Default constructor for JPA. */
public User() {}

/**
* Constructor for creating a new user.
*
* @param username the username
* @param password the password (should be BCrypt hashed)
* @param email the email address
*/
public User(String username, String password, String email) {
	this.username = username;
	this.password = password;
	this.email = email;
	this.enabled = true;
}

@PrePersist
protected void onCreate() {
	createdAt = LocalDateTime.now();
	updatedAt = LocalDateTime.now();
}

@PreUpdate
protected void onUpdate() {
	updatedAt = LocalDateTime.now();
}

// UserDetails interface methods

@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
	return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
}

@Override
public String getPassword() {
	return password;
}

@Override
public String getUsername() {
	return username;
}

@Override
public boolean isAccountNonExpired() {
	return true;
}

@Override
public boolean isAccountNonLocked() {
	return true;
}

@Override
public boolean isCredentialsNonExpired() {
	return true;
}

@Override
public boolean isEnabled() {
	return enabled;
}

// Getters and setters

public Long getId() {
	return id;
}

public void setId(Long id) {
	this.id = id;
}

public void setUsername(String username) {
	this.username = username;
}

public void setPassword(String password) {
	this.password = password;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public void setEnabled(boolean enabled) {
	this.enabled = enabled;
}

public LocalDateTime getCreatedAt() {
	return createdAt;
}

public LocalDateTime getUpdatedAt() {
	return updatedAt;
}

@Override
public boolean equals(Object o) {
	if (this == o) {
	return true;
	}
	if (!(o instanceof User)) {
	return false;
	}
	User user = (User) o;
	return id != null && id.equals(user.id);
}

@Override
public int hashCode() {
	return getClass().hashCode();
}

@Override
public String toString() {
	return "User{"
		+ "id="
		+ id
		+ ", username='"
		+ username
		+ '\''
		+ ", email='"
		+ email
		+ '\''
		+ ", enabled="
		+ enabled
		+ '}';
}
}
