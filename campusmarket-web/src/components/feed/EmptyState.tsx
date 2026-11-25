import { Link } from 'react-router-dom';
import { Package, Plus } from 'lucide-react';
import { Button } from '@components/ui/Button';

interface EmptyStateProps {
  title: string;
  message: string;
  actionLabel?: string;
  actionHref?: string;
  actionOnClick?: () => void;
  icon?: React.ReactNode;
}

export function EmptyState({
  title,
  message,
  actionLabel,
  actionHref,
  actionOnClick,
  icon,
}: EmptyStateProps) {
  const DefaultIcon = ({ className }: { className?: string }) => (
    <Package className={clsx('h-16 w-16 text-primary-300 dark:text-primary-700', className)} />
  );

  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
      <div className="mb-6">
        {icon || <DefaultIcon />}
      </div>
      <h3 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">{title}</h3>
      <p className="text-gray-500 dark:text-gray-400 max-w-sm mb-6">{message}</p>
      {actionLabel && actionHref && (
        <Link to={actionHref}>
          <Button variant="primary" size="lg" className="w-full sm:w-auto">
            <Plus className="h-4 w-4 mr-2" />
            {actionLabel}
          </Button>
        </Link>
      )}
      {actionLabel && actionOnClick && (
        <Button variant="primary" size="lg" onClick={actionOnClick} className="w-full sm:w-auto">
          <Plus className="h-4 w-4 mr-2" />
          {actionLabel}
        </Button>
      )}
    </div>
  );
}

import { clsx } from 'clsx';