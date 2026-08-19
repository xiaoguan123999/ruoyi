import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import { Platform } from 'react-native';

import { getToken } from '@/utils/storage';

export default function Index() {
  const [ready, setReady] = useState(false);
  const [hasToken, setHasToken] = useState(false);

  useEffect(() => {
    void getToken().then((token) => {
      setHasToken(Boolean(token));
      setReady(true);
    });
  }, []);

  if (!ready) {
    return null;
  }

  if (Platform.OS !== 'web') {
    return <Redirect href="/splash" />;
  }

  return <Redirect href={hasToken ? '/(tabs)' : '/sign-in'} />;
}
