export interface BlockedUser {
  id: string;
  label: string;
  createdAt: number;
}

export function blockedUserFromFirestore(data: any, id: string): BlockedUser {
  return {
    id,
    label: data.label || '',
    createdAt: data.createdAt?.toMillis?.() || data.createdAt || Date.now(),
  };
}

export function getBlockedIds(blockedUsers: BlockedUser[]): Set<string> {
  return new Set(blockedUsers.map((b) => b.id));
}