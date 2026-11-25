import { Link } from 'react-router-dom';
import { clsx } from 'clsx';
import { MoveOutBundle, isBundleActive, formatPickupDate } from '@models/MoveOutBundle';
import { Badge } from '@components/ui/Badge';

interface BundleCardProps {
  bundle: MoveOutBundle;
}

export function BundleCard({ bundle }: BundleCardProps) {
  const active = isBundleActive(bundle);

  return (
    <Link
      to={`/bundles/${bundle.id}`}
      className={clsx(
        'bg-white rounded-2xl border border-gray-200 shadow-card hover:shadow-card-hover transition-all duration-200 overflow-hidden dark:bg-gray-800 dark:border-gray-700',
        !active && 'opacity-60'
      )}
    >
      <div className="p-5">
        <div className="flex items-start justify-between gap-3 mb-3">
          <h3 className="font-semibold text-gray-900 dark:text-white line-clamp-2 flex-1 min-w-0">
            {bundle.title}
          </h3>
          {!active && (
            <Badge variant="danger" className="flex-shrink-0">
              Ended
            </Badge>
          )}
        </div>

        <div className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <span>{bundle.pickupLocation}</span>
          </div>
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span>Pickup: {formatPickupDate(bundle.pickupDate)}</span>
          </div>
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7l8 4" />
            </svg>
            <span>{bundle.itemCount} item{bundle.itemCount !== 1 ? 's' : ''}</span>
          </div>
        </div>

        {bundle.description && (
          <p className="mt-3 text-sm text-gray-500 dark:text-gray-400 line-clamp-2">
            {bundle.description}
          </p>
        )}
      </div>
    </Link>
  );
}