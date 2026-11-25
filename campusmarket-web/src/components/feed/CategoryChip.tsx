import { HTMLAttributes } from 'react';
import { clsx } from 'clsx';

interface CategoryChipProps extends HTMLAttributes<HTMLButtonElement> {
  label: string;
  active: boolean;
  onClick: () => void;
}

export function CategoryChip({ label, active, onClick, className, ...props }: CategoryChipProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={clsx(
        'px-3 py-1.5 rounded-full text-sm font-medium transition-all duration-200 whitespace-nowrap',
        active
          ? 'bg-primary-600 text-white shadow-sm'
          : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700',
        className
      )}
      {...props}
    >
      {label}
    </button>
  );
}