import { router } from 'expo-router';

import type { RuoyiUser } from '@/api/types';
import { getTokenSync, removeToken } from '@/utils/storage';
import { toastThenNavigate } from '@/utils/toast';

type AuthSnapshot = {
  token: string | null;
  user: RuoyiUser | null;
  roles: string[];
  permissions: string[];
};

type Listener = () => void;

const PUBLIC_AUTH_SEGMENTS = new Set(['sign-in', 'sign-up', 'splash', 'service', 'service-chat']);

const listeners = new Set<Listener>();

let user: RuoyiUser | null = null;
let roles: string[] = [];
let permissions: string[] = [];
let ignoreUnauthorizedUntil = 0;
let redirecting = false;
let currentSegments: string[] = [];

export function setCurrentAuthSegments(segments: string[]): void {
  currentSegments = segments;
}

export function subscribeAuth(listener: Listener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function notifyAuthChanged(): void {
  for (const listener of listeners) {
    listener();
  }
}

export function getAuthSnapshot(): AuthSnapshot {
  return {
    token: getTokenSync(),
    user,
    roles,
    permissions,
  };
}

export function setCurrentUser(
  nextUser: RuoyiUser | null,
  nextRoles: string[] = [],
  nextPermissions: string[] = [],
): void {
  user = nextUser;
  roles = nextRoles;
  permissions = nextPermissions;
  notifyAuthChanged();
}

export function clearCurrentUser(): void {
  setCurrentUser(null, [], []);
}

export function markAuthenticated(): void {
  ignoreUnauthorizedUntil = Date.now() + 3000;
  notifyAuthChanged();
}

export function isPublicAuthRoute(segments: string[]): boolean {
  const root = segments[0];
  if (!root || root === 'index') {
    return true;
  }
  return PUBLIC_AUTH_SEGMENTS.has(root);
}

export async function handleUnauthorized(
  message = '登录已过期，请重新登录',
): Promise<void> {
  if (Date.now() < ignoreUnauthorizedUntil) {
    return;
  }
  if (isPublicAuthRoute(currentSegments)) {
    return;
  }
  if (redirecting) {
    return;
  }
  redirecting = true;
  try {
    await removeToken();
    clearCurrentUser();
    toastThenNavigate(
      message || '登录已过期，请重新登录',
      () => {
        router.replace('/sign-in');
        redirecting = false;
      },
      { type: 'warning', presentation: 'toast', duration: 2800 },
    );
  } catch {
    redirecting = false;
  }
}
