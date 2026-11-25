import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { query, onSnapshot, getDocs, writeBatch, doc, collection, where } from 'firebase/firestore';
import { db } from '@lib/firebase/config';
import { activeBundlesQuery, bundleItemsQuery } from '@lib/firebase/firestore';
import { MoveOutBundle, isBundleActive, bundleToFirestore } from '@models/MoveOutBundle';
import { Product } from '@models/Product';

const BUNDLES_QUERY_KEY = 'bundles';

export function useBundles() {
  return useQuery({
    queryKey: [BUNDLES_QUERY_KEY, 'active'],
    queryFn: async () => {
      const q = activeBundlesQuery();
      const snapshot = await getDocs(q);
      return snapshot.docs
        .map((doc) => ({ id: doc.id, ...doc.data() })) as MoveOutBundle[];
    },
    staleTime: 1000 * 60 * 5,
  });
}

export function useRealtimeBundles() {
  return useQuery({
    queryKey: [BUNDLES_QUERY_KEY, 'active-realtime'],
    queryFn: () =>
      new Promise<MoveOutBundle[]>((resolve, reject) => {
        const q = activeBundlesQuery();
        const unsubscribe = onSnapshot(
          q,
          (snapshot) => {
            const bundles = snapshot.docs
              .map((doc) => ({ id: doc.id, ...doc.data() })) as MoveOutBundle[];
            const activeBundles = bundles.filter(isBundleActive);
            resolve(activeBundles);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}

export function useCreateBundle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      bundle,
      listingIds,
    }: {
      bundle: Omit<MoveOutBundle, 'id'>;
      listingIds: string[];
    }) => {
      const batch = writeBatch(db);

      // Create bundle document
      const bundleRef = doc(db, 'bundles');
      batch.set(bundleRef, bundleToFirestore({ ...bundle, id: bundleRef.id } as MoveOutBundle));

      // Update each listing with bundleId
      for (const listingId of listingIds) {
        const listingRef = doc(db, 'listings', listingId);
        batch.update(listingRef, { bundleId: bundleRef.id });
      }

      await batch.commit();
      return { id: bundleRef.id, ...bundle };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [BUNDLES_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

export function useCloseBundle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (bundleId: string) => {
      const batch = writeBatch(db);

      // Find and unlink listings
      const listingsQuery = query(
        collection(db, 'listings'),
        where('bundleId', '==', bundleId)
      );
      const listingsSnapshot = await getDocs(listingsQuery);

      for (const listingDoc of listingsSnapshot.docs) {
        batch.update(listingDoc.ref, { bundleId: null });
      }

      // Delete bundle
      batch.delete(doc(db, 'bundles', bundleId));

      await batch.commit();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [BUNDLES_QUERY_KEY] });
      queryClient.invalidateQueries({ queryKey: ['listings'] });
    },
  });
}

export function useAvailableBundleItems(userId: string | undefined) {
  return useQuery({
    queryKey: ['listings', 'available-for-bundle', userId],
    enabled: !!userId,
    queryFn: async () => {
      if (!userId) return [];
      const q = query(
        collection(db, 'listings'),
        where('ownerId', '==', userId),
        where('bundleId', '==', null),
        where('status', '==', 'ACTIVE'),
        where('type', '==', 'SALE')
      );
      const snapshot = await getDocs(q);
      return snapshot.docs
        .map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
    },
    staleTime: 1000 * 60,
  });
}

export function useRealtimeBundleItems(bundleId: string | undefined) {
  return useQuery({
    queryKey: ['listings', 'bundle-items', bundleId],
    enabled: !!bundleId,
    queryFn: () =>
      new Promise<Product[]>((resolve, reject) => {
        if (!bundleId) {
          resolve([]);
          return;
        }
        const q = bundleItemsQuery(bundleId);
        const unsubscribe = onSnapshot(
          q,
          (snapshot) => {
            const items = snapshot.docs
              .map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
            resolve(items);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}