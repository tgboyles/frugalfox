import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { renderHook } from '@testing-library/react';
import { AuthProvider, useAuth } from '@/contexts/AuthContext';
import { authApi } from '@/lib/api';
import * as jwtUtils from '@/lib/jwt';

// Mock the API module
vi.mock('@/lib/api', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
  },
}));

// Mock JWT utilities
vi.mock('@/lib/jwt', () => ({
  isValidToken: vi.fn(),
  getUsernameFromToken: vi.fn(),
}));

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('useAuth hook', () => {
    it('useAuth_OutsideProvider_ThrowsError', () => {
      // Arrange & Act & Assert
      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within an AuthProvider');
    });
  });

  describe('AuthProvider initialization', () => {
    it('AuthProvider_NoToken_InitializesAsUnauthenticated', () => {
      // Arrange
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);

      // Act
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Assert
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.username).toBeNull();
      expect(result.current.isLoading).toBe(false);
    });

    it('AuthProvider_ValidToken_InitializesAsAuthenticated', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Assert
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.username).toBe(mockUsername);
      expect(result.current.isLoading).toBe(false);
    });

    it('AuthProvider_ExpiredToken_ClearsLocalStorageAndInitializesAsUnauthenticated', () => {
      // Arrange
      localStorage.setItem('token', 'expired.jwt.token');
      localStorage.setItem('username', 'testuser');
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);

      // Act
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Assert
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.username).toBeNull();
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('username')).toBeNull();
    });
  });

  describe('login', () => {
    it('login_ValidCredentials_SetsAuthenticationState', async () => {
      // Arrange
      const mockToken = 'new.jwt.token';
      const mockUsername = 'testuser';
      const mockPassword = 'password123';
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false); // Start unauthenticated
      vi.mocked(authApi.login).mockResolvedValue({
        data: { token: mockToken },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Verify starting state
      expect(result.current.isAuthenticated).toBe(false);

      // Act
      await result.current.login(mockUsername, mockPassword);

      // Assert - wrap state checks in waitFor
      await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

      expect(authApi.login).toHaveBeenCalledWith(mockUsername, mockPassword);
      expect(result.current.username).toBe(mockUsername);
    });

    it('login_InvalidCredentials_ThrowsError', async () => {
      // Arrange
      const mockUsername = 'testuser';
      const mockPassword = 'wrongpassword';
      const mockError = new Error('Invalid credentials');
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false); // Start unauthenticated
      vi.mocked(authApi.login).mockRejectedValue(mockError);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Act & Assert
      await expect(result.current.login(mockUsername, mockPassword)).rejects.toThrow(
        'Invalid credentials'
      );

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.username).toBeNull();
    });
  });

  describe('register', () => {
    it('register_ValidData_RegistersAndLogsIn', async () => {
      // Arrange
      const mockToken = 'new.jwt.token';
      const mockUsername = 'newuser';
      const mockPassword = 'password123';
      const mockEmail = 'newuser@example.com';

      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false); // Start unauthenticated
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      vi.mocked(authApi.register).mockResolvedValue({ data: {} } as any);
      vi.mocked(authApi.login).mockResolvedValue({
        data: { token: mockToken },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } as any);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Act
      await result.current.register(mockUsername, mockPassword, mockEmail);

      // Assert
      await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

      expect(authApi.register).toHaveBeenCalledWith(mockUsername, mockPassword, mockEmail);
      expect(authApi.login).toHaveBeenCalledWith(mockUsername, mockPassword);
      expect(result.current.username).toBe(mockUsername);
    });

    it('register_RegistrationFails_ThrowsError', async () => {
      // Arrange
      const mockUsername = 'newuser';
      const mockPassword = 'password123';
      const mockEmail = 'newuser@example.com';
      const mockError = new Error('Username already exists');
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false); // Start unauthenticated
      vi.mocked(authApi.register).mockRejectedValue(mockError);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Act & Assert
      await expect(result.current.register(mockUsername, mockPassword, mockEmail)).rejects.toThrow(
        'Username already exists'
      );

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.username).toBeNull();
    });

    it('register_LoginAfterRegistrationFails_ThrowsError', async () => {
      // Arrange
      const mockUsername = 'newuser';
      const mockPassword = 'password123';
      const mockEmail = 'newuser@example.com';
      const mockError = new Error('Login failed');

      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false); // Start unauthenticated
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      vi.mocked(authApi.register).mockResolvedValue({ data: {} } as any);
      vi.mocked(authApi.login).mockRejectedValue(mockError);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Act & Assert
      await expect(result.current.register(mockUsername, mockPassword, mockEmail)).rejects.toThrow(
        'Login failed'
      );
    });
  });

  describe('logout', () => {
    it('logout_WhenAuthenticated_ClearsAuthenticationState', async () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Verify initial state
      expect(result.current.isAuthenticated).toBe(true);

      // Act
      result.current.logout();

      // Assert - wrap state checks in waitFor
      await waitFor(() => expect(result.current.isAuthenticated).toBe(false));
      expect(result.current.username).toBeNull();
    });

    it('logout_WhenNotAuthenticated_NoErrors', () => {
      // Arrange
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Act
      result.current.logout();

      // Assert
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.username).toBeNull();
    });
  });

  describe('children rendering', () => {
    it('AuthProvider_RendersChildren', () => {
      // Arrange
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);
      const testMessage = 'Test Child Component';

      // Act
      render(
        <AuthProvider>
          <div>{testMessage}</div>
        </AuthProvider>
      );

      // Assert
      expect(screen.getByText(testMessage)).toBeInTheDocument();
    });
  });
});
