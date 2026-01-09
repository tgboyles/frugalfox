/**
 * JWT token utilities for client-side token validation
 */

interface JwtPayload {
  sub: string; // username
  iat: number; // issued at (seconds)
  exp: number; // expiration (seconds)
}

/**
 * Decodes a JWT token without verification (client-side only)
 * @param token - The JWT token string
 * @returns The decoded payload or null if invalid
 */
export function decodeToken(token: string): JwtPayload | null {
  try {
    // JWT format: header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    // Decode the payload (second part)
    let payload = parts[1];
    // Add padding if needed for Base64 decoding
    payload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padding = payload.length % 4;
    if (padding > 0) {
      payload += '='.repeat(4 - padding);
    }
    const decoded = atob(payload);
    return JSON.parse(decoded) as JwtPayload;
  } catch (error) {
    console.error('Failed to decode token:', error);
    return null;
  }
}

/**
 * Checks if a JWT token is expired
 * @param token - The JWT token string
 * @returns true if expired, false if still valid
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeToken(token);
  if (!payload || !payload.exp) {
    return true;
  }

  // exp is in seconds, Date.now() is in milliseconds
  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();

  return currentTime >= expirationTime;
}

/**
 * Gets the username from a JWT token
 * @param token - The JWT token string
 * @returns The username or null if invalid
 */
export function getUsernameFromToken(token: string): string | null {
  const payload = decodeToken(token);
  return payload?.sub || null;
}

/**
 * Validates a token from localStorage
 * @returns true if token exists and is not expired
 */
export function isValidToken(): boolean {
  const token = localStorage.getItem('token');
  if (!token) {
    return false;
  }

  return !isTokenExpired(token);
}
