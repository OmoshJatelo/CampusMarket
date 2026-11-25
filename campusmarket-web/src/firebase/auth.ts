import {
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendEmailVerification,
  sendPasswordResetEmail,
  signOut as firebaseSignOut,
  reload,
  getIdToken,
  onAuthStateChanged,
  setPersistence,
  browserLocalPersistence,
} from 'firebase/auth';
import type { User, UserCredential } from 'firebase/auth';
import { auth } from './config';
import { STUDENT_EMAIL_DOMAIN } from '@utils/constants';

export async function initializeAuth() {
  await setPersistence(auth, browserLocalPersistence);
}

export async function signUp(email: string, password: string): Promise<UserCredential> {
  const normalizedEmail = email.trim().toLowerCase();
  if (!isAllowedEmail(normalizedEmail)) {
    throw new Error(`Use your student email (ending in ${STUDENT_EMAIL_DOMAIN})`);
  }
  return createUserWithEmailAndPassword(auth, normalizedEmail, password);
}

export async function signIn(email: string, password: string): Promise<UserCredential> {
  return signInWithEmailAndPassword(auth, email.trim().toLowerCase(), password);
}

export async function signOut(): Promise<void> {
  return firebaseSignOut(auth);
}

export async function sendVerificationEmail(user: User): Promise<void> {
  await sendEmailVerification(user);
}

export async function resendVerificationEmail(user: User): Promise<void> {
  await sendEmailVerification(user);
}

export async function sendResetEmail(email: string): Promise<void> {
  await sendPasswordResetEmail(auth, email.trim().toLowerCase());
}

export async function refreshUser(user: User): Promise<User> {
  await reload(user);
  await getIdToken(user, true);
  return auth.currentUser!;
}

export function onAuthChange(callback: (user: User | null) => void) {
  return onAuthStateChanged(auth, callback);
}

export function getCurrentUser(): User | null {
  return auth.currentUser;
}

export function isAllowedEmail(email: string): boolean {
  if (!STUDENT_EMAIL_DOMAIN) return true;
  const domain = email.split('@')[1];
  return domain === STUDENT_EMAIL_DOMAIN || domain.endsWith(`.${STUDENT_EMAIL_DOMAIN}`);
}

export { auth };