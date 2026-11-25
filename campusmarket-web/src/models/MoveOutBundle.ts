export interface MoveOutBundle {
  id: string;
  ownerId: string;
  title: string;
  description: string;
  pickupLocation: string;
  phone: string;
  pickupDate: number;
  createdAt: number;
  itemCount: number;
}

export const DAY_MS = 24 * 60 * 60 * 1000;

export function createMoveOutBundle(
  ownerId: string,
  title: string,
  description: string,
  pickupLocation: string,
  phone: string,
  pickupDate: number,
  itemCount: number
): MoveOutBundle {
  return {
    id: crypto.randomUUID(),
    ownerId,
    title,
    description,
    pickupLocation,
    phone,
    pickupDate,
    createdAt: Date.now(),
    itemCount,
  };
}

export function bundleFromFirestore(data: any, id: string): MoveOutBundle {
  return {
    id,
    ownerId: data.ownerId,
    title: data.title,
    description: data.description || '',
    pickupLocation: data.pickupLocation || '',
    phone: data.phone || '',
    pickupDate: data.pickupDate?.toMillis?.() || data.pickupDate || 0,
    createdAt: data.createdAt?.toMillis?.() || data.createdAt || Date.now(),
    itemCount: data.itemCount || 0,
  };
}

export function bundleToFirestore(bundle: MoveOutBundle) {
  const { id, ...data } = bundle;
  return {
    ...data,
    pickupDate: bundle.pickupDate,
    createdAt: bundle.createdAt,
  };
}

export function isBundleActive(bundle: MoveOutBundle): boolean {
  return Date.now() < bundle.pickupDate + DAY_MS;
}

export function formatPickupDate(pickupDate: number): string {
  const date = new Date(pickupDate);
  return date.toLocaleDateString('en-US', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  });
}

export function getWhatsAppUrl(bundle: MoveOutBundle): string {
  const digits = bundle.phone.replace(/[^0-9]/g, '');
  const message = `Hi, I saw your move-out sale "${bundle.title}" on CampusMarket. Are the items still available?`;
  return `https://wa.me/${digits}?text=${encodeURIComponent(message)}`;
}

export function getCallUrl(bundle: MoveOutBundle): string {
  return `tel:${bundle.phone}`;
}

export const MIN_BUNDLE_ITEMS = 2;
export const MAX_BUNDLE_ITEMS = 50;