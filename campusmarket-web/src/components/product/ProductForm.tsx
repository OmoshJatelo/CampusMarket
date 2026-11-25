import { useCallback } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createListingSchema, CreateListingForm } from '@utils/validation';
import { LISTING_CATEGORIES, LISTING_CONDITIONS, LISTING_LOCATIONS } from '@utils/constants';
import { Button } from '@components/ui/Button';
import { Input } from '@components/ui/Input';
import { Textarea } from '@components/ui/Input';
import { Select } from '@components/ui/Select';
import { useImageUpload } from '@hooks/useImageUpload';
import { X, Camera } from 'lucide-react';
import { clsx } from 'clsx';

interface ProductFormProps {
  onSubmit: (data: CreateListingForm) => Promise<void>;
  submitting?: boolean;
  initialData?: Partial<CreateListingForm>;
  imageUrls?: string[];
}

export function ProductForm({ onSubmit, submitting = false, initialData, imageUrls = [] }: ProductFormProps) {
  const { images, previews, uploading, progress, error, addImages, removeImage, upload, canAddMore } = useImageUpload();

  const form = useForm<CreateListingForm>({
    resolver: zodResolver(createListingSchema),
    defaultValues: {
      type: 'SALE',
      title: '',
      category: LISTING_CATEGORIES[0],
      condition: LISTING_CONDITIONS[0],
      price: '',
      negotiable: false,
      location: LISTING_LOCATIONS[0],
      description: '',
      phone: '',
      imageUris: imageUrls,
      ...initialData,
    },
  });

  const { watch, setValue, formState: { errors } } = form;
  const type = watch('type');
  const isWanted = type === 'WANTED';

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length > 0) {
      await addImages(files);
      e.target.value = '';
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      const files = Array.from(e.dataTransfer.files);
      if (files.length > 0) {
        addImages(files);
      }
    },
    [addImages]
  );

  const handleSubmit = async (data: CreateListingForm) => {
    try {
      let finalImageUrls = imageUrls;
      
      // If there are local images to upload, upload them first
      if (images.length > 0) {
        const results = await upload();
        finalImageUrls = [...imageUrls, ...results.map(r => r.url)];
      }
      
      setValue('imageUris', finalImageUrls);
      await onSubmit({ ...data, imageUris: finalImageUrls });
    } catch (err) {
      console.error('Submit error:', err);
    }
  };

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
      {/* Type Toggle */}
      <div>
        <label className="label">Listing Type</label>
        <div className="flex rounded-xl border border-gray-300 dark:border-gray-600 overflow-hidden" role="radiogroup">
          {(['SALE', 'WANTED'] as const).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setValue('type', t)}
              className={clsx(
                'flex-1 py-3 px-4 text-sm font-medium transition-colors',
                type === t
                  ? 'bg-primary-600 text-white'
                  : 'text-gray-600 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-gray-800'
              )}
              role="radio"
              aria-checked={type === t}
            >
              {t === 'SALE' ? 'For Sale' : 'Wanted Request'}
            </button>
          ))}
        </div>
      </div>

      {/* Photos */}
      <div>
        <label className="label flex items-center justify-between">
          Photos
          <span className="text-sm text-gray-500 dark:text-gray-400">{images.length} / 5</span>
        </label>

        <div
          className={clsx(
            'border-2 border-dashed rounded-xl p-4 transition-colors',
            images.length > 0 ? 'border-gray-300 dark:border-gray-600' : 'border-gray-300 dark:border-gray-600 hover:border-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/10'
          )}
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === 'Enter' && document.getElementById('photo-input')?.click()}
        >
          <input
            id="photo-input"
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageUpload}
            className="sr-only"
            aria-label="Upload photos"
            disabled={!canAddMore}
          />

          {images.length > 0 ? (
            <div className="flex gap-2 overflow-x-auto pb-2 -mx-2 px-2">
              {previews.map((preview, index) => (
                <div key={index} className="relative flex-shrink-0">
                  <div className="relative h-24 w-24 rounded-lg overflow-hidden">
                    <img src={preview} alt={`Photo ${index + 1}`} className="h-full w-full object-cover" />
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className="absolute top-1 right-1 p-1 rounded-full bg-red-600 text-white hover:bg-red-700 transition-colors shadow-sm"
                      aria-label={`Remove photo ${index + 1}`}
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
              {canAddMore && (
                <button
                  type="button"
                  onClick={() => document.getElementById('photo-input')?.click()}
                  className="flex h-24 w-24 items-center justify-center rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-600 hover:border-primary-400 transition-colors"
                  aria-label="Add more photos"
                >
                  <Camera className="h-8 w-8 text-gray-400 dark:text-gray-500" />
                </button>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center gap-3 py-8 text-center">
              <Camera className="h-12 w-12 text-gray-400 dark:text-gray-500" />
              <p className="text-gray-600 dark:text-gray-400">Drag & drop photos here, or click to browse</p>
              <p className="text-sm text-gray-500 dark:text-gray-500">Maximum 5 photos (JPEG, PNG, WebP, HEIC)</p>
              <Button variant="outline" onClick={() => document.getElementById('photo-input')?.click()}>
                Add Photos
              </Button>
            </div>
          )}

          {uploading && (
            <div className="mt-4 w-full">
              <div className="h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                <div
                  className="h-full bg-primary-600 transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-sm text-gray-500 dark:text-gray-400 text-center mt-1">
                Uploading... {progress}%
              </p>
            </div>
          )}

          {error && (
            <p className="mt-2 text-sm text-red-600 dark:text-red-400 text-center" role="alert">
              {error}
            </p>
          )}
        </div>
      </div>

      {/* Title */}
      <Controller
        name="title"
        control={form.control}
        render={({ field }) => (
          <Input
            {...field}
            label="Item Name"
            placeholder={isWanted ? "What are you looking for?" : "Item name (e.g., Mini Fridge)"}
            error={errors.title?.message}
            maxLength={60}
            autoComplete="off"
          />
        )}
      />

      {/* Category */}
      <Controller
        name="category"
        control={form.control}
        render={({ field }) => (
          <Select
            {...field}
            label="Category"
            options={LISTING_CATEGORIES.map((c) => ({ value: c, label: c }))}
            placeholder="Select category"
            error={errors.category?.message}
          />
        )}
      />

      {/* Condition (hidden for Wanted) */}
      {!isWanted && (
        <Controller
          name="condition"
          control={form.control}
          render={({ field }) => (
            <Select
              {...field}
              label="Condition"
              options={LISTING_CONDITIONS.map((c) => ({ value: c, label: c }))}
              placeholder="Select condition"
              error={errors.condition?.message}
            />
          )}
        />
      )}

      {/* Price */}
      <Controller
        name="price"
        control={form.control}
        render={({ field }) => (
          <Input
            {...field}
            label={isWanted ? 'Maximum Budget' : 'Price'}
            placeholder={isWanted ? 'e.g., 5000' : 'e.g., 4500'}
            type="number"
            inputMode="numeric"
            prefix="KSh "
            error={errors.price?.message}
          />
        )}
      />

      {/* Negotiable (only for Sale) */}
      {!isWanted && (
        <div className="flex items-center gap-3">
          <input
            type="checkbox"
            id="negotiable"
            {...form.register('negotiable')}
            className="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          />
          <label htmlFor="negotiable" className="text-gray-700 dark:text-gray-300 cursor-pointer">
            Price is negotiable
          </label>
        </div>
      )}

      {/* Location */}
      <Controller
        name="location"
        control={form.control}
        render={({ field }) => (
          <Select
            {...field}
            label="Pickup Location"
            options={LISTING_LOCATIONS.map((l) => ({ value: l, label: l }))}
            placeholder="Select location"
            error={errors.location?.message}
          />
        )}
      />

      {/* Description */}
      <Controller
        name="description"
        control={form.control}
        render={({ field }) => (
          <Textarea
            {...field}
            label="Description (optional)"
            placeholder="Add details about the item..."
            rows={4}
            maxLength={300}
            error={errors.description?.message}
          />
        )}
      />

      {/* Phone */}
      <Controller
        name="phone"
        control={form.control}
        render={({ field }) => (
          <Input
            {...field}
            label="WhatsApp Number"
            placeholder="e.g., 0712 345 678"
            type="tel"
            error={errors.phone?.message}
            helperText="We'll create a WhatsApp link for buyers to contact you"
          />
        )}
      />

      {/* Submit */}
      <Button type="submit" variant="primary" size="lg" fullWidth loading={submitting}>
        {isWanted ? 'Post Request' : 'Post Listing'}
      </Button>
    </form>
  );
}