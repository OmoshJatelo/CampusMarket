import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  onSnapshot,
  addDoc,
  updateDoc,
  deleteDoc,
  doc,
  getDocs,
} from 'firebase/firestore';
import { db } from '@lib/firebase/config';
import { listingsCol, feedQuery, myListingsQuery, bundleItemsQuery } from '@lib/firebase/firestore';
import { Product, excludeOwners, sortProducts } from '@models/Product';
import { FeedFilter, filterProducts } from '@models/FeedFilter';

const LISTINGS_QUERY_KEY = 'listings';

export function useFeedListings() {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'feed'],
    queryFn: async () => {
      const q = feedQuery();
      const snapshot = await getDocs(q);
      return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
    },
    staleTime: 1000 * 30, // 30 seconds
  });
}

export function useRealtimeFeed() {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'realtime'],
    queryFn: () =>
      new Promise<Product[]>((resolve, reject) => {
        const q = feedQuery();
        const unsubscribe = onSnapshot(
          q,
          (snapshot) => {
            const products = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
            resolve(products);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}

export function useMyListings(userId: string | undefined) {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'mine', userId],
    enabled: !!userId,
    queryFn: async () => {
      if (!userId) return [];
      const q = myListingsQuery(userId);
      const snapshot = await getDocs(q);
      return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
    },
    staleTime: 1000 * 60,
  });
}

export function useRealtimeMyListings(userId: string | undefined) {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'mine-realtime', userId],
    enabled: !!userId,
    queryFn: () =>
      new Promise<Product[]>((resolve, reject) => {
        if (!userId) {
          resolve([]);
          return;
        }
        const q = myListingsQuery(userId);
        const unsubscribe = onSnapshot(
          q,
          (snapshot) => {
            const products = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
            resolve(products);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}

export function useBundleItems(bundleId: string | undefined) {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'bundle-items', bundleId],
    enabled: !!bundleId,
    queryFn: async () => {
      if (!bundleId) return [];
      const q = bundleItemsQuery(bundleId);
      const snapshot = await getDocs(q);
      return snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
    },
    staleTime: 1000 * 60,
  });
}

export function useRealtimeBundleItems(bundleId: string | undefined) {
  return useQuery({
    queryKey: [LISTINGS_QUERY_KEY, 'bundle-items-realtime', bundleId],
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
            const products = snapshot.docs.map((doc) => ({ id: doc.id, ...doc.data() })) as Product[];
            resolve(products);
          },
          reject
        );
        return unsubscribe;
      }),
    staleTime: 0,
  });
}

export function useCreateListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (product: Omit<Product, 'id'>) => {
      const docRef = await addDoc(listingsCol, product);
      return { id: docRef.id, ...product };
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [LISTINGS_QUERY_KEY] });
    },
  });
}

export function useMarkSold() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (listingId: string) => {
      await updateDoc(doc(db, 'listings', listingId), { status: 'SOLD' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [LISTINGS_QUERY_KEY] });
    },
  });
}

export function useReopenListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (listingId: string) => {
      await updateDoc(doc(db, 'listings', listingId), { status: 'ACTIVE' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [LISTINGS_QUERY_KEY] });
    },
  });
}

export function useRenewListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (listingId: string) => {
      const newExpiry = Date.now() + 14 * 24 * 60 * 60 * 1000;
      await updateDoc(doc(db, 'listings', listingId), {
        status: 'ACTIVE',
        expiresAt: newExpiry,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [LISTINGS_QUERY_KEY] });
    },
  });
}

export function useDeleteListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (listingId: string) => {
      await deleteDoc(doc(db, 'listings', listingId));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [LISTINGS_QUERY_KEY] });
    },
  });
}

export function useProcessedFeed(
  allProducts: Product[] | undefined,
  filter: FeedFilter,
  searchQuery: string,
  blockedOwnerIds: Set<string>
) {
  if (!allProducts) return [];

  let filtered = allProducts;

  // Apply search
  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase().trim();
    filtered = filtered.filter(
      (p) =>
        p.title.toLowerCase().includes(query) ||
        p.category.toLowerCase().includes(query) ||
        p.location.toLowerCase().includes(query) ||
        p.description.toLowerCase().includes(query)
    );
  }

  // Apply feed filter
  filtered = filterProducts(filtered, filter);

  // Exclude blocked owners
  filtered = excludeOwners(filtered, blockedOwnerIds);

  // Apply sort
  filtered = sortProducts(filtered, filter.sort);

  return filtered;
}