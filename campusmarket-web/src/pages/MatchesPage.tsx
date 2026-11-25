import { ProductCard } from '@components/product/ProductCard';
import { ListingSkeleton } from '@components/ui/Skeleton';
import { EmptyState } from '@components/feed/EmptyState';
import { useMatchesPage } from '@hooks/useMatches';
import { PageLayout } from '@components/layout/PageLayout';
import { GitMerge } from 'lucide-react';

export function MatchesPage() {
  const { matchedProducts, isLoading } = useMatchesPage();

  if (isLoading) {
    return (
      <PageLayout>
        <div className="space-y-6">
          <div className="flex items-center gap-3">
            <GitMerge className="h-8 w-8 text-primary-600 dark:text-primary-400" />
            <div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Matches</h1>
              <p className="text-gray-500 dark:text-gray-400">Posts that fit what you are selling or looking for.</p>
            </div>
          </div>
          <ListingSkeleton />
        </div>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <div className="space-y-6">
        <div className="flex items-center gap-3">
          <GitMerge className="h-8 w-8 text-primary-600 dark:text-primary-400" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Matches</h1>
            <p className="text-gray-500 dark:text-gray-400">Posts that fit what you are selling or looking for.</p>
          </div>
        </div>

        {matchedProducts.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {matchedProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        ) : (
          <EmptyState
            title="No matches yet"
            message="Post a Wanted request or list an item, and we will tell you when something fits."
            actionLabel="Post a Wanted request"
            actionHref="/listing/new"
          />
        )}
      </div>
    </PageLayout>
  );
}