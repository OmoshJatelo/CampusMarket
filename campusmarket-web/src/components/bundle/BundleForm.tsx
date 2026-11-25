import { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createBundleSchema, CreateBundleForm } from '@utils/validation';
import { LISTING_LOCATIONS } from '@utils/constants';
import { Button } from '@components/ui/Button';
import { Input } from '@components/ui/Input';
import { Textarea } from '@components/ui/Input';
import { Select } from '@components/ui/Select';
import { Product } from '@models/Product';
import { Calendar, Package, X, Check } from 'lucide-react';
import { clsx } from 'clsx';
import { Badge } from '@components/ui/Badge';

interface BundleFormProps {
  availableItems: Product[];
  onSubmit: (data: CreateBundleForm) => Promise<void>;
  submitting?: boolean;
}

export function BundleForm({ availableItems, onSubmit, submitting = false }: BundleFormProps) {
  const [selectedItems, setSelectedItems] = useState<string[]>([]);
  const [showItemPicker, setShowItemPicker] = useState(false);
  const [pickupDate, setPickupDate] = useState<Date | null>(null);

  const form = useForm<CreateBundleForm>({
    resolver: zodResolver(createBundleSchema),
    defaultValues: {
      title: '',
      description: '',
      pickupLocation: LISTING_LOCATIONS[0],
      phone: '',
      pickupDate: 0,
      itemIds: [],
    },
  });

  const { setValue, formState: { errors } } = form;

  useEffect(() => {
    setValue('itemIds', selectedItems);
  }, [selectedItems, setValue]);

  useEffect(() => {
    if (pickupDate) {
      const utcMidnight = new Date(Date.UTC(pickupDate.getFullYear(), pickupDate.getMonth(), pickupDate.getDate()));
      setValue('pickupDate', utcMidnight.getTime());
    }
  }, [pickupDate, setValue]);

  const handleItemToggle = (itemId: string) => {
    setSelectedItems((prev) =>
      prev.includes(itemId) ? prev.filter((id) => id !== itemId) : [...prev, itemId]
    );
  };

  const handleSubmit = (data: CreateBundleForm) => {
    onSubmit(data);
  };

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      timeZone: 'UTC',
    });
  };

  return (
    <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
      <Controller
        name="title"
        control={form.control}
        render={({ field }) => (
          <Input
            {...field}
            label="Bundle Name"
            placeholder="e.g., Hall 6 Room Clearance"
            error={errors.title?.message}
            maxLength={60}
          />
        )}
      />

      <Controller
        name="pickupLocation"
        control={form.control}
        render={({ field }) => (
          <Select
            {...field}
            label="Pickup Location"
            options={LISTING_LOCATIONS.map((l) => ({ value: l, label: l }))}
            placeholder="Select location"
            error={errors.pickupLocation?.message}
          />
        )}
      />

      <div>
        <label className="label">Pickup Date</label>
        <div className="relative">
          <input
            type="date"
            value={pickupDate ? pickupDate.toISOString().split('T')[0] : ''}
            onChange={(e) => setPickupDate(e.target.value ? new Date(e.target.value) : null)}
            min={new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().split('T')[0]}
            className={clsx(
              'w-full pl-12 pr-4 py-3 rounded-xl border bg-white text-gray-900 focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all duration-200 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600',
              errors.pickupDate && 'border-red-500 focus:border-red-500 focus:ring-red-500/20'
            )}
          />
          <Calendar className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" aria-hidden="true" />
        </div>
        {pickupDate && (
          <p className="mt-1.5 text-sm text-gray-500 dark:text-gray-400">
            Pickup date: {formatDate(pickupDate)}
          </p>
        )}
        {errors.pickupDate && (
          <p className="mt-1.5 text-sm text-red-600 dark:text-red-400" role="alert">
            {errors.pickupDate.message}
          </p>
        )}
      </div>

      <div>
        <label className="label flex items-center justify-between">
          Choose Items
          <span className="text-sm text-gray-500 dark:text-gray-400">{selectedItems.length} selected</span>
        </label>

        <Button
          type="button"
          variant="outline"
          onClick={() => setShowItemPicker(true)}
          className="w-full justify-start"
          disabled={availableItems.length === 0}
        >
          <Package className="h-4 w-4 mr-2" />
          {selectedItems.length > 0
            ? `${selectedItems.length} item${selectedItems.length !== 1 ? 's' : ''} selected`
            : 'Choose items'}
        </Button>

        {selectedItems.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1.5">
            {selectedItems.map((itemId) => {
              const item = availableItems.find((i) => i.id === itemId);
              if (!item) return null;
              return (
                <Badge
                  key={itemId}
                  variant="primary"
                  className="gap-1"
                >
                  {item.title}
                  <X className="h-3 w-3 cursor-pointer" onClick={(e) => { e.stopPropagation(); handleItemToggle(itemId); }} />
                </Badge>
              );
            })}
          </div>
        )}

        {errors.itemIds && (
          <p className="mt-1.5 text-sm text-red-600 dark:text-red-400" role="alert">
            {errors.itemIds.message}
          </p>
        )}
      </div>

      {showItemPicker && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50" onClick={() => setShowItemPicker(false)}>
          <div className="bg-white dark:bg-gray-800 rounded-2xl p-6 max-w-lg w-full max-h-[80vh] overflow-y-auto shadow-xl" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Select Items</h3>
              <button
                type="button"
                onClick={() => setShowItemPicker(false)}
                className="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {availableItems.length === 0 ? (
              <p className="text-gray-500 dark:text-gray-400 text-center py-8">
                No unbundled items available. Post items for sale first, then create a bundle.
              </p>
            ) : (
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {availableItems.map((item) => {
                  const selected = selectedItems.includes(item.id);
                  return (
                    <label
                      key={item.id}
                      className={clsx(
                        'flex items-center gap-3 p-3 rounded-xl border transition-colors cursor-pointer',
                        selected
                          ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                          : 'border-gray-200 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-700'
                      )}
                    >
                      <input
                        type="checkbox"
                        checked={selected}
                        onChange={() => handleItemToggle(item.id)}
                        className="h-4 w-4 text-primary-600 focus:ring-primary-500"
                      />
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-gray-900 dark:text-white truncate">{item.title}</p>
                        <p className="text-sm text-primary-600 dark:text-primary-400 font-semibold">
                          {item.price.toLocaleString('en-US', { style: 'currency', currency: 'KES', minimumFractionDigits: 0 }).replace('KES', 'KSh')}
                        </p>
                      </div>
                      {selected && <Check className="h-5 w-5 text-primary-600" />}
                    </label>
                  );
                })}
              </div>
            )}

            <div className="flex gap-3 mt-6">
              <Button variant="ghost" onClick={() => setShowItemPicker(false)} className="flex-1">
                Cancel
              </Button>
              <Button variant="primary" onClick={() => setShowItemPicker(false)} className="flex-1">
                Done
              </Button>
            </div>
          </div>
        </div>
      )}

      <Controller
        name="description"
        control={form.control}
        render={({ field }) => (
          <Textarea
            {...field}
            label="Notes for Buyers (optional)"
            placeholder="Any additional details about the pickup, payment, etc."
            rows={3}
            maxLength={200}
            error={errors.description?.message}
          />
        )}
      />

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

      <Button type="submit" variant="primary" size="lg" fullWidth loading={submitting}>
        Create Bundle
      </Button>
    </form>
  );
}