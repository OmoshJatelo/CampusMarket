import { useState } from 'react';
import { X } from 'lucide-react';
import { clsx } from 'clsx';
import { FeedFilter, defaultFeedFilter, FeedFilterSort, FeedFilterType } from '@models/FeedFilter';
import { LISTING_CONDITIONS } from '@utils/constants';
import { Button } from '@components/ui/Button';
import { Input } from '@components/ui/Input';

interface FilterSheetProps {
  isOpen: boolean;
  onClose: () => void;
  filter: FeedFilter;
  onApply: (filter: FeedFilter) => void;
}

export function FilterSheet({ isOpen, onClose, filter, onApply }: FilterSheetProps) {
  const [localFilter, setLocalFilter] = useState(filter);

  const sortOptions = [
    { value: 'NEWEST', label: 'Newest' },
    { value: 'PRICE_LOW', label: 'Price: Low to High' },
    { value: 'PRICE_HIGH', label: 'Price: High to Low' },
  ];

  const typeOptions = [
    { value: 'ALL', label: 'Everything' },
    { value: 'SALE', label: 'For Sale' },
    { value: 'WANTED', label: 'Wanted Requests' },
  ];

  const handleSortChange = (sort: FeedFilterSort) => {
    setLocalFilter((prev) => ({ ...prev, sort }));
  };

  const handleTypeChange = (type: FeedFilterType) => {
    setLocalFilter((prev) => ({ ...prev, type }));
  };

  const handleConditionToggle = (condition: string) => {
    setLocalFilter((prev) => {
      const newConditions = new Set(prev.conditions);
      if (newConditions.has(condition)) {
        newConditions.delete(condition);
      } else {
        newConditions.add(condition);
      }
      return { ...prev, conditions: newConditions };
    });
  };

  const handlePriceChange = (field: 'minPrice' | 'maxPrice', value: string) => {
    const num = parseInt(value, 10);
    if (isNaN(num) || num < 0) {
      setLocalFilter((prev) => ({ ...prev, [field]: 0 }));
    } else {
      setLocalFilter((prev) => ({ ...prev, [field]: num }));
    }
  };

  const handleReset = () => {
    setLocalFilter(defaultFeedFilter);
  };

  const handleApply = () => {
    onApply(localFilter);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 lg:hidden" role="dialog" aria-modal="true" aria-labelledby="filter-sheet-title">
      <div className="fixed inset-0 bg-black/50" onClick={onClose} aria-hidden="true" />
      <div className="fixed bottom-0 left-0 right-0 bg-white dark:bg-gray-900 rounded-t-2xl shadow-xl max-h-[85vh] overflow-y-auto animate-slide-up">
        <div className="sticky top-0 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-4 py-3 flex items-center justify-between">
          <h2 id="filter-sheet-title" className="text-lg font-semibold text-gray-900 dark:text-white">
            Filters and sorting
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="p-2 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-800"
            aria-label="Close filters"
          >
            <X className="h-6 w-6" />
          </button>
        </div>

        <div className="p-4 space-y-6">
          {/* Sort */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Sort by</h3>
            <div className="space-y-2">
              {sortOptions.map((option) => (
                <label
                  key={option.value}
                  className={clsx(
                    'flex items-center justify-between px-4 py-3 rounded-xl border transition-colors cursor-pointer',
                    localFilter.sort === option.value
                      ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                      : 'border-gray-200 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-800'
                  )}
                >
                  <span className="text-gray-900 dark:text-white">{option.label}</span>
                  <input
                    type="radio"
                    name="sort"
                    value={option.value}
                    checked={localFilter.sort === option.value}
                    onChange={() => handleSortChange(option.value as FeedFilterSort)}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500"
                  />
                </label>
              ))}
            </div>
          </div>

          {/* Price Range */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Price range (KSh)</h3>
            <div className="flex gap-3">
              <Input
                type="number"
                placeholder="Min"
                value={localFilter.minPrice > 0 ? localFilter.minPrice : ''}
                onChange={(e) => handlePriceChange('minPrice', e.target.value)}
                className="w-1/2"
                inputMode="numeric"
              />
              <Input
                type="number"
                placeholder="Max"
                value={localFilter.maxPrice > 0 ? localFilter.maxPrice : ''}
                onChange={(e) => handlePriceChange('maxPrice', e.target.value)}
                className="w-1/2"
                inputMode="numeric"
              />
            </div>
          </div>

          {/* Condition */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Condition</h3>
            <div className="flex flex-wrap gap-2">
              {LISTING_CONDITIONS.map((condition) => (
                <label
                  key={condition}
                  className={clsx(
                    'px-3 py-1.5 rounded-full text-sm font-medium cursor-pointer transition-colors',
                    localFilter.conditions.has(condition)
                      ? 'bg-primary-600 text-white'
                      : 'bg-gray-100 text-gray-700 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700'
                  )}
                >
                  <input
                    type="checkbox"
                    value={condition}
                    checked={localFilter.conditions.has(condition)}
                    onChange={() => handleConditionToggle(condition)}
                    className="sr-only"
                  />
                  {condition}
                </label>
              ))}
            </div>
          </div>

          {/* Type */}
          <div>
            <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-3">Show</h3>
            <div className="space-y-2">
              {typeOptions.map((option) => (
                <label
                  key={option.value}
                  className={clsx(
                    'flex items-center justify-between px-4 py-3 rounded-xl border transition-colors cursor-pointer',
                    localFilter.type === option.value
                      ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                      : 'border-gray-200 hover:bg-gray-50 dark:border-gray-700 dark:hover:bg-gray-800'
                  )}
                >
                  <span className="text-gray-900 dark:text-white">{option.label}</span>
                  <input
                    type="radio"
                    name="type"
                    value={option.value}
                    checked={localFilter.type === option.value}
                    onChange={() => handleTypeChange(option.value as FeedFilterType)}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500"
                  />
                </label>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
            <Button variant="ghost" onClick={handleReset} className="flex-1">
              Reset
            </Button>
            <Button variant="primary" onClick={handleApply} className="flex-1">
              Apply
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}