interface OnboardingStepProps {
  icon: string;
  title: string;
  body: string;
}

export function OnboardingStep({ icon, title, body }: OnboardingStepProps) {
  return (
    <div className="animate-fade-in">
      <div className="mx-auto h-24 w-24 rounded-2xl bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center mb-8 text-5xl">
        {icon}
      </div>

      <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">{title}</h2>

      <p className="text-gray-600 dark:text-gray-400 text-lg leading-relaxed">{body}</p>
    </div>
  );
}