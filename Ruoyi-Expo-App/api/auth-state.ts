import { router } from 'expo-router';

import type { RuoyiUser } from '@/api/types';
import { getTokenSync, removeToken } from '@/utils/storage';

type AuthSnapshot = {
  token: string | null;
  user: RuoyiUser | null;
  roles: string[];
  permissions: string[];
};

type Listener = () => void;

const listeners = new Set<Listener>();

let user: RuoyiUser | null = null;
let roles: string[] = [];
let permissions: string[] = [];
let ignoreUnauthorizedUntil = 0;
let redirecting = false;

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

export async function handleUnauthorized(): Promise<void> {
  if (Date.now() < ignoreUnauthorizedUntil) {
    return;
  }
  if (redirecting) {
    return;
  }
  redirecting = true;
  try {
    await removeToken();
    clearCurrentUser();
    router.replace('/sign-in');
  } finally {
    redirecting = false;
  }
}
