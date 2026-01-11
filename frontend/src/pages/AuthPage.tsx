import { useState, useEffect, useRef, useLayoutEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';
import { QUOTES } from '@/lib/quotes';

const TRANSITION_DURATION = 500; // milliseconds
const QUOTE_ROTATION_INTERVAL = 8000; // milliseconds

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [currentQuoteIndex, setCurrentQuoteIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { login, register, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  // Force light mode on login page using MutationObserver
  useLayoutEffect(() => {
    const root = document.documentElement;
    const hadDarkClass = root.classList.contains('dark');

    // Remove dark class immediately
    root.classList.remove('dark');

    // Watch for any attempts to add the dark class back and remove it
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
          if (root.classList.contains('dark')) {
            root.classList.remove('dark');
          }
        }
      });
    });

    observer.observe(root, {
      attributes: true,
      attributeFilter: ['class'],
    });

    // Restore previous state when component unmounts
    return () => {
      observer.disconnect();
      if (hadDarkClass) {
        root.classList.add('dark');
      }
    };
  }, []);

  // Redirect to dashboard if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    const interval = setInterval(() => {
      // Start fade-out
      setIsTransitioning(true);

      // Change quote after fade-out completes
      timeoutRef.current = setTimeout(() => {
        setCurrentQuoteIndex((prevIndex) => (prevIndex + 1) % QUOTES.length);
        // Start fade-in
        setIsTransitioning(false);
      }, TRANSITION_DURATION);
    }, QUOTE_ROTATION_INTERVAL);

    return () => {
      clearInterval(interval);
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      if (isLogin) {
        await login(username, password);
      } else {
        await register(username, password, email);
      }
      navigate('/dashboard');
    } catch (err: unknown) {
      const errorMessage =
        (err as { response?: { data?: { message?: string } } }).response?.data?.message ||
        `Failed to ${isLogin ? 'login' : 'register'}. Please try again.`;
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Left side - Branding */}
      <div className="bg-primary text-primary-foreground hidden flex-col justify-between p-12 lg:flex">
        <div className="space-y-4">
          <img
            src="/fox.png"
            alt="Frugal Fox Logo"
            className="border-primary-foreground/20 h-24 w-24 rounded-full border-4 bg-white object-cover p-2"
          />
          <h1 className="text-3xl font-bold">Frugal Fox</h1>
        </div>
        <div className="space-y-4">
          <blockquote
            className={`text-lg transition-opacity duration-500 ${isTransitioning ? 'opacity-0' : 'opacity-100'}`}
          >
            "{QUOTES[currentQuoteIndex].text}"
          </blockquote>
          <div
            className={`text-sm transition-opacity duration-500 ${isTransitioning ? 'opacity-0' : 'opacity-100'}`}
          >
            — {QUOTES[currentQuoteIndex].author}
          </div>
        </div>
      </div>

      {/* Right side - Auth Form */}
      <div className="flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-6">
          <div className="space-y-2 text-center">
            <h1 className="text-3xl font-bold">{isLogin ? 'Welcome back' : 'Create an account'}</h1>
            <p className="text-muted-foreground">
              {isLogin
                ? 'Enter your credentials to access your account'
                : 'Enter your details to create your account'}
            </p>
          </div>

          <Card className="p-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  type="text"
                  placeholder="Enter your username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  disabled={isLoading}
                />
              </div>

              {!isLogin && (
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    disabled={isLoading}
                  />
                </div>
              )}

              {error && <div className="text-destructive text-sm">{error}</div>}

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? 'Please wait...' : isLogin ? 'Sign In' : 'Create Account'}
              </Button>
            </form>
          </Card>

          <div className="text-center text-sm">
            {isLogin ? (
              <>
                Don't have an account?{' '}
                <button
                  type="button"
                  onClick={() => setIsLogin(false)}
                  className="hover:text-primary underline underline-offset-4"
                >
                  Sign up
                </button>
              </>
            ) : (
              <>
                Already have an account?{' '}
                <button
                  type="button"
                  onClick={() => setIsLogin(true)}
                  className="hover:text-primary underline underline-offset-4"
                >
                  Sign in
                </button>
              </>
            )}
          </div>

          <p className="text-muted-foreground px-8 text-center text-xs">
            By continuing, you agree to our Terms of Service and Privacy Policy.
          </p>
        </div>
      </div>
    </div>
  );
}
