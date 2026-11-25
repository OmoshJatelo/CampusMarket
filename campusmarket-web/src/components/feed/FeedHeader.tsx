import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, ChevronDown } from 'lucide-react';
import { clsx } from 'clsx';
import { FeedFilter, getActiveFilterCount } from '@models/FeedFilter';
import { LISTING_CATEGORIES } from '@utils/constants';
import { Button } from '@components/ui/Button';
import { FilterSheet } from './FilterSheet';
import { CategoryChip } from './CategoryChip';

interface FeedHeaderProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  filter: FeedFilter;
  onFilterChange: (filter: FeedFilter) => void;
  onCategorySelect: (category: string) => void;
  activeCategory: string | null;
}

export function FeedHeader({
  searchQuery,
  onSearchChange,
  filter,
  onFilterChange,
  onCategorySelect,
  activeCategory,
}: FeedHeaderProps) {
  const [filterSheetOpen, setFilterSheetOpen] = useState(false);
  const filterButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (filterButtonRef.current && !filterButtonRef.current.contains(e.target as Node)) {
        setFilterSheetOpen(false);
      }
    };

    if (filterSheetOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [filterSheetOpen]);

  return (
    <div className="space-y-4">
      {/* Title and tagline */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">CampusMarket</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400">Buy and sell items on campus</p>
        </div>
        <Link
          to="/listing/new"
          className="p-2 rounded-lg bg-primary-600 text-white hover:bg-primary-700 transition-colors lg:hidden"
          aria-label="Create new listing"
        >
          <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </Link>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" aria-hidden="true" />
        <input
          type="search"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search items…"
          className="w-full pl-12 pr-4 py-3 rounded-xl border border-gray-300 bg-white text-gray-900 placeholder:text-gray-400 focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 focus:outline-none transition-all duration-200 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 dark:placeholder:text-gray-500"
          aria-label="Search listings"
        />
      </div>

      {/* Filters and Category Chips */}
      <div className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center gap-2">
          <CategoryChip
            label="All"
            active={!activeCategory}
            onClick={() => onCategorySelect('')}
          />
          {LISTING_CATEGORIES.slice(0, 4).map((category) => (
            <CategoryChip
              key={category}
              label={category}
              active={activeCategory === category}
              onClick={() => onCategorySelect(category as string)}
            />
          ))}
          <CategoryChip
            label="More"
            active={!!activeCategory && !LISTING_CATEGORIES.slice(0, 4).includes(activeCategory! as typeof LISTING_CATEGORIES[number])}
            onClick={() => {}}
          />
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button
            ref={filterButtonRef}
            variant="outline"
            size="sm"
            onClick={() => setFilterSheetOpen(!filterSheetOpen)}
            className="flex items-center gap-1.5"
          >
            <Filter className="h-4 w-4" />
            <span>Filters</span>
            {getActiveFilterCount(filter) > 0 && (
              <span className="bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-300 text-xs font-medium px-1.5 py-0.5 rounded-full">
                {getActiveFilterCount(filter)}
              </span>
            )}
            <ChevronDown className={clsx('h-4 w-4 transition-transform', filterSheetOpen && 'rotate-180')} />
          </Button>

          <Link to="/listing/new" className="ml-auto hidden sm:inline-flex">
            <Button variant="primary" size="sm">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Post Item
            </Button>
          </Link>
        </div>
      </div>

      {/* Filter Sheet */}
      <FilterSheet
        isOpen={filterSheetOpen}
        onClose={() => setFilterSheetOpen(false)}
        filter={filter}
        onApply={onFilterChange}
      />
    </div>
  );
}