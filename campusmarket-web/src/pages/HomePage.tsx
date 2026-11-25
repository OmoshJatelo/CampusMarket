import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { FeedHeader } from '@components/feed/FeedHeader';
import { ProductCard } from '@components/product/ProductCard';
import { ListingSkeleton } from '@components/ui/Skeleton';
import { EmptyState } from '@components/feed/EmptyState';
import { useRealtimeFeed, useProcessedFeed } from '@hooks/useListings';
import { useBlockedOwnerIds } from '@hooks/useModeration';
import { FeedFilter, defaultFeedFilter, getActiveFilterCount } from '@models/FeedFilter';
import { Button } from '@components/ui/Button';
import { Plus } from 'lucide-react';

export function HomePage() {
  const blockedOwnerIds = useBlockedOwnerIds();
  const [searchQuery, setSearchQuery] = useState('');
  const [filter, setFilter] = useState<FeedFilter>(defaultFeedFilter);
  const [activeCategory, setActiveCategory] = useState<string | null>(null);

  // Use real-time feed
  const { data: allProducts = [], isLoading, error } = useRealtimeFeed();

  // Process feed with search, filter, and blocked users
  const processedProducts = useProcessedFeed(allProducts, filter, searchQuery, blockedOwnerIds);

  const hasSearchOrFilter = searchQuery.trim() || getActiveFilterCount(filter) > 0 || activeCategory;

  const handleCategoryClick = useCallback((category: string) => {
    setActiveCategory((prev) => (prev === category ? null : category));
    setSearchQuery(category);
  }, []);

  const handleClearFilters = useCallback(() => {
    setSearchQuery('');
    setFilter(defaultFeedFilter);
    setActiveCategory(null);
  }, []);

  if (isLoading) {
    return <ListingSkeleton />;
  }

  if (error) {
    return (
      <EmptyState
        title="Could not load listings"
        message="Please check your connection and try again."
        actionLabel="Retry"
        actionOnClick={() => window.location.reload()}
        icon={
          <svg className="h-16 w-16 text-red-300 dark:text-red-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      {/* Feed Header */}
      <FeedHeader
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        filter={filter}
        onFilterChange={setFilter}
        onCategorySelect={handleCategoryClick}
        activeCategory={activeCategory}
      />

      {/* Feed Content */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {processedProducts.length > 0 ? (
          processedProducts.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))
        ) : (
          <EmptyState
            title={hasSearchOrFilter ? 'Nothing matches your search' : 'No listings yet'}
            message={hasSearchOrFilter
              ? 'Try different words, or clear the search and filters.'
              : 'Be the first to post something for sale, or ask for what you need.'}
            actionLabel={hasSearchOrFilter ? 'Clear search and filters' : 'Post an item'}
            actionOnClick={hasSearchOrFilter ? handleClearFilters : undefined}
            actionHref={!hasSearchOrFilter ? '/listing/new' : undefined}
          />
        )}
      </div>

      {/* FAB for mobile */}
      <div className="fixed bottom-6 right-6 lg:hidden z-30">
        <Link to="/listing/new">
          <Button variant="primary" size="lg" className="shadow-xl">
            <Plus className="h-5 w-5" />
          </Button>
        </Link>
      </div>
    </div>
  );
}