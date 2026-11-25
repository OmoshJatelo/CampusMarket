export function formatKSh(amount: number): string {
  return `KSh ${amount.toLocaleString('en-US')}`;
}

export function formatKShCompact(amount: number): string {
  if (amount >= 1000000) {
    return `KSh ${(amount / 1000000).toFixed(1)}M`;
  }
  if (amount >= 1000) {
    return `KSh ${(amount / 1000).toFixed(1)}K`;
  }
  return `KSh ${amount}`;
}

export function parseKSh(text: string): number {
  if (!text) return 0;
  const digits = text.replace(/[^0-9]/g, '');
  if (!digits) return 0;
  const parsed = parseInt(digits, 10);
  return isNaN(parsed) ? 0 : parsed;
}

export function formatPriceForDisplay(price: number, negotiable = false): string {
  const formatted = formatKSh(price);
  return negotiable ? `${formatted} (negotiable)` : formatted;
}

export function formatBudgetForDisplay(budget: number): string {
  return `Budget: ${formatKSh(budget)}`;
}