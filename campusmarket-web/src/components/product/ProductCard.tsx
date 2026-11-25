import { Link } from 'react-router-dom';
import { clsx } from 'clsx';
import { formatPriceCompact, getProductTimeAgo } from '@models/Product';
import { getThumbnailUrl } from '@lib/cloudinary/config';
import { Badge } from '@components/ui/Badge';
import { Product } from '@models/Product';

interface ProductCardProps {
  product: Product;
  priority?: boolean;
}

export function ProductCard({ product, priority = false }: ProductCardProps) {
  const imageUrl = product.imageUris[0];
  const thumbnailUrl = imageUrl ? getThumbnailUrl(imageUrl) : null;
  const isWanted = product.type === 'WANTED';
  const isBundled = !!product.bundleId;

  return (
    <Link
      to={`/listing/${product.id}`}
      className="group bg-white rounded-2xl border border-gray-200 shadow-card hover:shadow-card-hover transition-all duration-200 overflow-hidden dark:bg-gray-800 dark:border-gray-700"
      aria-label={`View ${product.title}`}
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-gray-100 dark:bg-gray-700">
        {thumbnailUrl ? (
          <img
            src={thumbnailUrl}
            alt=""
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            loading={priority ? 'eager' : 'lazy'}
            width="400"
            height="300"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-300 dark:text-gray-600">
            <svg className="h-16 w-16" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}

        {/* Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-1.5">
          {isWanted && (
            <Badge variant="danger" className="text-xs px-2 py-0.5">
              WANTED
            </Badge>
          )}
          {isBundled && !isWanted && (
            <Badge variant="primary" className="text-xs px-2 py-0.5">
              MOVE-OUT SALE
            </Badge>
          )}
        </div>
      </div>

      <div className="p-4 space-y-2">
        <h3 className="font-semibold text-gray-900 dark:text-white line-clamp-2 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
          {product.title}
        </h3>

        <div className="flex items-center justify-between">
          <span className={clsx(
            'font-semibold',
            isWanted ? 'text-amber-600 dark:text-amber-400' : 'text-primary-600 dark:text-primary-400'
          )}>
            {isWanted ? `Budget: ${formatPriceCompact(product.price)}` : formatPriceCompact(product.price)}
            {product.negotiable && !isWanted && (
              <span className="ml-1 text-xs text-gray-500 dark:text-gray-400 font-normal">(negotiable)</span>
            )}
          </span>

          <span className="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-1">
            <svg className="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            {product.location}
          </span>
        </div>

        <p className="text-xs text-gray-400 dark:text-gray-500">{getProductTimeAgo(product)}</p>
      </div>
    </Link>
  );
}