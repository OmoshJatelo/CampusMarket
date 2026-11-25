import { useParams } from 'react-router-dom';
import { BundleDetail } from '@components/bundle/BundleDetail';
import { useRealtimeBundleItems } from '@hooks/useBundles';
import { useCloseBundle } from '@hooks/useBundles';
import { useAuth } from '@hooks/useAuth';
import { MoveOutBundle } from '@models/MoveOutBundle';
import { useQuery } from '@tanstack/react-query';
import { doc, getDoc } from 'firebase/firestore';
import { db } from '@lib/firebase/config';
import { PageLayout } from '@components/layout/PageLayout';
import { useToast } from '@components/ui/Toast';

export function BundleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const { toast } = useToast();
  const { mutate: closeBundle } = useCloseBundle();

  const { data: bundle, isLoading: bundleLoading } = useQuery({
    queryKey: ['bundle', id],
    enabled: !!id,
    queryFn: async () => {
      if (!id) throw new Error('No bundle ID');
      const docRef = doc(db, 'bundles', id);
      const docSnap = await getDoc(docRef);
      if (!docSnap.exists()) throw new Error('Bundle not found');
      return { id: docSnap.id, ...docSnap.data() } as MoveOutBundle;
    },
  });

  const { data: items = [], isLoading: itemsLoading } = useRealtimeBundleItems(id);

  const isOwner = !!user && !!bundle && bundle.ownerId === user.uid;
  const loading = bundleLoading || itemsLoading;

  const handleClose = () => {
    if (!bundle) return;
    closeBundle(
      bundle.id,
      {
        onSuccess: () => {
          toast({ type: 'success', message: 'Bundle closed.' });
        },
        onError: () => {
          toast({ type: 'error', message: 'Failed to close bundle.' });
        },
      }
    );
  };

  if (loading) {
    return (
      <PageLayout>
        <div className="space-y-6 animate-pulse">
          <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
          <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/3" />
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4 h-40" />
            ))}
          </div>
        </div>
      </PageLayout>
    );
  }

  if (!bundle) {
    return (
      <PageLayout>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-2">Bundle not found</h2>
          <p className="text-gray-500 dark:text-gray-400 mb-4">This bundle may have been removed.</p>
        </div>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <BundleDetail
        bundle={bundle}
        items={items}
        isOwner={isOwner}
        onClose={handleClose}
        loading={false}
      />
    </PageLayout>
  );
}