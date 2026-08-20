import { useEffect, useState } from 'react';

import { getAuthSnapshot, subscribeAuth } from '@/api/auth-state';
import { getToken } from '@/utils/storage';

export function useAuth() {
  const [snapshot, setSnapshot] = useState(getAuthSnapshot);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    let alive = true;
    void getToken().then(() => {
      if (!alive) {
        return;
      }
      setSnapshot(getAuthSnapshot());
      setHydrated(true);
    });
    const unsubscribe = subscribeAuth(() => {
      setSnapshot(getAuthSnapshot());
    });
    return () => {
      alive = false;
      unsubscribe();
    };
  }, []);

  return {
    ...snapshot,
    hydrated,
    isLoggedIn: Boolean(snapshot.token),
  };
}
