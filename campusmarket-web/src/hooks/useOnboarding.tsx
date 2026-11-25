import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { isOnboardingDone, setOnboardingDone } from '@utils/storage';

interface OnboardingContextType {
  done: boolean;
  loading: boolean;
  completeOnboarding: () => void;
  skipOnboarding: () => void;
}

const OnboardingContext = createContext<OnboardingContextType | undefined>(undefined);

export function OnboardingProvider({ children }: { children: ReactNode }) {
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkOnboarding = async () => {
      const done = isOnboardingDone();
      setDone(done);
      setLoading(false);
    };
    checkOnboarding();
  }, []);

  const completeOnboarding = () => {
    setOnboardingDone();
    setDone(true);
  };

  const skipOnboarding = () => {
    setOnboardingDone();
    setDone(true);
  };

  return (
    <OnboardingContext.Provider value={{ done, loading, completeOnboarding, skipOnboarding }}>
      {children}
    </OnboardingContext.Provider>
  );
}

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }
  return context;
}