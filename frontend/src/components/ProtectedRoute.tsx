import { Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { isValidToken } from '@/lib/jwt';
import { useEffect } from 'react';

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading, logout } = useAuth();

  // Check token validity on every render of a protected route
  useEffect(() => {
    if (isAuthenticated && !isValidToken()) {
      // Token has expired - log out the user
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
