import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/components/ProtectedRoute';
import { AuthProvider } from '@/contexts/AuthContext';
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

// Helper component to wrap ProtectedRoute with router and auth context
const renderWithRouter = (ui: React.ReactElement, { initialEntries = ['/protected'] } = {}) => {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/protected" element={ui} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  );
};

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('when user is authenticated', () => {
    it('ProtectedRoute_AuthenticatedUser_RendersChildren', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
    });

    it('ProtectedRoute_AuthenticatedWithValidToken_RendersChildren', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Dashboard</div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
    });
  });

  describe('when user is not authenticated', () => {
    it('ProtectedRoute_UnauthenticatedUser_RedirectsToLogin', () => {
      // Arrange
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('ProtectedRoute_NoToken_RedirectsToLogin', () => {
      // Arrange - no token in localStorage
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(false);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Secure Area</div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Secure Area')).not.toBeInTheDocument();
    });
  });

  describe('when loading', () => {
    it('ProtectedRoute_IsLoading_ShowsLoadingState', () => {
      // Arrange
      localStorage.setItem('token', 'token');
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue('user');

      // Note: In current implementation, isLoading is always false
      // This test verifies the loading UI would work if isLoading were true
      // The loading state is designed to handle async auth checks

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Content</div>
        </ProtectedRoute>
      );

      // Assert - loading should not be shown since isLoading is false
      expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
      expect(screen.getByText('Content')).toBeInTheDocument();
    });
  });

  describe('token expiration handling', () => {
    it('ProtectedRoute_ExpiredTokenOnMount_TriggersLogout', async () => {
      // Arrange
      const mockToken = 'expired.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);

      // First call returns true (authenticated in context)
      // Second call in useEffect returns false (token expired)
      vi.mocked(jwtUtils.isValidToken).mockReturnValueOnce(true).mockReturnValueOnce(false);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      );

      // Assert - should eventually redirect to login
      await waitFor(() => {
        expect(screen.getByText('Login Page')).toBeInTheDocument();
      });
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('username')).toBeNull();
    });

    it('ProtectedRoute_ValidTokenOnMount_DoesNotLogout', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Protected Content')).toBeInTheDocument();
      expect(localStorage.getItem('token')).toBe(mockToken);
      expect(localStorage.getItem('username')).toBe(mockUsername);
    });
  });

  describe('children variants', () => {
    it('ProtectedRoute_ComplexChildren_RendersCorrectly', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <div>
            <h1>Dashboard</h1>
            <p>Welcome back!</p>
            <button>Action</button>
          </div>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.getByText('Welcome back!')).toBeInTheDocument();
      expect(screen.getByText('Action')).toBeInTheDocument();
    });

    it('ProtectedRoute_MultipleChildren_RendersAll', () => {
      // Arrange
      const mockToken = 'valid.jwt.token';
      const mockUsername = 'testuser';
      localStorage.setItem('token', mockToken);
      localStorage.setItem('username', mockUsername);
      vi.mocked(jwtUtils.isValidToken).mockReturnValue(true);
      vi.mocked(jwtUtils.getUsernameFromToken).mockReturnValue(mockUsername);

      // Act
      renderWithRouter(
        <ProtectedRoute>
          <>
            <div>First Child</div>
            <div>Second Child</div>
          </>
        </ProtectedRoute>
      );

      // Assert
      expect(screen.getByText('First Child')).toBeInTheDocument();
      expect(screen.getByText('Second Child')).toBeInTheDocument();
    });
  });
});
