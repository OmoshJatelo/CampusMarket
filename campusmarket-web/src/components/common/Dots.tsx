import { HTMLAttributes } from 'react';
import { clsx } from 'clsx';

interface DotsProps extends HTMLAttributes<HTMLDivElement> {
  count: number;
  activeIndex: number;
  size?: 'sm' | 'md';
}

export function Dots({ count, activeIndex, size = 'md', className, ...props }: DotsProps) {
  if (count <= 1) return null;

  const dotSize = size === 'sm' ? 'h-1.5 w-1.5' : 'h-2 w-2';
  const gap = size === 'sm' ? 'gap-1' : 'gap-1.5';

  return (
    <div
      className={clsx('flex items-center justify-center', gap, className)}
      {...props}
      aria-hidden="true"
    >
      {Array.from({ length: count }).map((_, i) => (
        <span
          key={i}
          className={clsx(
            dotSize,
            'rounded-full transition-colors duration-200',
            i === activeIndex
              ? 'bg-primary-600'
              : 'bg-gray-300 dark:bg-gray-600'
          )}
        />
      ))}
    </div>
  );
}