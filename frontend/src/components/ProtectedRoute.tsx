import { Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { isValidToken } from '@/lib/jwt';
import { useEffect, useRef } from 'react';

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading, logout } = useAuth();
  const hasCheckedToken = useRef(false);

  // Check token validity once when component mounts
  useEffect(() => {
    if (isAuthenticated && !hasCheckedToken.current && !isValidToken()) {
      // Token has expired - log out the user
      hasCheckedToken.current = true;
      logout();
    }
  }, [isAuthenticated, logout]);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
