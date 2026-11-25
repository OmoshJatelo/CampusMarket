import { HTMLAttributes, forwardRef } from 'react';
import { clsx } from 'clsx';

interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
  src?: string | null;
  alt?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  fallback?: string;
}

const sizeClasses = {
  xs: 'h-6 w-6 text-xs',
  sm: 'h-8 w-8 text-sm',
  md: 'h-10 w-10 text-base',
  lg: 'h-12 w-12 text-lg',
  xl: 'h-16 w-16 text-xl',
};

export const Avatar = forwardRef<HTMLDivElement, AvatarProps>(
  ({ src, alt, size = 'md', fallback, className, ...props }, ref) => {
    const [imageError, setImageError] = useState(false);

    if (src && !imageError) {
      return (
        <div
          ref={ref}
          className={clsx('relative inline-flex shrink-0 overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800', sizeClasses[size], className)}
          {...props}
        >
          <img
            src={src}
            alt={alt || ''}
            onError={() => setImageError(true)}
            className="h-full w-full object-cover"
            aria-hidden={true}
          />
        </div>
      );
    }

    return (
      <div
        ref={ref}
        className={clsx(
          'inline-flex items-center justify-center font-medium rounded-full bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-300',
          sizeClasses[size],
          className
        )}
        {...props}
      >
        {fallback || alt?.charAt(0).toUpperCase() || '?'}
      </div>
    );
  }
);

Avatar.displayName = 'Avatar';

import { useState } from 'react';