package com.tgboyles.frugalfox.security;

import com.tgboyles.frugalfox.user.User;
import com.tgboyles.frugalfox.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication endpoints.
 *
 * <p>Handles user registration and login operations.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  public AuthController(
      AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.userService = userService;
    this.jwtUtil = jwtUtil;
  }

  /**
   * Registers a new user.
   *
   * @param registerRequest the registration request
   * @return the authentication response with JWT token
   */
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
    try {
      User user =
          userService.registerUser(
              registerRequest.getUsername(),
              registerRequest.getPassword(),
              registerRequest.getEmail());

      UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
      String token = jwtUtil.generateToken(userDetails);

      AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail());
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException(e.getMessage());
    }
  }

  /**
   * Authenticates a user and returns a JWT token.
   *
   * @param authRequest the authentication request
   * @return the authentication response with JWT token
   */
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  authRequest.getUsername(), authRequest.getPassword()));

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String token = jwtUtil.generateToken(userDetails);

      User user = userService.findByUsername(userDetails.getUsername());
      AuthResponse response = new AuthResponse(token, user.getUsername(), user.getEmail());

      return ResponseEntity.ok(response);
    } catch (BadCredentialsException e) {
      throw new BadCredentialsException("Invalid username or password");
    }
  }
}
