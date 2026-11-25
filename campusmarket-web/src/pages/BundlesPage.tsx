import { Link } from 'react-router-dom';
import { BundleCard } from '@components/bundle/BundleCard';
import { ProductCardSkeleton } from '@components/ui/Skeleton';
import { EmptyState } from '@components/feed/EmptyState';
import { useRealtimeBundles } from '@hooks/useBundles';
import { PageLayout } from '@components/layout/PageLayout';
import { Button } from '@components/ui/Button';
import { Package, Plus } from 'lucide-react';

export function BundlesPage() {
  const { data: bundles = [], isLoading } = useRealtimeBundles();

  return (
    <PageLayout>
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
              <Package className="h-8 w-8 text-primary-600 dark:text-primary-400" />
              Move-out Sales
            </h1>
            <p className="text-gray-500 dark:text-gray-400 mt-1">
              Students clearing out their rooms. Collect several items in one trip.
            </p>
          </div>
          <Link to="/bundles/new">
            <Button variant="primary"><Plus className="h-4 w-4 mr-2" />Create Bundle</Button>
          </Link>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <ProductCardSkeleton key={i} />
            ))}
          </div>
        ) : bundles.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {bundles.map((bundle) => (
              <BundleCard key={bundle.id} bundle={bundle} />
            ))}
          </div>
        ) : (
          <EmptyState
            title="No move-out sales right now"
            message="Check back later or create your own move-out bundle."
            actionLabel="Create a move-out sale"
            actionHref="/bundles/new"
          />
        )}
      </div>
    </PageLayout>
  );
}