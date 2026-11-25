import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { OnboardingStep } from './OnboardingStep';
import { Button } from '@components/ui/Button';
import { Dots } from '@components/common/Dots';
import { ArrowRight, X } from 'lucide-react';

const steps = [
  {
    icon: '🏠',
    title: 'Buy and sell on campus',
    body: 'Find second-hand items from verified students, or post what you want to sell.',
  },
  {
    icon: '🔍',
    title: 'Tell us what you need',
    body: 'Post a Wanted request and we will alert you when a matching item appears.',
  },
  {
    icon: '📦',
    title: 'Clearing out your room?',
    body: 'Bundle your items into a move-out sale and share it on WhatsApp in one tap.',
  },
];

export function OnboardingFlow({ onComplete }: { onComplete: () => void }) {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);

  const handleNext = () => {
    if (currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else {
      onComplete();
      navigate('/');
    }
  };

  const handleSkip = () => {
    onComplete();
    navigate('/');
  };

  const step = steps[currentStep];

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col">
      <div className="flex-1 flex items-center justify-center p-6">
        <div className="w-full max-w-md text-center">
          <OnboardingStep
            icon={step.icon}
            title={step.title}
            body={step.body}
          />
        </div>
      </div>

      <div className="border-t border-gray-200 dark:border-gray-800 p-6">
        <div className="flex items-center justify-between">
          <button
            type="button"
            onClick={handleSkip}
            className="text-sm text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300 flex items-center gap-1"
          >
            <X className="h-4 w-4" />
            Skip
          </button>

          <Dots count={steps.length} activeIndex={currentStep} />

          <Button
            variant="primary"
            size="lg"
            onClick={handleNext}
            className="flex items-center gap-2"
          >
            {currentStep === steps.length - 1 ? 'Get Started' : 'Next'}
            <ArrowRight className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </div>
  );
}