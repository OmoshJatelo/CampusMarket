import {
  collection,
  query,
  where,
  orderBy,
  limit,
  type Query,
  type QueryConstraint,
  type DocumentData,
  type FirestoreDataConverter,
  type WithFieldValue,
  Timestamp,
} from 'firebase/firestore';
import { db } from './config';
import type { Product } from '@models/Product';
import type { MoveOutBundle } from '@models/MoveOutBundle';
import type { BlockedUser } from '@models/BlockedUser';

const timestampConverter = {
  toFirestore: (data: WithFieldValue<DocumentData>) => {
    const result: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(data)) {
      if (value instanceof Date) {
        result[key] = Timestamp.fromDate(value);
      } else if (typeof value === 'number' && key.endsWith('At')) {
        result[key] = Timestamp.fromMillis(value);
      } else {
        result[key] = value;
      }
    }
    return result;
  },
  fromFirestore: (snapshot: any) => {
    const data = snapshot.data();
    const result: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(data)) {
      if (value instanceof Timestamp) {
        result[key] = value.toMillis();
      } else {
        result[key] = value;
      }
    }
    return result;
  },
};

function createConverter<T extends { id?: string }>(): FirestoreDataConverter<T> {
  return {
    toFirestore: (model: WithFieldValue<T>) => {
      const { id, ...data } = model as any;
      return timestampConverter.toFirestore(data);
    },
    fromFirestore: (snapshot: any) => {
      const data = timestampConverter.fromFirestore(snapshot);
      return { id: snapshot.id, ...data } as T;
    },
  };
}

export const listingsCol = collection(db, 'listings').withConverter(createConverter<Product>());
export const bundlesCol = collection(db, 'bundles').withConverter(createConverter<MoveOutBundle>());
export const reportsCol = collection(db, 'reports');
export const usersCol = collection(db, 'users');

export function blockedCol(uid: string) {
  return collection(db, 'users', uid, 'blocked').withConverter(createConverter<BlockedUser>());
}

export function listingsQuery(constraints: QueryConstraint[] = []): Query<Product> {
  return query(listingsCol, ...constraints);
}

export function bundlesQuery(constraints: QueryConstraint[] = []): Query<MoveOutBundle> {
  return query(bundlesCol, ...constraints);
}

export const feedQuery = () =>
  query(listingsCol, orderBy('createdAt', 'desc'), limit(200));

export const myListingsQuery = (uid: string) =>
  query(listingsCol, where('ownerId', '==', uid), orderBy('createdAt', 'desc'));

export const bundleItemsQuery = (bundleId: string) =>
  query(listingsCol, where('bundleId', '==', bundleId));

export const activeBundlesQuery = () =>
  query(bundlesCol, orderBy('createdAt', 'desc'), limit(50));