package com.tgboyles.frugalfox.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utility class for JWT token operations.
 *
 * <p>Handles token generation, validation, and extraction of claims.
 */
@Component
public class JwtUtil {

@Value("${jwt.secret}")
private String secret;

@Value("${jwt.expiration}")
private Long expiration;

/**
* Generates a JWT token for the given user.
*
* @param userDetails the user details
* @return the generated JWT token
*/
public String generateToken(UserDetails userDetails) {
	Map<String, Object> claims = new HashMap<>();
	return createToken(claims, userDetails.getUsername());
}

private String createToken(Map<String, Object> claims, String subject) {
	Date now = new Date();
	Date expiryDate = new Date(now.getTime() + expiration);

	return Jwts.builder()
		.claims(claims)
		.subject(subject)
		.issuedAt(now)
		.expiration(expiryDate)
		.signWith(getSigningKey())
		.compact();
}

private SecretKey getSigningKey() {
	byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
	return Keys.hmacShaKeyFor(keyBytes);
}

/**
* Extracts the username from the JWT token.
*
* @param token the JWT token
* @return the username
*/
public String extractUsername(String token) {
	return extractClaim(token, Claims::getSubject);
}

/**
* Extracts the expiration date from the JWT token.
*
* @param token the JWT token
* @return the expiration date
*/
public Date extractExpiration(String token) {
	return extractClaim(token, Claims::getExpiration);
}

/**
* Extracts a specific claim from the JWT token.
*
* @param token the JWT token
* @param claimsResolver function to extract the claim
* @param <T> the type of the claim
* @return the extracted claim
*/
public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
	final Claims claims = extractAllClaims(token);
	return claimsResolver.apply(claims);
}

private Claims extractAllClaims(String token) {
	return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
}

private Boolean isTokenExpired(String token) {
	return extractExpiration(token).before(new Date());
}

/**
* Validates the JWT token against user details.
*
* @param token the JWT token
* @param userDetails the user details
* @return true if the token is valid
*/
public Boolean validateToken(String token, UserDetails userDetails) {
	final String username = extractUsername(token);
	return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
}
