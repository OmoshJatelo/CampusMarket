import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { authSchema, AuthForm } from '@utils/validation';
import { Button } from '@components/ui/Button';
import { Input } from '@components/ui/Input';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { clsx } from 'clsx';

interface AuthFormProps {
  mode: 'login' | 'signup';
  onSubmit: (data: AuthForm) => Promise<void>;
  onModeChange: (mode: 'login' | 'signup') => void;
  submitting?: boolean;
  error?: string;
}

export function AuthFormComponent({
  mode,
  onSubmit,
  onModeChange,
  submitting = false,
  error,
}: AuthFormProps) {
  const [showPassword, setShowPassword] = useState(false);

  const form = useForm<AuthForm>({
    resolver: zodResolver(authSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const handleSubmit = (data: AuthForm) => {
    onSubmit(data);
  };

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-5">
      {error && (
        <div className="p-3 rounded-lg bg-red-50 text-red-700 dark:bg-red-900/30 dark:text-red-400 text-sm" role="alert">
          {error}
        </div>
      )}

      {/* Mode Toggle */}
      <div className="flex rounded-xl border border-gray-300 dark:border-gray-600 overflow-hidden" role="radiogroup">
        {(['login', 'signup'] as const).map((m) => (
          <button
            key={m}
            type="button"
            onClick={() => onModeChange(m)}
            className={clsx(
              'flex-1 py-3 px-4 text-sm font-medium transition-colors',
              mode === m
                ? 'bg-primary-600 text-white'
                : 'text-gray-600 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800'
            )}
            role="radio"
            aria-checked={mode === m}
          >
            {m === 'login' ? 'Log in' : 'Sign up'}
          </button>
        ))}
      </div>

      {/* Email */}
      <Controller
        name="email"
        control={form.control}
        render={({ field }) => {
          const error = form.formState.errors.email?.message;
          return (
            <Input
              {...field}
              type="email"
              label="Student Email"
              placeholder="you@university.edu"
              autoComplete="email"
              error={error}
              disabled={submitting}
            />
          );
        }}
      />

      {/* Password */}
      <Controller
        name="password"
        control={form.control}
        render={({ field }) => {
          const error = form.formState.errors.password?.message;
          return (
            <div className="relative">
              <Input
                {...field}
                type={showPassword ? 'text' : 'password'}
                label="Password"
                placeholder="6+ characters"
                autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                error={error}
                disabled={submitting}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-[38px] text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>
          );
        }}
      />

      {/* Submit */}
      <Button type="submit" variant="primary" size="lg" fullWidth loading={submitting}>
        {submitting ? (
          <>
            <Loader2 className="h-5 w-5 animate-spin" />
            {mode === 'login' ? 'Signing in...' : 'Creating account...'}
          </>
        ) : (
          mode === 'login' ? 'Log in' : 'Sign up'
        )}
      </Button>

      {/* Forgot Password */}
      {mode === 'login' && (
        <p className="text-center">
          <a href="/forgot-password" className="text-sm text-primary-600 hover:underline dark:text-primary-400">
            Forgot password?
          </a>
        </p>
      )}
    </form>
  );
}

import { Controller } from 'react-hook-form';