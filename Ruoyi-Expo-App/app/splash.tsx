import { useRouter } from 'expo-router';
import { useEffect } from 'react';
import { View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { images } from '@/constants/images';
import { getToken } from '@/utils/storage';

export default function SplashScreen() {
  const router = useRouter();

  useEffect(() => {
    let cancelled = false;
    const run = async () => {
      await new Promise((r) => setTimeout(r, 700));
      const token = await getToken();
      if (cancelled) {
        return;
      }
      router.replace(token ? '/(tabs)' : '/sign-in');
    };
    void run();
    return () => {
      cancelled = true;
    };
  }, [router]);

  return (
    <AppBackground source={images.loginBg} dim={false}>
      <View style={{ flex: 1 }} />
    </AppBackground>
  );
}
