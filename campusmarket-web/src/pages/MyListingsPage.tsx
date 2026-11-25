import { useState } from 'react';
import { Link } from 'react-router-dom';
import { MyListingRow } from '@components/product/MyListingRow';
import { useRealtimeMyListings } from '@hooks/useListings';
import { useAuth } from '@hooks/useAuth';
import { useToast } from '@components/ui/Toast';
import { PageLayout } from '@components/layout/PageLayout';
import { Button } from '@components/ui/Button';
import { EmptyState } from '@components/feed/EmptyState';
import { Plus, Package } from 'lucide-react';
import { Product } from '@models/Product';

export function MyListingsPage() {
  const { user } = useAuth();
  const { toast } = useToast();
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [action, setAction] = useState<'sold' | 'renew' | 'delete' | null>(null);
  const [loadingIds, setLoadingIds] = useState<Set<string>>(new Set());

  const { data: myProducts = [], isLoading } = useRealtimeMyListings(user?.uid);

  const handleAction = (product: Product, actionType: 'sold' | 'renew' | 'delete') => {
    setSelectedProduct(product);
    setAction(actionType);
  };

  const confirmAction = async () => {
    if (!selectedProduct || !action) return;
    const id = selectedProduct.id;
    setLoadingIds((prev) => new Set(prev).add(id));

    try {
      switch (action) {
        case 'sold':
          // This would call the markSold mutation
          break;
        case 'renew':
          // This would call the renew mutation
          break;
        case 'delete':
          // This would call the delete mutation
          break;
      }
      toast({ type: 'success', message: `Listing ${action} successfully` });
    } catch {
      toast({ type: 'error', message: `Failed to ${action} listing` });
    } finally {
      setLoadingIds((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
      setSelectedProduct(null);
      setAction(null);
    }
  };

  if (isLoading) {
    return (
      <PageLayout>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">My Listings</h1>
            <Link to="/listing/new">
              <Button variant="primary" size="sm"><Plus className="h-4 w-4 mr-2" />Post Item</Button>
            </Link>
          </div>
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="animate-pulse bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-4" />
            ))}
          </div>
        </div>
      </PageLayout>
    );
  }

  return (
    <PageLayout>
      <div className="space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">My Listings</h1>
            <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">Swipe right to mark sold, left to delete</p>
          </div>
          <div className="flex gap-2">
            <Link to="/bundles/new">
              <Button variant="outline"><Package className="h-4 w-4 mr-2" />Create Bundle</Button>
            </Link>
            <Link to="/listing/new">
              <Button variant="primary"><Plus className="h-4 w-4 mr-2" />Post Item</Button>
            </Link>
          </div>
        </div>

        {myProducts.length === 0 ? (
          <EmptyState
            title="You haven't posted anything yet"
            message="Create your first listing to start selling or finding items."
            actionLabel="Post an item"
            actionHref="/listing/new"
          />
        ) : (
          <div className="space-y-3">
            {myProducts.map((product) => (
              <MyListingRow
                key={product.id}
                product={product}
                onMarkSold={() => handleAction(product, 'sold')}
                onRenew={() => handleAction(product, 'renew')}
                onDelete={() => handleAction(product, 'delete')}
                onShare={() => {}}
                loadingIds={loadingIds}
              />
            ))}
          </div>
        )}

        {/* Action Confirmation Modal */}
        {selectedProduct && action && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
            <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 max-w-md w-full shadow-xl">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                {action === 'sold' ? `Mark "${selectedProduct.title}" as ${selectedProduct.type === 'WANTED' ? 'found' : 'sold'}?` :
                 action === 'renew' ? `Renew "${selectedProduct.title}" for another 14 days?` :
                 `Delete "${selectedProduct.title}"?`}
              </h3>
              <p className="text-gray-600 dark:text-gray-400 mb-6">
                {action === 'delete' ? 'This cannot be undone.' : 'This action can be undone from the notification.'}
              </p>
              <div className="flex gap-3">
                <Button variant="ghost" onClick={() => { setSelectedProduct(null); setAction(null); }} className="flex-1">
                  Cancel
                </Button>
                <Button
                  variant={action === 'delete' ? 'danger' : 'primary'}
                  onClick={confirmAction}
                  disabled={loadingIds.has(selectedProduct.id)}
                  className="flex-1"
                >
                  {action === 'sold' ? (selectedProduct.type === 'WANTED' ? 'Found' : 'Sold') :
                   action === 'renew' ? 'Renew' : 'Delete'}
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </PageLayout>
  );
}