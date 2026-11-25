import { Product } from '@models/Product';
import { MoveOutBundle } from '@models/MoveOutBundle';
import { formatPriceCompact } from '@models/Product';

const MAX_BUNDLE_LINES = 8;

export async function sharePost(product: Product): Promise<boolean> {
  const text = buildShareText(product);
  return shareText(text);
}

export async function shareBundle(bundle: MoveOutBundle, items: Product[]): Promise<boolean> {
  const text = buildBundleShareText(bundle, items);
  return shareText(text);
}

function buildShareText(product: Product): string {
  const lines: string[] = [];

  if (isWanted(product)) {
    lines.push(`Looking for: ${product.title} (budget ${formatPriceCompact(product.price)})`);
  } else {
    lines.push(`For sale: ${product.title} for ${formatPriceCompact(product.price)}`);
    if (product.negotiable) {
      lines.push('(negotiable)');
    }
  }

  if (product.location) {
    lines.push(`Pickup: ${product.location}`);
  }

  const photo = product.imageUris[0];
  if (photo && photo.startsWith('http')) {
    lines.push(`Photo: ${photo}`);
  }

  lines.push('\nPosted on CampusMarket');
  return lines.join('\n');
}

function buildBundleShareText(bundle: MoveOutBundle, items: Product[]): string {
  const lines: string[] = [];
  lines.push(`Move-out sale: ${bundle.title}`);

  let total = 0;
  let shown = 0;

  for (const product of items) {
    total += product.price;
    if (shown < MAX_BUNDLE_LINES) {
      lines.push(`- ${product.title}: ${formatPriceCompact(product.price)}`);
      shown++;
    }
  }

  if (items.length > MAX_BUNDLE_LINES) {
    lines.push(`+ ${items.length - MAX_BUNDLE_LINES} more items`);
  }

  lines.push('');
  lines.push(`${items.length} items, ${formatPriceCompact(total)} in total`);
  lines.push(`Pickup: ${bundle.pickupLocation}, ${formatPickupDate(bundle.pickupDate)}`);
  lines.push('\nPosted on CampusMarket');

  return lines.join('\n');
}

function formatPickupDate(timestamp: number): string {
  const date = new Date(timestamp);
  return date.toLocaleDateString('en-US', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'UTC',
  });
}

async function shareText(text: string): Promise<boolean> {
  if (navigator.share) {
    try {
      await navigator.share({ text });
      return true;
    } catch {
      // User cancelled or error, fall back to clipboard
    }
  }

  // Clipboard fallback
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}

import { isWanted } from '@models/Product';