import { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Product } from '@models/Product';
import { useQuery } from '@tanstack/react-query';
import { doc, getDoc } from 'firebase/firestore';
import { db } from '@lib/firebase/config';
import { ImageGallery } from '@components/product/ImageGallery';
import { Button } from '@components/ui/Button';
import { Badge } from '@components/ui/Badge';
import { ReportDialog } from '@components/moderation/ReportDialog';
import { BlockDialog } from '@components/moderation/BlockDialog';
import { useShare } from '@hooks/useShare';
import { useAuth } from '@hooks/useAuth';
import { formatPriceCompact, getWhatsAppUrl, getCallUrl, isWanted, isActive, daysLeft, getProductTimeAgo } from '@models/Product';
import { Share2, MessageCircle, Phone, Flag, UserMinus } from 'lucide-react';
import { clsx } from 'clsx';

export function ListingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { shareProduct } = useShare();
  const [showReport, setShowReport] = useState(false);
  const [showBlock, setShowBlock] = useState(false);
  const [galleryIndex, setGalleryIndex] = useState(0);

  const { data: product, isLoading, error } = useQuery({
    queryKey: ['product', id],
    enabled: !!id,
    queryFn: async () => {
      if (!id) throw new Error('No product ID');
      const docRef = doc(db, 'listings', id);
      const docSnap = await getDoc(docRef);
      if (!docSnap.exists()) throw new Error('Product not found');
      return { id: docSnap.id, ...docSnap.data() } as Product;
    },
  });

  const canModerate = user && product && product.ownerId !== user.uid;

  const handleContact = () => {
    if (product) window.open(getWhatsAppUrl(product), '_blank');
  };

  const handleCall = () => {
    if (product) window.location.href = getCallUrl(product);
  };

  const handleShare = async () => {
    if (product) await shareProduct(product);
  };

  if (isLoading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="aspect-[4/3] bg-gray-200 dark:bg-gray-700 rounded-2xl" />
        <div className="space-y-4 px-2">
          <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-3/4" />
          <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
          <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-full" />
          <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-2/3" />
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="text-center py-12">
        <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Listing not found</h2>
        <p className="text-gray-500 dark:text-gray-400 mb-4">This listing may have been removed or doesn't exist.</p>
        <Link to="/" className="text-primary-600 hover:underline">Back to home</Link>
      </div>
    );
  }

  const isOwner = user && product.ownerId === user.uid;
  const wanted = isWanted(product);
  const active = isActive(product);

  return (
    <div className="space-y-6">
      <div className="relative">
        <ImageGallery
          images={product.imageUris}
          initialIndex={galleryIndex}
        />

        {product.imageUris.length > 1 && (
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
            {product.imageUris.map((_, i) => (
              <button
                key={i}
                type="button"
                onClick={() => setGalleryIndex(i)}
                className={clsx(
                  'h-2 w-2 rounded-full transition-all',
                  i === galleryIndex ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/75'
                )}
                aria-label={`View image ${i + 1}`}
              />
            ))}
          </div>
        )}

        {wanted && (
          <Badge variant="danger" className="absolute top-4 left-4">
            WANTED
          </Badge>
        )}
      </div>

      <div className="space-y-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{product.title}</h1>
            <p className="text-gray-500 dark:text-gray-400 mt-1">
              Posted {getProductTimeAgo(product)} • {product.location}
            </p>
          </div>
          {isOwner && (
            <Link to={`/my-listings`} className="text-sm text-primary-600 hover:underline">
              Manage listing
            </Link>
          )}
        </div>

        <div className={clsx('text-3xl font-bold', wanted ? 'text-amber-600 dark:text-amber-400' : 'text-primary-600 dark:text-primary-400')}>
          {wanted ? `Budget: ${formatPriceCompact(product.price)}` : formatPriceCompact(product.price)}
          {product.negotiable && !wanted && (
            <span className="ml-2 text-lg font-normal text-gray-500 dark:text-gray-400">(negotiable)</span>
          )}
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5 space-y-3">
          <div className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
            <span className="flex items-center gap-1">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
              {product.location}
            </span>
            <span className="flex items-center gap-1">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8-4v10M4 7l8 4" /></svg>
              Category: {product.category}
            </span>
          </div>

          {!wanted && product.condition && (
            <div className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
              <span className="flex items-center gap-1">
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
                Condition: {product.condition}
              </span>
            </div>
          )}

          <div className="flex items-center gap-3 text-sm text-gray-600 dark:text-gray-400">
            <Badge variant={active ? 'success' : 'danger'}>
              {active ? `${daysLeft(product)} days left` : 'Expired'}
            </Badge>
          </div>

          {product.description && (
            <p className="text-gray-700 dark:text-gray-300 mt-2">{product.description}</p>
          )}
        </div>

        <div className="flex gap-3">
          <Button
            variant="primary"
            size="lg"
            onClick={handleContact}
            className="flex-1 flex items-center justify-center gap-2"
          >
            <MessageCircle className="h-5 w-5" />
            {wanted ? 'I have this item' : 'Chat on WhatsApp'}
          </Button>
          <Button
            variant="outline"
            size="lg"
            onClick={handleCall}
            className="flex items-center justify-center gap-2"
          >
            <Phone className="h-5 w-5" />
            Call
          </Button>
        </div>

        <div className="flex gap-2">
          <Button variant="outline" onClick={handleShare} className="flex-1 flex items-center justify-center gap-2">
            <Share2 className="h-5 w-5" />
            Share
          </Button>
          {canModerate && (
            <>
              <Button variant="outline" onClick={() => setShowReport(true)} className="flex-1 flex items-center justify-center gap-2">
                <Flag className="h-5 w-5" />
                Report
              </Button>
              <Button variant="outline" onClick={() => setShowBlock(true)} className="flex-1 flex items-center justify-center gap-2">
                <UserMinus className="h-5 w-5" />
                Block
              </Button>
            </>
          )}
        </div>
      </div>

      <ReportDialog
        isOpen={showReport}
        onClose={() => setShowReport(false)}
        listingId={product.id}
        sellerId={product.ownerId}
        listingTitle={product.title}
      />

      <BlockDialog
        isOpen={showBlock}
        onClose={() => setShowBlock(false)}
        sellerId={product.ownerId}
        sellerLabel={product.title}
      />
    </div>
  );
}