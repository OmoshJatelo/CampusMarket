import { Mail } from 'lucide-react';
import { Button } from '@components/ui/Button';

interface VerificationNoticeProps {
  email: string;
  onResend: () => void;
  onBack: () => void;
}

export function VerificationNotice({ email, onResend, onBack }: VerificationNoticeProps) {
  return (
    <div className="text-center space-y-6">
      <div className="mx-auto h-20 w-20 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
        <Mail className="h-10 w-10 text-primary-600 dark:text-primary-400" />
      </div>

      <div className="space-y-2">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Check your email</h2>
        <p className="text-gray-600 dark:text-gray-400">
          We sent a verification link to <strong>{email}</strong>. Open it, then come back and log in.
        </p>
      </div>

      <div className="space-y-4">
        <Button variant="primary" size="lg" fullWidth onClick={onBack}>
          I've verified my email, log me in
        </Button>

        <Button variant="outline" size="lg" fullWidth onClick={onResend}>
          Resend verification email
        </Button>

        <p className="text-sm text-gray-500 dark:text-gray-400">
          Didn't receive it? Check your spam folder, or make sure you entered the correct email address.
        </p>
      </div>
    </div>
  );
}