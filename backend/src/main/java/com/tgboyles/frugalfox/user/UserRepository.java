package com.tgboyles.frugalfox.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity.
 *
 * <p>Provides database operations for user management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

/**
* Finds a user by username.
*
* @param username the username to search for
* @return an Optional containing the user if found
*/
Optional<User> findByUsername(String username);

/**
* Checks if a username already exists.
*
* @param username the username to check
* @return true if the username exists
*/
boolean existsByUsername(String username);

/**
* Checks if an email already exists.
*
* @param email the email to check
* @return true if the email exists
*/
boolean existsByEmail(String email);

/**
* Checks if an email already exists for a different user.
*
* @param email the email to check
* @param userId the user ID to exclude from the check
* @return true if the email exists for a different user
*/
boolean existsByEmailAndIdNot(String email, Long userId);
}
