import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { query, orderBy, onSnapshot, addDoc, deleteDoc, doc, getDocs, Timestamp } from 'firebase/firestore';
import { db } from '@lib/firebase/config';
import { blockedCol, reportsCol } from '@lib/firebase/firestore';
import { BlockedUser, blockedUserFromFirestore, getBlockedIds } from '@models/BlockedUser';
import { useAuth } from './useAuth';

export function useBlockedUsers() {
  const { user } = useAuth();

  return useQuery({
    queryKey: ['moderation', 'blocked', user?.uid],
    enabled: !!user,
    queryFn: async () => {
      if (!user) return [];
      const q = query(blockedCol(user.uid), orderBy('createdAt', 'desc'));
      const snapshot = await getDocs(q);
      return snapshot.docs.map((doc) => blockedUserFromFirestore(doc.data(), doc.id)) as BlockedUser[];
    },
    staleTime: 1000 * 60 * 10,
  });
}

export function useRealtimeBlockedUsers() {
  const { user } = useAuth();

  return useQuery({
    queryKey: ['moderation', 'blocked-realtime', user?.uid],
    enabled: !!user,
    queryFn: () =>
      new Promise<BlockedUser[]>((resolve, reject) => {
        if (!user) {
          resolve([]);
          return;
        }
        const q = query(blockedCol(user.uid), orderBy('createdAt', 'desc'));
        const unsubscribe = onSnapshot(
          q,
          (snapshot) => {
            const blocked = snapshot.docs
              .map((doc) => blockedUserFromFirestore(doc.data(), doc.id)) as BlockedUser[];
            resolve(blocked);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}

export function useBlockUser() {
  const queryClient = useQueryClient();
  const { user } = useAuth();

  return useMutation({
    mutationFn: async ({ blockedId: _blockedId, label }: { blockedId: string; label: string }) => {
      if (!user) throw new Error('Not signed in');
      await addDoc(blockedCol(user.uid), {
        label,
        createdAt: Date.now(),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moderation', 'blocked'] });
    },
  });
}

export function useUnblockUser() {
  const queryClient = useQueryClient();
  const { user } = useAuth();

  return useMutation({
    mutationFn: async (blockedId: string) => {
      if (!user) throw new Error('Not signed in');
      await deleteDoc(doc(db, 'users', user.uid, 'blocked', blockedId));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moderation', 'blocked'] });
    },
  });
}

export function useReportListing() {
  const queryClient = useQueryClient();
  const { user } = useAuth();

  return useMutation({
    mutationFn: async ({
      listingId,
      sellerId,
      listingTitle,
      reason,
    }: {
      listingId: string;
      sellerId: string;
      listingTitle: string;
      reason: string;
    }) => {
      if (!user) throw new Error('Not signed in');

      await addDoc(reportsCol, {
        reporterId: user.uid,
        listingId,
        sellerId,
        listingTitle,
        reason,
        status: 'OPEN',
        createdAt: Timestamp.fromMillis(Date.now()),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moderation', 'reports'] });
    },
  });
}

export function useBlockedOwnerIds() {
  const { data: blockedUsers = [] } = useBlockedUsers();
  return getBlockedIds(blockedUsers);
}