import { useMemo } from 'react';
import { useFeedListings } from './useListings';
import { useAuth } from './useAuth';
import { useBlockedUsers } from './useModeration';
import { findMatches, getUniqueMatchedProducts, getMatchCountForBadge } from '@models/MatchEngine';

export function useMatches() {
  const { user } = useAuth();
  const { data: allProducts = [] } = useFeedListings();
  const { data: blockedUsers = [] } = useBlockedUsers();

  const blockedOwnerIds = useMemo(
    () => new Set(blockedUsers.map((b) => b.id)),
    [blockedUsers]
  );

  const activeProducts = useMemo(
    () => allProducts.filter((p) => !blockedOwnerIds.has(p.ownerId)),
    [allProducts, blockedOwnerIds]
  );

  const matches = useMemo(
    () => (user ? findMatches(user.uid, activeProducts) : []),
    [user?.uid, activeProducts]
  );

  const matchedProducts = useMemo(
    () => getUniqueMatchedProducts(matches),
    [matches]
  );

  const matchCount = useMemo(
    () => matchedProducts.length,
    [matchedProducts]
  );

  return {
    matches,
    matchedProducts,
    matchCount,
    isLoading: false, // We use useFeedListings which handles loading
  };
}

export function useMatchBadge() {
  const { user } = useAuth();
  const { data: allProducts = [] } = useFeedListings();
  const { data: blockedUsers = [] } = useBlockedUsers();

  const blockedOwnerIds = useMemo(
    () => new Set(blockedUsers.map((b) => b.id)),
    [blockedUsers]
  );

  const activeProducts = useMemo(
    () => allProducts.filter((p) => !blockedOwnerIds.has(p.ownerId)),
    [allProducts, blockedOwnerIds]
  );

  const count = useMemo(
    () => (user ? getMatchCountForBadge(user.uid, activeProducts) : 0),
    [user?.uid, activeProducts]
  );

  return count;
}

// For Matches page - fetch feed separately to avoid dependency issues
export function useMatchesPage() {
  const { user } = useAuth();
  const { data: allProducts = [], isLoading, error } = useFeedListings();
  const { data: blockedUsers = [] } = useBlockedUsers();

  const blockedOwnerIds = useMemo(
    () => new Set(blockedUsers.map((b) => b.id)),
    [blockedUsers]
  );

  const activeProducts = useMemo(
    () => allProducts.filter((p) => !blockedOwnerIds.has(p.ownerId)),
    [allProducts, blockedOwnerIds]
  );

  const matches = useMemo(
    () => (user ? findMatches(user.uid, activeProducts) : []),
    [user?.uid, activeProducts]
  );

  const matchedProducts = useMemo(
    () => getUniqueMatchedProducts(matches),
    [matches]
  );

  return {
    matchedProducts,
    isLoading,
    error,
  };
}