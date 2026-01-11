import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { decodeToken, isTokenExpired, getUsernameFromToken, isValidToken } from '@/lib/jwt';

describe('JWT Utilities', () => {
  describe('decodeToken', () => {
    it('decodeToken_ValidToken_ReturnsPayload', () => {
      // Arrange
      const payload = { sub: 'testuser', iat: 1234567890, exp: 9999999999 };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = decodeToken(token);

      // Assert
      expect(result).toEqual(payload);
    });

    it('decodeToken_TokenWithUrlSafeBase64_ReturnsPayload', () => {
      // Arrange
      const payload = { sub: 'user', iat: 1234567890, exp: 9999999999 };
      const encodedPayload = btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_');
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = decodeToken(token);

      // Assert
      expect(result).not.toBeNull();
      expect(result?.sub).toBe('user');
    });

    it('decodeToken_InvalidFormat_ReturnsNull', () => {
      // Arrange
      const invalidToken = 'not.a.valid.token.format';

      // Act
      const result = decodeToken(invalidToken);

      // Assert
      expect(result).toBeNull();
    });

    it('decodeToken_MalformedPayload_ReturnsNull', () => {
      // Arrange
      const token = 'header.not-base64!!!.signature';

      // Act
      const result = decodeToken(token);

      // Assert
      expect(result).toBeNull();
    });

    it('decodeToken_EmptyString_ReturnsNull', () => {
      // Arrange
      const token = '';

      // Act
      const result = decodeToken(token);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('isTokenExpired', () => {
    beforeEach(() => {
      // Mock Date.now() to return a fixed timestamp
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2024-01-01T12:00:00Z'));
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('isTokenExpired_ValidToken_ReturnsFalse', () => {
      // Arrange - token expires in the future
      const futureExp = Math.floor(Date.now() / 1000) + 3600; // 1 hour from now
      const payload = { sub: 'testuser', iat: 1234567890, exp: futureExp };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = isTokenExpired(token);

      // Assert
      expect(result).toBe(false);
    });

    it('isTokenExpired_ExpiredToken_ReturnsTrue', () => {
      // Arrange - token expired in the past
      const pastExp = Math.floor(Date.now() / 1000) - 3600; // 1 hour ago
      const payload = { sub: 'testuser', iat: 1234567890, exp: pastExp };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = isTokenExpired(token);

      // Assert
      expect(result).toBe(true);
    });

    it('isTokenExpired_TokenAtExactExpirationTime_ReturnsTrue', () => {
      // Arrange - token expires at exactly current time
      const currentExp = Math.floor(Date.now() / 1000);
      const payload = { sub: 'testuser', iat: 1234567890, exp: currentExp };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = isTokenExpired(token);

      // Assert
      expect(result).toBe(true);
    });

    it('isTokenExpired_InvalidToken_ReturnsTrue', () => {
      // Arrange
      const invalidToken = 'invalid.token';

      // Act
      const result = isTokenExpired(invalidToken);

      // Assert
      expect(result).toBe(true);
    });

    it('isTokenExpired_TokenWithoutExpField_ReturnsTrue', () => {
      // Arrange
      const payload = { sub: 'testuser', iat: 1234567890 }; // No exp field
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = isTokenExpired(token);

      // Assert
      expect(result).toBe(true);
    });
  });

  describe('getUsernameFromToken', () => {
    it('getUsernameFromToken_ValidToken_ReturnsUsername', () => {
      // Arrange
      const payload = { sub: 'john.doe', iat: 1234567890, exp: 9999999999 };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = getUsernameFromToken(token);

      // Assert
      expect(result).toBe('john.doe');
    });

    it('getUsernameFromToken_InvalidToken_ReturnsNull', () => {
      // Arrange
      const invalidToken = 'invalid.token';

      // Act
      const result = getUsernameFromToken(invalidToken);

      // Assert
      expect(result).toBeNull();
    });

    it('getUsernameFromToken_TokenWithoutSubField_ReturnsNull', () => {
      // Arrange
      const payload = { iat: 1234567890, exp: 9999999999 }; // No sub field
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;

      // Act
      const result = getUsernameFromToken(token);

      // Assert
      expect(result).toBeNull();
    });
  });

  describe('isValidToken', () => {
    beforeEach(() => {
      vi.useFakeTimers();
      vi.setSystemTime(new Date('2024-01-01T12:00:00Z'));
      localStorage.clear();
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('isValidToken_ValidTokenInLocalStorage_ReturnsTrue', () => {
      // Arrange
      const futureExp = Math.floor(Date.now() / 1000) + 3600;
      const payload = { sub: 'testuser', iat: 1234567890, exp: futureExp };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;
      localStorage.setItem('token', token);

      // Act
      const result = isValidToken();

      // Assert
      expect(result).toBe(true);
    });

    it('isValidToken_ExpiredTokenInLocalStorage_ReturnsFalse', () => {
      // Arrange
      const pastExp = Math.floor(Date.now() / 1000) - 3600;
      const payload = { sub: 'testuser', iat: 1234567890, exp: pastExp };
      const encodedPayload = btoa(JSON.stringify(payload));
      const token = `header.${encodedPayload}.signature`;
      localStorage.setItem('token', token);

      // Act
      const result = isValidToken();

      // Assert
      expect(result).toBe(false);
    });

    it('isValidToken_NoTokenInLocalStorage_ReturnsFalse', () => {
      // Arrange - no token in localStorage

      // Act
      const result = isValidToken();

      // Assert
      expect(result).toBe(false);
    });

    it('isValidToken_InvalidTokenInLocalStorage_ReturnsFalse', () => {
      // Arrange
      localStorage.setItem('token', 'invalid.token');

      // Act
      const result = isValidToken();

      // Assert
      expect(result).toBe(false);
    });
  });
});
