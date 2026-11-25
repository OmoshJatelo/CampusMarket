import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '@hooks/useAuth';
import { useOnboarding } from '@hooks/useOnboarding';
import { Spinner } from '@components/common/Spinner';

export function AuthGate() {
  const { user, loading: authLoading } = useAuth();
  const { done: onboardingDone, loading: onboardingLoading } = useOnboarding();
  const location = useLocation();

  if (authLoading || onboardingLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  // Redirect to login if not authenticated
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // Redirect to onboarding if not completed
  if (!onboardingDone) {
    return <Navigate to="/onboarding" replace />;
  }

  return <Outlet />;
}

export function PublicOnlyGate() {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  // Redirect to home if already authenticated
  if (user) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

export function OnboardingGate() {
  const { done, loading } = useOnboarding();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  if (done) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}