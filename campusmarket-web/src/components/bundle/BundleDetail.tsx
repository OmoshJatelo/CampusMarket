import { useState } from 'react';
import { clsx } from 'clsx';
import { formatPickupDate, isBundleActive, MoveOutBundle } from '@models/MoveOutBundle';
import { formatPriceCompact } from '@models/Product';
import { Product } from '@models/Product';
import { ProductCard } from '@components/product/ProductCard';
import { Button } from '@components/ui/Button';
import { Badge } from '@components/ui/Badge';
import { Share2, MessageCircle, X } from 'lucide-react';
import { getWhatsAppUrl } from '@models/MoveOutBundle';
import { useShare } from '@hooks/useShare';

interface BundleDetailProps {
  bundle: MoveOutBundle;
  items: Product[];
  isOwner: boolean;
  onClose?: () => void;
  loading?: boolean;
}

export function BundleDetail({
  bundle,
  items,
  isOwner,
  onClose,
  loading = false,
}: BundleDetailProps) {
  const { shareBundleItems } = useShare();
  const active = isBundleActive(bundle);
  const totalPrice = items.reduce((sum, item) => sum + item.price, 0);
  const [showShareModal, setShowShareModal] = useState(false);

  const handleShare = async () => {
    await shareBundleItems(bundle, items);
    setShowShareModal(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap mb-2">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white truncate">
              {bundle.title}
            </h1>
            {!active && <Badge variant="danger">Sale Ended</Badge>}
            {isOwner && !active && <Badge variant="gray">Closed</Badge>}
          </div>

          <div className="flex flex-wrap items-center gap-4 text-sm text-gray-600 dark:text-gray-400 mb-3">
            <span className="flex items-center gap-1">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              {bundle.pickupLocation}
            </span>
            <span className="flex items-center gap-1">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              Pickup: {formatPickupDate(bundle.pickupDate)}
            </span>
            <span className="flex items-center gap-1">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8-4v10M4 7l8 4" />
              </svg>
              {items.length} item{items.length !== 1 ? 's' : ''}
            </span>
          </div>

          <p className={clsx(
            'font-semibold',
            active ? 'text-primary-600 dark:text-primary-400' : 'text-red-600 dark:text-red-400'
          )}>
            {active ? `Total: ${formatPriceCompact(totalPrice)}` : 'Sale has ended'}
          </p>
        </div>

        <div className="flex flex-wrap gap-2 sm:ml-4">
          <Button
            variant="primary"
            size="lg"
            onClick={() => window.open(getWhatsAppUrl(bundle), '_blank')}
            className="flex items-center gap-2"
          >
            <MessageCircle className="h-5 w-5" />
            Contact Seller
          </Button>

          <Button
            variant="outline"
            size="lg"
            onClick={() => setShowShareModal(true)}
            className="flex items-center gap-2"
          >
            <Share2 className="h-5 w-5" />
            Share
          </Button>

          {isOwner && active && (
            <Button
              variant="danger"
              size="lg"
              onClick={onClose}
              disabled={loading}
              className="flex items-center gap-2"
            >
              <X className="h-5 w-5" />
              Close Sale
            </Button>
          )}
        </div>
      </div>

      {bundle.description && (
        <div className="bg-gray-50 dark:bg-gray-800/50 rounded-xl p-4">
          <p className="text-gray-700 dark:text-gray-300">{bundle.description}</p>
        </div>
      )}

      <div>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Items ({items.length})
        </h2>
        {items.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {items.map((item) => (
              <ProductCard key={item.id} product={item} />
            ))}
          </div>
        ) : (
          <p className="text-gray-500 dark:text-gray-400 py-8 text-center">No items in this sale</p>
        )}
      </div>

      {showShareModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" onClick={() => setShowShareModal(false)}>
          <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 max-w-md w-full shadow-xl" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Share this sale</h3>
            <p className="text-gray-600 dark:text-gray-400 mb-6">
              Share via WhatsApp, copy link, or use the system share sheet.
            </p>
            <div className="flex flex-col gap-3">
              <Button
                variant="primary"
                onClick={handleShare}
                className="flex items-center justify-center gap-2"
              >
                <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M17.472 14.382c-.047-.055-.279-.127-.518-.127H4.28c-.966 0-1.886.648-2.09 1.573a13.24 13.24 0 00-.017 3.268c0 .446.105.88.292 1.268.189.38.47.69.821.94.084.06.182.1.29.1.239 0 .445-.076.636-.223.35-.274.742-.858 1.055-1.43.3-.575.436-1.212.436-1.879V7.89c0-.958-.57-1.932-1.412-2.167a4.79 4.79 0 00-.862-.076 4.59 4.59 0 00-.842.034c-.23.07-.435.16-.61.288a2.26 2.26 0 00-.732.917 30.31 30.31 0 00-.058 3.392c0 .61.09 1.206.256 1.787.12.41.33.756.603 1.029.08.08.16.16.24.24.28.28.61.48.98.48.39 0 .697-.14.944-.394.235-.24.407-.54.51-.89a1.5 1.5 0 00-.02-.275 4.55 4.55 0 00-1.442-2.607 4.46 4.46 0 00-.87-.146c-.265-.013-.527.05-.756.164-.228.114-.446.266-.646.435-.122.1-.245.2-.36.31-.23.23-.442.473-.63.723a12.5 12.5 0 00-.02 1.28c0 .487.08.95.222 1.384a2.5 2.5 0 00.824 1.4c.17.23.38.42.61.57.07.04.14.07.21.1.14.07.28.12.43.15.29.06.57.07.858-.032a8.6 8.6 0 001.444-.718c.26-.2.487-.443.672-.71.11-.15.21-.29.29-.44.13-.25.23-.515.29-.78.16-.68.18-1.374.08-2.06-.06-.38-.17-.75-.315-1.105-.145-.35-.33-.676-.542-.976-.265-.37-.57-.705-.902-.99-.33-.29-.69-.53-1.055-.712-.256-.12-.518-.213-.784-.27-.57-.12-1.15.05-1.69.5a4.4 4.4 0 01-1.023 1.99c-.22.25-.42.516-.61.788-.19.27-.37.55-.55.82-.18.27-.34.54-.49.81-.16.27-.31.53-.44.79-.12.26-.23.51-.32.75-.09.25-.16.5-.2.748-.05.248-.07.497-.07.746v1.5h8.5v-1.5c0-.357-.035-.71-.09-1.042a2.5 2.5 0 00-.31-1.05 4.5 4.5 0 00-.775-1.168 6 6 0 00-1.13-1.15ZM12 19.5a3.5 3.5 0 110-7 3.5 3.5 0 010 7Z" />
                </svg>
                Share on WhatsApp
              </Button>
              <Button
                variant="outline"
                onClick={async () => {
                  const text = `Move-out sale: ${bundle.title}\n${items.map(i => `- ${i.title}: ${formatPriceCompact(i.price)}`).join('\n')}\n\n${items.length} items, ${formatPriceCompact(totalPrice)} in total\nPickup: ${bundle.pickupLocation}, ${formatPickupDate(bundle.pickupDate)}\n\nPosted on CampusMarket`;
                  try {
                    await navigator.clipboard.writeText(text);
                    alert('Copied to clipboard!');
                  } catch {
                    alert('Failed to copy');
                  }
                  setShowShareModal(false);
                }}
                className="flex items-center justify-center gap-2"
              >
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                Copy Link
              </Button>
              <Button
                variant="ghost"
                onClick={() => setShowShareModal(false)}
              >
                Cancel
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}