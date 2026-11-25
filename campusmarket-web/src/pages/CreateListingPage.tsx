import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProductForm } from '@components/product/ProductForm';
import { CreateListingForm } from '@utils/validation';
import { useCreateListing } from '@hooks/useListings';
import { useAuth } from '@hooks/useAuth';
import { useToast } from '@components/ui/Toast';
import { PageLayout } from '@components/layout/PageLayout';
import { Button } from '@components/ui/Button';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import { LISTING_DAYS, DAY_MS } from '@models/Product';

export function CreateListingPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { toast } = useToast();
  const { mutate: createListing, isPending } = useCreateListing();
  const [uploading, setUploading] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [formData, setFormData] = useState<CreateListingForm | null>(null);

  const handleFormSubmit = async (data: CreateListingForm) => {
    setFormData(data);
    if (data.imageUris.length > 0) {
      setShowConfirm(true);
    } else {
      await submitListing(data);
    }
  };

  const handleConfirmSubmit = async () => {
    if (!formData) return;
    await submitListing(formData);
    setShowConfirm(false);
    setFormData(null);
  };

  const submitListing = async (data: CreateListingForm) => {
    if (!user) return;

    try {
      setUploading(true);
      const now = Date.now();
      const productData = {
        ...data,
        ownerId: user.uid,
        price: parseInt(data.price.replace(/[^0-9]/g, ''), 10),
        imageUris: data.imageUris,
        description: data.description || '',
        condition: data.condition || 'Good',
        createdAt: now,
        expiresAt: now + LISTING_DAYS * DAY_MS,
        status: 'ACTIVE' as const,
      };

      await createListing(productData);
      toast({ type: 'success', message: 'Listing posted successfully!' });
      navigate('/');
    } catch (err) {
      toast({ type: 'error', message: err instanceof Error ? err.message : 'Failed to post listing' });
    } finally {
      setUploading(false);
    }
  };

  return (
    <PageLayout>
      <div className="max-w-2xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <Link to="/" className="p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:hover:bg-gray-800">
            <ArrowLeft className="h-6 w-6" />
          </Link>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex-1 text-center">
            {formData?.type === 'WANTED' ? 'Request an Item' : 'Sell an Item'}
          </h1>
          <div className="w-10" />
        </div>

        <ProductForm
          onSubmit={handleFormSubmit}
          submitting={isPending || uploading}
        />

        {/* Confirm Modal */}
        {showConfirm && formData && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
            <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 max-w-md w-full shadow-xl">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Confirm Details</h3>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Type</span>
                  <span className="font-medium">{formData.type === 'WANTED' ? 'Wanted Request' : 'For Sale'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Item</span>
                  <span className="font-medium truncate max-w-[200px]">{formData.title}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">{formData.type === 'WANTED' ? 'Budget' : 'Price'}</span>
                  <span className="font-medium">{formData.price}{formData.negotiable && formData.type === 'SALE' ? ' (negotiable)' : ''}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Category</span>
                  <span className="font-medium truncate max-w-[200px]">{formData.category}</span>
                </div>
                {!formData.type.includes('WANTED') && formData.condition && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Condition</span>
                    <span className="font-medium">{formData.condition}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-gray-500">Location</span>
                  <span className="font-medium truncate max-w-[200px]">{formData.location}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">WhatsApp</span>
                  <span className="font-medium">{formData.phone}</span>
                </div>
                {formData.imageUris.length > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-500">Photos</span>
                    <span className="font-medium">{formData.imageUris.length}</span>
                  </div>
                )}
              </div>
              <div className="flex gap-3 mt-6">
                <Button variant="ghost" onClick={() => setShowConfirm(false)} className="flex-1">
                  Edit
                </Button>
                <Button variant="primary" onClick={handleConfirmSubmit} className="flex-1" loading={isPending || uploading}>
                  Post
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </PageLayout>
  );
}