/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, type ReactNode } from 'react';
import { authApi } from '@/lib/api';
import { isValidToken, getUsernameFromToken } from '@/lib/jwt';

interface AuthContextType {
  isAuthenticated: boolean;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, email: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  // Compute initial authentication state
  const getInitialAuthState = () => {
    const token = localStorage.getItem('token');

    if (token && isValidToken()) {
      // Token exists and is valid
      const usernameFromToken = getUsernameFromToken(token);
      return {
        isAuthenticated: true,
        username: usernameFromToken || localStorage.getItem('username'),
      };
    } else {
      // Token is missing or expired - clear everything
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      return {
        isAuthenticated: false,
        username: null,
      };
    }
  };

  const initialState = getInitialAuthState();
  const [isAuthenticated, setIsAuthenticated] = useState(initialState.isAuthenticated);
  const [username, setUsername] = useState<string | null>(initialState.username);

  // No loading state needed since token validation is synchronous
  const isLoading = false;

  const login = async (username: string, password: string) => {
    try {
      const response = await authApi.login(username, password);
      const { token } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('username', username);

      setIsAuthenticated(true);
      setUsername(username);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const register = async (username: string, password: string, email: string) => {
    try {
      await authApi.register(username, password, email);
      // After registration, automatically log in
      await login(username, password);
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setIsAuthenticated(false);
    setUsername(null);
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        username,
        login,
        register,
        logout,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
