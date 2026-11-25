import { clsx } from 'clsx';
import { formatPriceCompact, isActive, isExpired, daysLeft, getProductTimeAgo, Product } from '@models/Product';
import { getThumbnailUrl } from '@lib/cloudinary/config';
import { Badge } from '@components/ui/Badge';
import { Button } from '@components/ui/Button';
import { Share2, RotateCcw, Trash2, CheckCircle } from 'lucide-react';

interface MyListingRowProps {
  product: Product;
  onMarkSold: (id: string) => void;
  onRenew: (id: string) => void;
  onDelete: (id: string) => void;
  onShare: (product: Product) => void;
  loadingIds?: Set<string>;
}

export function MyListingRow({
  product,
  onMarkSold,
  onRenew,
  onDelete,
  onShare,
  loadingIds = new Set(),
}: MyListingRowProps) {
  const imageUrl = product.imageUris[0];
  const thumbnailUrl = imageUrl ? getThumbnailUrl(imageUrl) : null;
  const isWanted = product.type === 'WANTED';
  const status = product.status;
  const active = isActive(product);
  const expired = isExpired(product);
  const expiringSoon = !expired && daysLeft(product) <= 3;
  const isLoading = loadingIds.has(product.id);

  let statusBadge: { label: string; variant: 'success' | 'warning' | 'danger' | 'gray' } = {
    label: 'Active',
    variant: 'success',
  };

  if (status === 'SOLD') {
    statusBadge = { label: isWanted ? 'Found' : 'Sold', variant: 'gray' };
  } else if (expired) {
    statusBadge = { label: 'Expired', variant: 'danger' };
  } else if (expiringSoon) {
    statusBadge = { label: `Expires in ${daysLeft(product)} day(s)`, variant: 'warning' };
  } else if (active) {
    statusBadge = { label: `${daysLeft(product)} days left`, variant: 'success' };
  }

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-card dark:bg-gray-800 dark:border-gray-700 overflow-hidden">
      <div className="flex flex-col sm:flex-row">
        {/* Thumbnail */}
        <div className="relative sm:w-24 sm:h-24 sm:flex-shrink-0">
          {thumbnailUrl ? (
            <img
              src={thumbnailUrl}
              alt=""
              className="w-full h-full object-cover sm:w-full sm:h-full"
            />
          ) : (
            <div className="w-full h-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
              <svg className="h-8 w-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
          )}
          {isWanted && (
            <Badge variant="danger" className="absolute top-2 left-2 text-xs">
              WANTED
            </Badge>
          )}
          {product.bundleId && (
            <Badge variant="primary" className="absolute top-2 left-2 text-xs">
              BUNDLE
            </Badge>
          )}
        </div>

        {/* Details */}
        <div className="flex-1 p-4 sm:p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-start justify-between gap-2 mb-2">
              <h3 className="font-semibold text-gray-900 dark:text-white flex-1 min-w-0">
                {product.title}
              </h3>
              <Badge variant={statusBadge.variant} className="flex-shrink-0">
                {statusBadge.label}
              </Badge>
            </div>

            <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600 dark:text-gray-400 mb-3">
              <span className={clsx(
                'font-semibold',
                isWanted ? 'text-amber-600 dark:text-amber-400' : 'text-primary-600 dark:text-primary-400'
              )}>
                {isWanted ? `Budget: ${formatPriceCompact(product.price)}` : formatPriceCompact(product.price)}
                {product.negotiable && !isWanted && (
                  <span className="ml-1 text-xs font-normal text-gray-500">(negotiable)</span>
                )}
              </span>
              <span className="flex items-center gap-1">
                <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {product.location}
              </span>
            </div>

            <p className="text-xs text-gray-400 dark:text-gray-500">
              Posted {getProductTimeAgo(product)}
            </p>
          </div>

          {/* Actions */}
          <div className="flex flex-wrap gap-2 pt-3 border-t border-gray-100 dark:border-gray-800">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onShare(product)}
              className="flex items-center gap-1.5"
            >
              <Share2 className="h-4 w-4" />
              Share
            </Button>

            {status !== 'SOLD' && (expired || expiringSoon) && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onRenew(product.id)}
                disabled={isLoading}
                className="flex items-center gap-1.5"
              >
                <RotateCcw className="h-4 w-4" />
                Renew
              </Button>
            )}

            {status !== 'SOLD' && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onMarkSold(product.id)}
                disabled={isLoading}
                className="flex items-center gap-1.5 text-green-600 hover:text-green-700 dark:text-green-400"
              >
                <CheckCircle className="h-4 w-4" />
                {isWanted ? 'Found' : 'Sold'}
              </Button>
            )}

            <Button
              variant="ghost"
              size="sm"
              onClick={() => onDelete(product.id)}
              disabled={isLoading}
              className="flex items-center gap-1.5 text-red-600 hover:text-red-700 dark:text-red-400 ml-auto"
            >
              <Trash2 className="h-4 w-4" />
              Delete
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}