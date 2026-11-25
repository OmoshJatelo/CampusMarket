import { HTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';

interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'text' | 'circular' | 'rectangular';
  width?: string | number;
  height?: string | number;
  lines?: number;
}

export const Skeleton = forwardRef<HTMLDivElement, SkeletonProps>(
  ({ variant = 'text', width, height, lines, className, ...props }, ref) => {
    const baseStyles = 'animate-pulse bg-gray-200 rounded dark:bg-gray-700';

    if (variant === 'circular') {
      return (
        <div
          ref={ref}
          className={clsx(baseStyles, 'rounded-full', className)}
          style={{ width: width || '40px', height: height || '40px', ...props.style }}
          {...props}
        />
      );
    }

    if (variant === 'rectangular') {
      return (
        <div
          ref={ref}
          className={clsx(baseStyles, 'rounded-lg', className)}
          style={{ width: width || '100%', height: height || '120px', ...props.style }}
          {...props}
        />
      );
    }

    // Text variant with multiple lines
    if (lines && lines > 1) {
      return (
        <div ref={ref} className={clsx('space-y-2', className)} {...props}>
          {Array.from({ length: lines }).map((_, i) => (
            <div
              key={i}
              className={clsx(
                baseStyles,
                'h-4 rounded',
                i === lines - 1 ? 'w-3/4' : 'w-full'
              )}
              style={{ ...props.style }}
            />
          ))}
        </div>
      );
    }

    return (
      <div
        ref={ref}
        className={clsx(baseStyles, 'h-4 rounded', className)}
        style={{ width: width || '100%', ...props.style }}
        {...props}
      />
    );
  }
);

Skeleton.displayName = 'Skeleton';

export function ProductCardSkeleton() {
  return (
    <div className="group bg-white rounded-2xl border border-gray-200 shadow-card dark:bg-gray-800 dark:border-gray-700 overflow-hidden">
      <Skeleton variant="rectangular" className="aspect-[4/3] w-full" />
      <div className="p-4 space-y-3">
        <Skeleton lines={2} className="w-full" />
        <Skeleton width="40%" />
        <Skeleton width="60%" />
      </div>
    </div>
  );
}

export function ListingSkeleton() {
  return (
    <div className="space-y-4">
      {Array.from({ length: 6 }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  );
}

export function PageSkeleton() {
  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <Skeleton width="30%" height="32px" />
          <Skeleton width="50%" height="16px" />
        </div>
        <Skeleton variant="circular" width="48px" height="48px" />
      </div>
      <Skeleton variant="rectangular" height="56px" />
      <div className="flex gap-2 overflow-x-auto pb-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton variant="rectangular" width="100px" height="36px" key={i} className="flex-shrink-0" />
        ))}
      </div>
      <ListingSkeleton />
    </div>
  );
}