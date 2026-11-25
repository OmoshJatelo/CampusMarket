export function normalizePhone(raw: string): string | null {
  const digits = raw.replace(/[^0-9+]/g, '');
  let normalized: string;
  if (digits.startsWith('0')) {
    normalized = '+254' + digits.slice(1);
  } else if (digits.startsWith('254')) {
    normalized = '+' + digits;
  } else if (digits.startsWith('7') || digits.startsWith('1')) {
    normalized = '+254' + digits;
  } else {
    return null;
  }
  return normalized.match(/^\+254[17]\d{8}$/) ? normalized : null;
}

export function formatPhoneForDisplay(phone: string): string {
  // Format: +254 7XX XXX XXX
  const normalized = normalizePhone(phone);
  if (!normalized) return phone;
  const digits = normalized.slice(4); // Remove +254
  return `+254 ${digits.slice(0, 3)} ${digits.slice(3, 6)} ${digits.slice(6)}`;
}

export function isValidKenyanPhone(phone: string): boolean {
  return normalizePhone(phone) !== null;
}