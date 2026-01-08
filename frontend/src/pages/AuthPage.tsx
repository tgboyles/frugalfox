import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';

const QUOTES = [
  { text: "A penny saved is a penny earned.", author: "Benjamin Franklin" },
  { text: "Beware of little expenses; a small leak will sink a great ship.", author: "Benjamin Franklin" },
  { text: "Do not save what is left after spending, but spend what is left after saving.", author: "Warren Buffett" },
  { text: "The habit of saving is itself an education; it fosters every virtue, teaches self-denial, cultivates the sense of order, trains to forethought, and so broadens the mind.", author: "T.T. Munger" },
  { text: "It's not your salary that makes you rich, it's your spending habits.", author: "Charles A. Jaffe" },
  { text: "Too many people spend money they haven't earned, to buy things they don't want, to impress people they don't like.", author: "Will Rogers" },
  { text: "Annual income twenty pounds, annual expenditure nineteen six, result happiness. Annual income twenty pounds, annual expenditure twenty pound ought and six, result misery.", author: "Charles Dickens" },
  { text: "He who buys what he does not need steals from himself.", author: "Swedish Proverb" },
  { text: "The price of anything is the amount of life you exchange for it.", author: "Henry David Thoreau" },
  { text: "Frugality includes all the other virtues.", author: "Cicero" },
  { text: "Wealth consists not in having great possessions, but in having few wants.", author: "Epictetus" },
  { text: "A budget is telling your money where to go instead of wondering where it went.", author: "Dave Ramsey" },
  { text: "Never spend your money before you have earned it.", author: "Thomas Jefferson" },
  { text: "Money is a terrible master but an excellent servant.", author: "P.T. Barnum" },
  { text: "The art is not in making money, but in keeping it.", author: "Proverb" },
  { text: "Economy is half the battle of life; it is not so hard to earn money as to spend it well.", author: "Charles Spurgeon" },
  { text: "Live below your means but within your needs.", author: "Unknown" },
  { text: "Mere parsimony is not economy. Expense, and great expense, may be an essential part of true economy.", author: "Edmund Burke" },
  { text: "By sowing frugality we reap liberty, a golden harvest.", author: "Agesilaus" },
  { text: "The quickest way to double your money is to fold it in half and put it in your back pocket.", author: "Will Rogers" },
];

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [currentQuoteIndex, setCurrentQuoteIndex] = useState(0);
  const [isTransitioning, setIsTransitioning] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const interval = setInterval(() => {
      // Start fade-out
      setIsTransitioning(true);
      
      // Change quote after fade-out completes
      setTimeout(() => {
        setCurrentQuoteIndex((prevIndex) => (prevIndex + 1) % QUOTES.length);
        // Start fade-in
        setIsTransitioning(false);
      }, 500); // Match the transition duration
    }, 8000); // Change quote every 8 seconds

    return () => clearInterval(interval);
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
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
        `Failed to ${isLogin ? 'login' : 'register'}. Please try again.`
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      {/* Left side - Branding */}
      <div className="hidden lg:flex bg-primary text-primary-foreground p-12 flex-col justify-between">
        <div className="space-y-4">
          <img
            src="/fox.png"
            alt="Frugal Fox Logo"
            className="w-24 h-24 rounded-full object-cover bg-white p-2 border-4 border-primary-foreground/20"
          />
          <h1 className="text-3xl font-bold">Frugal Fox</h1>
        </div>
        <div className="space-y-4">
          <blockquote className={`text-lg transition-opacity duration-500 ${isTransitioning ? 'opacity-0' : 'opacity-100'}`}>
            "{QUOTES[currentQuoteIndex].text}"
          </blockquote>
          <div className={`text-sm transition-opacity duration-500 ${isTransitioning ? 'opacity-0' : 'opacity-100'}`}>— {QUOTES[currentQuoteIndex].author}</div>
        </div>
      </div>

      {/* Right side - Auth Form */}
      <div className="flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-6">
          <div className="space-y-2 text-center">
            <h1 className="text-3xl font-bold">
              {isLogin ? 'Welcome back' : 'Create an account'}
            </h1>
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

              {error && (
                <div className="text-sm text-destructive">{error}</div>
              )}

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading
                  ? 'Please wait...'
                  : isLogin
                  ? 'Sign In'
                  : 'Create Account'}
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
                  className="underline underline-offset-4 hover:text-primary"
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
                  className="underline underline-offset-4 hover:text-primary"
                >
                  Sign in
                </button>
              </>
            )}
          </div>

          <p className="text-xs text-center text-muted-foreground px-8">
            By continuing, you agree to our Terms of Service and Privacy Policy.
          </p>
        </div>
      </div>
    </div>
  );
}
