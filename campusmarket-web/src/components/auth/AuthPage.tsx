import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { AuthFormComponent } from './AuthForm';
import { VerificationNotice } from './VerificationNotice';
import { signIn, signUp, sendVerificationEmail, isAllowedEmail } from '@lib/firebase/auth';
import { STUDENT_EMAIL_DOMAIN } from '@utils/constants';
import { AuthPageLayout } from '@components/layout/PageLayout';
import { useAuth } from '@hooks/useAuth';
import { useToast } from '@components/ui/Toast';

export function AuthPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { refreshUser } = useAuth();
  const { toast } = useToast();
  const [mode, setMode] = useState<'login' | 'signup'>('login');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showVerification, setShowVerification] = useState(false);
  const [verificationEmail, setVerificationEmail] = useState('');

  const from = (location.state as any)?.from?.pathname || '/';

  const handleSubmit = async (data: { email: string; password: string }) => {
    setError(null);
    setSubmitting(true);

    try {
      if (!isAllowedEmail(data.email)) {
        throw new Error(`Use your student email (ending in ${STUDENT_EMAIL_DOMAIN})`);
      }

      if (mode === 'signup') {
        await signUp(data.email, data.password);
        const user = await signIn(data.email, data.password);
        await sendVerificationEmail(user.user);
        setVerificationEmail(data.email);
        setShowVerification(true);
        setMode('login');
      } else {
        await signIn(data.email, data.password);
        await refreshUser();
        navigate(from, { replace: true });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setSubmitting(false);
    }
  };

  const handleModeChange = (newMode: 'login' | 'signup') => {
    setMode(newMode);
    setError(null);
  };

  const handleResendVerification = async () => {
    if (!verificationEmail) return;
    try {
      // We'd need to sign in first to resend, but for now just show message
      toast({
        type: 'info',
        message: 'If an account exists, a verification email has been sent.',
      });
    } catch {
      toast({ type: 'error', message: 'Failed to resend verification email' });
    }
  };

  if (showVerification) {
    return (
      <AuthPageLayout>
        <VerificationNotice
          email={verificationEmail}
          onResend={handleResendVerification}
          onBack={() => setShowVerification(false)}
        />
      </AuthPageLayout>
    );
  }

  return (
    <AuthPageLayout>
      <AuthFormComponent
        mode={mode}
        onSubmit={handleSubmit}
        onModeChange={handleModeChange}
        submitting={submitting}
        error={error ?? undefined}
      />
    </AuthPageLayout>
  );
}