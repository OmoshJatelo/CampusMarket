export type ProductType = 'SALE' | 'WANTED';
export type ProductStatus = 'ACTIVE' | 'SOLD' | 'EXPIRED';
import type { FeedFilterSort } from './FeedFilter';

export interface Product {
  id: string;
  type: ProductType;
  title: string;
  price: number;
  negotiable: boolean;
  category: string;
  condition: string;
  location: string;
  description: string;
  phone: string;
  imageUris: string[];
  createdAt: number;
  expiresAt: number;
  status: ProductStatus;
  ownerId: string;
  bundleId?: string;
}

export const LISTING_DAYS = 14;
export const EXPIRY_WARNING_DAYS = 3;
export const MAX_FEED_LISTINGS = 200;
export const MAX_PHOTOS = 5;
export const DAY_MS = 24 * 60 * 60 * 1000;

export function createProduct(
  type: ProductType,
  title: string,
  price: number,
  negotiable: boolean,
  category: string,
  condition: string,
  location: string,
  description: string,
  phone: string,
  imageUris: string[],
  ownerId: string
): Product {
  const now = Date.now();
  return {
    id: crypto.randomUUID(),
    type,
    title,
    price,
    negotiable,
    category,
    condition,
    location,
    description,
    phone,
    imageUris,
    createdAt: now,
    expiresAt: now + LISTING_DAYS * DAY_MS,
    status: 'ACTIVE',
    ownerId,
  };
}

export function productFromFirestore(data: any, id: string): Product {
  return {
    id,
    type: data.type || 'SALE',
    title: data.title,
    price: data.price,
    negotiable: data.negotiable || false,
    category: data.category || '',
    condition: data.condition || '',
    location: data.location || '',
    description: data.description || '',
    phone: data.phone || '',
    imageUris: data.imageUris || [],
    createdAt: data.createdAt?.toMillis?.() || data.createdAt || Date.now(),
    expiresAt: data.expiresAt?.toMillis?.() || data.expiresAt || Date.now() + LISTING_DAYS * DAY_MS,
    status: data.status || 'ACTIVE',
    ownerId: data.ownerId,
    bundleId: data.bundleId,
  };
}

export function productToFirestore(product: Product) {
  const { id, ...data } = product;
  return {
    ...data,
    createdAt: product.createdAt,
    expiresAt: product.expiresAt,
  };
}

export function parsePrice(text: string): number {
  if (!text) return 0;
  const digits = text.replace(/[^0-9]/g, '');
  if (!digits) return 0;
  const parsed = parseInt(digits, 10);
  return isNaN(parsed) ? 0 : parsed;
}

export function formatPrice(price: number): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'KES',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(price).replace('KES', 'KSh');
}

export function formatPriceCompact(price: number): string {
  return `KSh ${price.toLocaleString('en-US')}`;
}

export function isExpired(product: Product): boolean {
  return Date.now() > product.expiresAt;
}

export function isActive(product: Product): boolean {
  return product.status === 'ACTIVE' && !isExpired(product);
}

export function daysLeft(product: Product): number {
  const ms = product.expiresAt - Date.now();
  if (ms <= 0) return 0;
  return Math.ceil(ms / DAY_MS);
}

export function isExpiringSoon(product: Product): boolean {
  return isActive(product) && daysLeft(product) <= EXPIRY_WARNING_DAYS;
}

export function isWanted(product: Product): boolean {
  return product.type === 'WANTED';
}

export function getFirstImageUri(product: Product): string | null {
  return product.imageUris.length > 0 ? product.imageUris[0] : null;
}

export function getTimeAgo(createdAt: number): string {
  const minutes = Math.floor((Date.now() - createdAt) / 60000);
  if (minutes < 1) return 'Just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

export function getProductTimeAgo(product: Product): string {
  return getTimeAgo(product.createdAt);
}

export function getWhatsAppUrl(product: Product): string {
  const digits = product.phone.replace(/[^0-9]/g, '');
  const message = isWanted(product)
    ? `Hi, I saw your request for "${product.title}" on Campus Market. I may have one.`
    : `Hi, is "${product.title}" still available? (Campus Market)`;
  return `https://wa.me/${digits}?text=${encodeURIComponent(message)}`;
}

export function getCallUrl(product: Product): string {
  return `tel:${product.phone}`;
}

export function filterActiveProducts(products: Product[]): Product[] {
  return products.filter(isActive);
}

export function excludeOwners(products: Product[], blockedOwnerIds: Set<string>): Product[] {
  return products.filter((p) => p.ownerId && !blockedOwnerIds.has(p.ownerId));
}

export function sortProducts(products: Product[], sort: FeedFilterSort): Product[] {
  const sorted = [...products];
  switch (sort) {
    case 'PRICE_LOW':
      return sorted.sort((a, b) => a.price - b.price);
    case 'PRICE_HIGH':
      return sorted.sort((a, b) => b.price - a.price);
    case 'NEWEST':
    default:
      return sorted.sort((a, b) => b.createdAt - a.createdAt);
  }
}