// Firebase type declarations for bundler module resolution
// This file provides types for firebase/auth, firebase/firestore, firebase/app
// when the package's own types are not properly resolved by bundler resolution

// firebase/app
declare module 'firebase/app' {
  export interface FirebaseOptions {
    apiKey: string;
    authDomain: string;
    projectId: string;
    storageBucket: string;
    messagingSenderId: string;
    appId: string;
    measurementId?: string;
  }

  export interface FirebaseApp {
    readonly name: string;
    readonly options: FirebaseOptions;
    automaticDataCollectionEnabled: boolean;
  }

  export function initializeApp(options: FirebaseOptions, name?: string): FirebaseApp;
  export function getApps(): FirebaseApp[];
  export function getApp(name?: string): FirebaseApp;
  export function registerVersion(name: string, version: string, platform?: string): void;
  export function onLog(logCallback: (options: { level: string; message: string; args: unknown[] }) => void, options?: { level: string }): () => void;
  export function setLogLevel(logLevel: string): void;
  export const SDK_VERSION: string;
  export const DEFAULT_ENTRY_NAME = '[DEFAULT]';
}

// firebase/auth
declare module 'firebase/auth' {
  import type { FirebaseApp } from 'firebase/app';

  export interface Auth {
    readonly app: FirebaseApp;
    readonly currentUser: User | null;
    readonly config: { apiKey: string; appName: string };
    readonly name: string;
    languageCode: string | null;
    settings: { appVerificationDisabledForTesting: boolean };
    tenantId: string | null;
  }

  export interface User {
    readonly uid: string;
    readonly email: string | null;
    readonly displayName: string | null;
    readonly photoURL: string | null;
    readonly emailVerified: boolean;
    readonly isAnonymous: boolean;
    readonly metadata: { creationTime: string; lastSignInTime: string };
    readonly phoneNumber: string | null;
    readonly providerData: UserInfo[];
    readonly refreshToken: string;
    readonly tenantId: string | null;
    delete(): Promise<void>;
    getIdToken(forceRefresh?: boolean): Promise<string>;
    getIdTokenResult(forceRefresh?: boolean): Promise<IdTokenResult>;
    reload(): Promise<void>;
    toJSON(): object;
  }

  export interface UserInfo {
    readonly uid: string;
    readonly displayName: string | null;
    readonly email: string | null;
    readonly photoURL: string | null;
    readonly phoneNumber: string | null;
    readonly providerId: string;
  }

  export interface UserCredential {
    readonly user: User;
    readonly providerId: string;
    readonly operationType: string;
  }

  export interface Persistence {
    readonly type: 'NONE' | 'SESSION' | 'LOCAL';
  }

  export interface IdTokenResult {
    readonly token: string;
    readonly accessToken: string;
    readonly expirationTime: string;
    readonly issuedAtTime: string;
    readonly signInProvider: string | null;
    readonly signInSecondFactor: string | null;
    readonly claims: { [key: string]: unknown };
  }

  export interface ActionCodeInfo {
    readonly operation: string;
    readonly data: { email?: string | null; previousEmail?: string | null; multiFactorInfo?: MultiFactorInfo | null };
  }

  export interface ActionCodeSettings {
    url: string;
    handleCodeInApp?: boolean;
    iOS?: { bundleId: string };
    android?: { packageName: string; installApp?: boolean; minimumVersion?: string };
    dynamicLinkDomain?: string;
  }

  export interface MultiFactorInfo {
    readonly factorId: string;
    readonly uid: string;
    readonly displayName: string | null;
    readonly enrollmentTime: string;
    readonly phoneNumber?: string;
  }

  export interface MultiFactorUser {
    readonly enrolledFactors: MultiFactorInfo[];
    enroll(credential: AuthCredential, displayName?: string): Promise<MultiFactorInfo>;
    unenroll(info: MultiFactorInfo | string): Promise<void>;
    getSession(): Promise<MultiFactorSession>;
  }

  export interface MultiFactorSession {}

  export interface AuthCredential {
    readonly providerId: string;
    readonly signInMethod: string;
    toJSON(): object;
  }

  export interface ConfirmationResult {
    readonly verificationId: string;
    confirm(verificationCode: string): Promise<UserCredential>;
  }

  export type NextOrObserver<T> = ((value: T) => void | Promise<void>) | { next?: (value: T) => void | Promise<void>; error?: (error: Error) => void; complete?: () => void };

  export function getAuth(app?: FirebaseApp): Auth;
  export function connectAuthEmulator(auth: Auth, url: string, options?: { disableWarnings?: boolean }): void;
  export function signInWithEmailAndPassword(auth: Auth, email: string, password: string): Promise<UserCredential>;
  export function createUserWithEmailAndPassword(auth: Auth, email: string, password: string): Promise<UserCredential>;
  export function sendEmailVerification(user: User): Promise<void>;
  export function sendPasswordResetEmail(auth: Auth, email: string): Promise<void>;
  export function signOut(auth: Auth): Promise<void>;
  export function updateProfile(user: User, profile: { displayName?: string | null; photoURL?: string | null }): Promise<void>;
  export function reload(user: User): Promise<void>;
  export function getIdToken(user: User, forceRefresh?: boolean): Promise<string>;
  export function onAuthStateChanged(auth: Auth, nextOrObserver: NextOrObserver<User | null>, error?: (error: Error) => void, completed?: () => void): () => void;
  export function setPersistence(auth: Auth, persistence: Persistence): Promise<void>;
  export const browserLocalPersistence: Persistence;
  export const browserSessionPersistence: Persistence;
  export const inMemoryPersistence: Persistence;
  export const signInWithPopup: (auth: Auth, provider: AuthProvider) => Promise<UserCredential>;
  export const signInWithRedirect: (auth: Auth, provider: AuthProvider) => Promise<never>;
  export const linkWithCredential: (user: User, credential: AuthCredential) => Promise<UserCredential>;
  export const reauthenticateWithCredential: (user: User, credential: AuthCredential) => Promise<UserCredential>;
  export const fetchSignInMethodsForEmail: (auth: Auth, email: string) => Promise<string[]>;
  export const sendSignInLinkToEmail: (auth: Auth, email: string, actionCodeSettings: ActionCodeSettings) => Promise<void>;
  export const isSignInWithEmailLink: (auth: Auth, emailLink: string) => boolean;
  export const signInWithEmailLink: (auth: Auth, email: string, emailLink: string) => Promise<UserCredential>;
  export const AuthProvider: new (providerId: string) => AuthProvider;
  export const GoogleAuthProvider: { new (): AuthProvider; PROVIDER_ID: string; credential(idToken: string, accessToken?: string): AuthCredential };
  export const FacebookAuthProvider: { new (): AuthProvider; PROVIDER_ID: string; credential(accessToken: string): AuthCredential };
  export const TwitterAuthProvider: { new (): AuthProvider; PROVIDER_ID: string; credential(token: string, secret: string): AuthCredential };
  export const GithubAuthProvider: { new (): AuthProvider; PROVIDER_ID: string; credential(token: string): AuthCredential };
  export const OAuthProvider: { new (providerId: string): AuthProvider; credential(idToken: string, accessToken?: string): AuthCredential };
  export const PhoneAuthProvider: { new (auth: Auth): PhoneAuthProvider; PROVIDER_ID: string; credential(verificationId: string, verificationCode: string): AuthCredential };
  export const RecaptchaVerifier: new (container: string | HTMLElement, parameters: object, auth?: Auth) => ApplicationVerifier;
  export interface ApplicationVerifier {}
  export interface PhoneAuthProvider extends AuthProvider {
    verifyPhoneNumber(phoneNumber: string, appVerifier: ApplicationVerifier): Promise<string>;
  }
  export interface AuthProvider {
    providerId: string;
    setCustomParameters(customParams: object): AuthProvider;
  }
}

// firebase/firestore
declare module 'firebase/firestore' {
  import type { FirebaseApp } from 'firebase/app';

  export interface Firestore {
    readonly app: FirebaseApp;
    readonly _databaseId: unknown;
    readonly _credentials: unknown;
    readonly _queue: unknown;
    readonly _persistenceKey: string;
    readonly _settings: unknown;
    readonly _settingsFrozen: boolean;
    readonly _terminated: boolean;
  }

  export interface Query<T = DocumentData> {
    readonly firestore: Firestore;
    readonly _query: unknown;
    readonly type: 'query' | 'collection' | 'collectionGroup';
    withConverter<U>(converter: FirestoreDataConverter<U>): Query<U>;
  }

  export interface QueryConstraint {
    readonly type: string;
    readonly _apply: (query: unknown) => unknown;
  }

  export interface DocumentData {
    [field: string]: any;
  }

  export interface FirestoreDataConverter<T> {
    toFirestore(model: WithFieldValue<T>): DocumentData;
    fromFirestore(snapshot: QueryDocumentSnapshot): T;
  }

  export interface WithFieldValue<T> extends T {}

  export interface DocumentReference<T = DocumentData> {
    readonly firestore: Firestore;
    readonly path: string;
    readonly id: string;
    readonly parent: CollectionReference<T>;
    withConverter<U>(converter: FirestoreDataConverter<U>): DocumentReference<U>;
  }

  export interface CollectionReference<T = DocumentData> extends Query<T> {
    readonly path: string;
    readonly id: string;
    readonly parent: DocumentReference<DocumentData> | null;
    doc(documentPath?: string): DocumentReference<T>;
    withConverter<U>(converter: FirestoreDataConverter<U>): CollectionReference<U>;
  }

  export class Timestamp {
    readonly seconds: number;
    readonly nanoseconds: number;
    constructor(seconds: number, nanoseconds: number);
    toMillis(): number;
    toDate(): Date;
    static fromMillis(milliseconds: number): Timestamp;
    static fromDate(date: Date): Timestamp;
    static now(): Timestamp;
    isEqual(other: Timestamp): boolean;
    valueOf(): number;
  }

  export interface QueryDocumentSnapshot<T = DocumentData> {
    readonly ref: DocumentReference<T>;
    readonly id: string;
    exists(): true;
    data(): T;
    get(fieldPath: string | FieldPath): unknown;
  }

  export interface DocumentSnapshot<T = DocumentData> {
    readonly ref: DocumentReference<T>;
    readonly id: string;
    exists(): boolean;
    data(): T | undefined;
    get(fieldPath: string | FieldPath): unknown;
  }

  export interface QuerySnapshot<T = DocumentData> {
    readonly docs: QueryDocumentSnapshot<T>[];
    readonly size: number;
    readonly empty: boolean;
    readonly query: Query<T>;
    readonly metadata: { hasPendingWrites: boolean; fromCache: boolean };
    docChanges(): Array<{ type: 'added' | 'removed' | 'modified'; doc: QueryDocumentSnapshot<T>; oldIndex: number; newIndex: number }>;
    forEach(callback: (doc: QueryDocumentSnapshot<T>) => void): void;
  }

  export interface WriteBatch {
    set<T>(documentRef: DocumentReference<T>, data: WithFieldValue<T>, options?: { merge: boolean }): WriteBatch;
    update(documentRef: DocumentReference, data: WithFieldValue<Partial<DocumentData>>): WriteBatch;
    delete(documentRef: DocumentReference): WriteBatch;
    commit(): Promise<void>;
  }

  export interface FieldPath {
    readonly _internalPath: string[];
    constructor(...fieldNames: string[]);
    static documentId(): FieldPath;
    isEqual(other: FieldPath): boolean;
  }

  export type WhereFilterOp = '<' | '<=' | '==' | '!=' | '>=' | '>' | 'array-contains' | 'array-contains-any' | 'in' | 'not-in';

  export function getFirestore(app?: FirebaseApp): Firestore;
  export function connectFirestoreEmulator(firestore: Firestore, host: string, port: number, options?: { disableWarnings?: boolean }): void;
  export function enableIndexedDbPersistence(firestore: Firestore, persistenceSettings?: { forceOwnership?: boolean }): Promise<void>;
  export function collection<T = DocumentData>(firestore: Firestore, path: string, ...pathSegments: string[]): CollectionReference<T>;
  export function doc<T = DocumentData>(firestore: Firestore, path: string, ...pathSegments: string[]): DocumentReference<T>;
  export function query<T = DocumentData>(query: Query<T>, ...constraints: QueryConstraint[]): Query<T>;
  export function where(fieldPath: string | FieldPath, opStr: WhereFilterOp, value: unknown): QueryConstraint;
  export function orderBy(fieldPath: string | FieldPath, directionStr?: 'asc' | 'desc'): QueryConstraint;
  export function limit(limit: number): QueryConstraint;
  export function limitToLast(limit: number): QueryConstraint;
  export function startAt(...snapshotOrFieldValues: (DocumentSnapshot | unknown)[]): QueryConstraint;
  export function startAfter(...snapshotOrFieldValues: (DocumentSnapshot | unknown)[]): QueryConstraint;
  export function endAt(...snapshotOrFieldValues: (DocumentSnapshot | unknown)[]): QueryConstraint;
  export function endBefore(...snapshotOrFieldValues: (DocumentSnapshot | unknown)[]): QueryConstraint;
  export function onSnapshot<T = DocumentData>(query: Query<T>, onNext: (snapshot: QuerySnapshot<T>) => void, onError?: (error: Error) => void, onCompletion?: () => void): () => void;
  export function getDocs<T = DocumentData>(query: Query<T>): Promise<QuerySnapshot<T>>;
  export function addDoc<T = DocumentData>(collection: CollectionReference<T>, data: WithFieldValue<T>): Promise<DocumentReference<T>>;
  export function setDoc<T = DocumentData>(reference: DocumentReference<T>, data: WithFieldValue<T>, options?: { merge: boolean }): Promise<void>;
  export function updateDoc<T = DocumentData>(reference: DocumentReference<T>, data: WithFieldValue<Partial<T>>): Promise<void>;
  export function deleteDoc(reference: DocumentReference): Promise<void>;
  export function writeBatch(firestore?: Firestore): WriteBatch;
  export function runTransaction<T>(firestore: Firestore, updateFunction: (transaction: Transaction) => Promise<T>): Promise<T>;
  export interface Transaction {
    get<T>(documentRef: DocumentReference<T>): Promise<DocumentSnapshot<T>>;
    set<T>(documentRef: DocumentReference<T>, data: WithFieldValue<T>, options?: { merge: boolean }): Transaction;
    update(documentRef: DocumentReference, data: WithFieldValue<Partial<DocumentData>>): Transaction;
    delete(documentRef: DocumentReference): Transaction;
  }
  export function getDoc<T = DocumentData>(reference: DocumentReference<T>): Promise<DocumentSnapshot<T>>;
  export function onSnapshot<T = DocumentData>(reference: DocumentReference<T>, onNext: (snapshot: DocumentSnapshot<T>) => void, onError?: (error: Error) => void, onCompletion?: () => void): () => void;
}