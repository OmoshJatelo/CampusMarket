const ONBOARDING_KEY = 'campus_market_onboarding_done';
const NOTIFIED_MATCHES_PREFIX = 'notified_matches_';
const LAST_PHONE_KEY = 'last_phone';

export function isOnboardingDone(): boolean {
  try {
    return localStorage.getItem(ONBOARDING_KEY) === 'true';
  } catch {
    return false;
  }
}

export function setOnboardingDone(): void {
  try {
    localStorage.setItem(ONBOARDING_KEY, 'true');
  } catch {
    // Ignore storage errors
  }
}

export function getNotifiedMatchKeys(userId: string): Set<string> {
  try {
    const stored = localStorage.getItem(`${NOTIFIED_MATCHES_PREFIX}${userId}`);
    return stored ? new Set(JSON.parse(stored)) : new Set();
  } catch {
    return new Set();
  }
}

export function addNotifiedMatchKey(userId: string, key: string): void {
  try {
    const keys = getNotifiedMatchKeys(userId);
    keys.add(key);
    localStorage.setItem(`${NOTIFIED_MATCHES_PREFIX}${userId}`, JSON.stringify([...keys]));
  } catch {
    // Ignore storage errors
  }
}

export function getLastPhone(): string {
  try {
    return localStorage.getItem(LAST_PHONE_KEY) || '';
  } catch {
    return '';
  }
}

export function setLastPhone(phone: string): void {
  try {
    localStorage.setItem(LAST_PHONE_KEY, phone);
  } catch {
    // Ignore storage errors
  }
}

export function clearAllStorage(): void {
  try {
    localStorage.removeItem(ONBOARDING_KEY);
    localStorage.removeItem(LAST_PHONE_KEY);
    // Clear notified matches for all users
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key?.startsWith(NOTIFIED_MATCHES_PREFIX)) {
        localStorage.removeItem(key);
      }
    }
  } catch {
    // Ignore storage errors
  }
}