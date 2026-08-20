import { useRouter } from 'expo-router';
import { useEffect } from 'react';
import { View } from 'react-native';

import { fetchAppProfile } from '@/api/app-auth';
import { AppBackground } from '@/components/ui/AppBackground';
import { images } from '@/constants/images';
import { getToken, removeToken } from '@/utils/storage';
import { toastThenNavigate } from '@/utils/toast';

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
      if (!token) {
        router.replace('/sign-in');
        return;
      }
      try {
        await fetchAppProfile();
        if (!cancelled) {
          router.replace('/(tabs)');
        }
      } catch {
        await removeToken();
        if (!cancelled) {
          toastThenNavigate('登录已失效，请重新登录', () => router.replace('/sign-in'), {
            type: 'warning',
          });
        }
      }
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
