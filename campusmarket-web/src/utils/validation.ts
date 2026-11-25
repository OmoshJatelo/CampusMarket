import { z } from 'zod';
import { normalizePhone } from './phone';
import { parsePrice } from '@models/Product';
import { LISTING_CATEGORIES, LISTING_CONDITIONS, LISTING_LOCATIONS } from './constants';

export const phoneSchema = z.string().refine(
  (val) => normalizePhone(val) !== null,
  { message: 'Enter a valid Kenyan number, e.g., 0712 345 678' }
);

export const priceSchema = z.string().refine(
  (val) => parsePrice(val) > 0,
  { message: 'Enter an amount above 0' }
);

export const titleSchema = z.string().min(3, 'Enter a name (at least 3 characters)').max(60);

export const categorySchema = z.enum(LISTING_CATEGORIES, {
  errorMap: () => ({ message: 'Choose a category' }),
});

export const conditionSchema = z.enum(LISTING_CONDITIONS, {
  errorMap: () => ({ message: 'Choose a condition' }),
});

export const locationSchema = z.enum(LISTING_LOCATIONS, {
  errorMap: () => ({ message: 'Choose a pickup location' }),
});

export const descriptionSchema = z.string().max(300).optional();

export const listingTypeSchema = z.enum(['SALE', 'WANTED']);

export const createListingSchema = z.object({
  type: listingTypeSchema,
  title: titleSchema,
  category: categorySchema,
  condition: conditionSchema.optional(),
  price: priceSchema,
  negotiable: z.boolean().default(false),
  location: locationSchema,
  description: descriptionSchema,
  phone: phoneSchema,
  imageUris: z.array(z.string().url()).max(5).default([]),
});

export const createBundleSchema = z.object({
  title: z.string().min(3, 'Enter a name (at least 3 characters)').max(60),
  description: z.string().max(200).optional(),
  pickupLocation: z.enum(LISTING_LOCATIONS, {
    errorMap: () => ({ message: 'Choose a pickup location' }),
  }),
  phone: phoneSchema,
  pickupDate: z.number().min(Date.now() + 24 * 60 * 60 * 1000, 'Choose a pickup date that has not passed'),
  itemIds: z.array(z.string()).min(2, 'Select at least 2 items'),
});

export const authSchema = z.object({
  email: z.string().email('Enter a valid email address').toLowerCase(),
  password: z.string().min(6, 'Password must be at least 6 characters'),
});

export const passwordResetSchema = z.object({
  email: z.string().email('Enter a valid email address').toLowerCase(),
});

export type CreateListingForm = z.infer<typeof createListingSchema>;
export type CreateBundleForm = z.infer<typeof createBundleSchema>;
export type AuthForm = z.infer<typeof authSchema>;
export type PasswordResetForm = z.infer<typeof passwordResetSchema>;

export function validateCreateListing(data: unknown) {
  return createListingSchema.safeParse(data);
}

export function validateCreateBundle(data: unknown) {
  return createBundleSchema.safeParse(data);
}

export function validateAuth(data: unknown) {
  return authSchema.safeParse(data);
}

export function validatePasswordReset(data: unknown) {
  return passwordResetSchema.safeParse(data);
}