import { useAvailableBundleItems, useCreateBundle } from '@hooks/useBundles';
import { BundleForm } from '@components/bundle/BundleForm';
import { PageLayout } from '@components/layout/PageLayout';
import { Link } from 'react-router-dom';
import { ArrowLeft, Package, Plus } from 'lucide-react';
import { CreateBundleForm } from '@utils/validation';
import { useAuth } from '@hooks/useAuth';

export function CreateBundlePage() {
  const { user } = useAuth();
  const { data: availableItems = [], isLoading } = useAvailableBundleItems(user?.uid);
  const { mutate: createBundle, isPending } = useCreateBundle();

  const handleSubmit = async (data: CreateBundleForm) => {
    const bundle = {
      title: data.title,
      description: data.description || '',
      pickupLocation: data.pickupLocation,
      phone: data.phone,
      pickupDate: data.pickupDate,
      itemCount: data.itemIds.length,
      ownerId: user?.uid || '',
      createdAt: Date.now(),
    };
    await createBundle({ bundle, listingIds: data.itemIds });
  };

  return (
    <PageLayout>
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <Link to="/bundles" className="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800">
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex-1 text-center flex items-center gap-2">
            <Package className="h-7 w-7 text-primary-600 dark:text-primary-400" />
            New Move-out Bundle
          </h1>
          <div className="w-10" />
        </div>

        {isLoading ? (
          <div className="space-y-6 animate-pulse">
            <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded w-1/2" />
            <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded w-1/3" />
            <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded" />
            <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded" />
          </div>
        ) : availableItems.length === 0 ? (
          <div className="text-center py-12">
            <Package className="h-16 w-16 mx-auto text-gray-300 dark:text-gray-600 mb-4" />
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">No items available</h3>
            <p className="text-gray-500 dark:text-gray-400 mb-6">
              You have no unbundled items for sale. Post items first, then bundle them.
            </p>
            <Link to="/listing/new">
              <button className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium text-white bg-primary-600 rounded-xl hover:bg-primary-700">
                <Plus className="h-4 w-4" />
                Post an Item
              </button>
            </Link>
          </div>
        ) : (
          <BundleForm
            availableItems={availableItems}
            onSubmit={handleSubmit}
            submitting={isPending}
          />
        )}
      </div>
    </PageLayout>
  );
}