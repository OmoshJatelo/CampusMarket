export type FeedFilterSort = 'NEWEST' | 'PRICE_LOW' | 'PRICE_HIGH';
export type FeedFilterType = 'ALL' | 'SALE' | 'WANTED';

export interface FeedFilter {
  sort: FeedFilterSort;
  type: FeedFilterType;
  minPrice: number;
  maxPrice: number;
  conditions: Set<string>;
}

export const defaultFeedFilter: FeedFilter = {
  sort: 'NEWEST',
  type: 'ALL',
  minPrice: 0,
  maxPrice: 0,
  conditions: new Set(),
};

export function createFeedFilter(partial: Partial<FeedFilter> = {}): FeedFilter {
  return {
    ...defaultFeedFilter,
    ...partial,
    conditions: new Set(partial.conditions || []),
  };
}

export function feedFilterMatches(filter: FeedFilter, product: Product): boolean {
  if (filter.type === 'SALE' && isWanted(product)) return false;
  if (filter.type === 'WANTED' && !isWanted(product)) return false;

  if (filter.minPrice > 0 && product.price < filter.minPrice) return false;
  if (filter.maxPrice > 0 && product.price > filter.maxPrice) return false;

  if (filter.conditions.size > 0 && !isWanted(product) && !filter.conditions.has(product.condition)) {
    return false;
  }

  return true;
}

export function filterProducts(products: Product[], filter: FeedFilter): Product[] {
  return products.filter((p) => feedFilterMatches(filter, p));
}

export function getActiveFilterCount(filter: FeedFilter): number {
  let count = 0;
  if (filter.sort !== 'NEWEST') count++;
  if (filter.type !== 'ALL') count++;
  if (filter.minPrice > 0 || filter.maxPrice > 0) count++;
  if (filter.conditions.size > 0) count++;
  return count;
}

export function resetFeedFilter(): FeedFilter {
  return defaultFeedFilter;
}

export function feedFilterToUrlParams(filter: FeedFilter): URLSearchParams {
  const params = new URLSearchParams();
  if (filter.sort !== 'NEWEST') params.set('sort', filter.sort);
  if (filter.type !== 'ALL') params.set('type', filter.type);
  if (filter.minPrice > 0) params.set('minPrice', filter.minPrice.toString());
  if (filter.maxPrice > 0) params.set('maxPrice', filter.maxPrice.toString());
  if (filter.conditions.size > 0) params.set('conditions', [...filter.conditions].join(','));
  return params;
}

export function feedFilterFromUrlParams(params: URLSearchParams): FeedFilter {
  const filter = createFeedFilter();
  const sort = params.get('sort');
  if (sort && ['NEWEST', 'PRICE_LOW', 'PRICE_HIGH'].includes(sort)) filter.sort = sort as FeedFilterSort;
  const type = params.get('type');
  if (type && ['ALL', 'SALE', 'WANTED'].includes(type)) filter.type = type as FeedFilterType;
  const minPrice = params.get('minPrice');
  if (minPrice) filter.minPrice = parseInt(minPrice, 10);
  const maxPrice = params.get('maxPrice');
  if (maxPrice) filter.maxPrice = parseInt(maxPrice, 10);
  const conditions = params.get('conditions');
  if (conditions) conditions.split(',').forEach((c) => filter.conditions.add(c));
  return filter;
}

import { Product, isWanted } from './Product';