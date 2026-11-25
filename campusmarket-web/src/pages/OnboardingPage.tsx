import { OnboardingFlow } from '@components/onboarding/OnboardingFlow';
import { useOnboarding } from '@hooks/useOnboarding';
import { PageLayout } from '@components/layout/PageLayout';

export function OnboardingPage() {
  const { completeOnboarding } = useOnboarding();

  return (
    <PageLayout>
      <OnboardingFlow onComplete={completeOnboarding} />
    </PageLayout>
  );
}