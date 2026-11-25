export const APP_CONFIG = {
  STUDENT_EMAIL_DOMAIN: '',
  LISTING_DAYS: 14,
  EXPIRY_WARNING_DAYS: 3,
  MAX_FEED_LISTINGS: 200,
  MAX_PHOTOS: 5,
  REQUIRE_PHOTO_FOR_SALE: true,
  MIN_BUNDLE_ITEMS: 2,
} as const;

export const LISTING_CATEGORIES = [
  'Books',
  'Electronics',
  'Utensils',
  'Clothes',
  'Furniture',
  'Housing and rentals',
  'Services',
  'Other',
] as const;

export const LISTING_CONDITIONS = [
  'New',
  'Like new',
  'Used - good',
  'Used - fair',
] as const;

export const LISTING_LOCATIONS = [
  'Main gate',
  'Library',
  'Cafeteria',
  'Hostel area',
  'Off campus (nearby)',
  'Other',
] as const;

export const REPORT_REASONS = [
  'Scam or fake listing',
  'Prohibited or illegal item',
  'Wrong price or details',
  'Spam or duplicate',
  'Other',
] as const;

export const STUDENT_EMAIL_DOMAIN = APP_CONFIG.STUDENT_EMAIL_DOMAIN;