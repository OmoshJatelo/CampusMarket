import { useState, useCallback } from 'react';
import { sharePost, shareBundle } from '@utils/share';
import { Product } from '@models/Product';
import { MoveOutBundle } from '@models/MoveOutBundle';

export function useShare() {
  const [sharing, setSharing] = useState(false);
  const [shareSuccess, setShareSuccess] = useState(false);
  const [shareError, setShareError] = useState<string | null>(null);

  const shareProduct = useCallback(async (product: Product) => {
    setSharing(true);
    setShareError(null);
    setShareSuccess(false);

    try {
      await sharePost(product);
      setShareSuccess(true);
    } catch (err) {
      setShareError(err instanceof Error ? err.message : 'Failed to share');
    } finally {
      setSharing(false);
      // Reset success after 2 seconds
      setTimeout(() => setShareSuccess(false), 2000);
    }
  }, []);

  const shareBundleItems = useCallback(async (bundle: MoveOutBundle, items: Product[]) => {
    setSharing(true);
    setShareError(null);
    setShareSuccess(false);

    try {
      await shareBundle(bundle, items);
      setShareSuccess(true);
    } catch (err) {
      setShareError(err instanceof Error ? err.message : 'Failed to share');
    } finally {
      setSharing(false);
      setTimeout(() => setShareSuccess(false), 2000);
    }
  }, []);

  return {
    shareProduct,
    shareBundleItems,
    sharing,
    shareSuccess,
    shareError,
  };
}