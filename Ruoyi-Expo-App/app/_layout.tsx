import 'react-native-gesture-handler';
import 'react-native-get-random-values';
import 'react-native-reanimated';

import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import * as ScreenOrientation from 'expo-screen-orientation';
import { useEffect } from 'react';
import { Platform } from 'react-native';

import { isPublicAuthRoute, setCurrentAuthSegments } from '@/api/auth-state';
import { useAuth } from '@/hooks/useAuth';
import { AppUpdateGate } from '@/components/ui/AppUpdateGate';
import { AppProviders } from '@/providers/AppProviders';

export { ErrorBoundary } from 'expo-router';

export const unstable_settings = {
  initialRouteName: 'index',
};

SplashScreen.preventAutoHideAsync();

function AuthGate({ children }: { children: React.ReactNode }) {
  const { hydrated, isLoggedIn } = useAuth();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (!hydrated) {
      return;
    }
    void SplashScreen.hideAsync();
  }, [hydrated]);

  useEffect(() => {
    setCurrentAuthSegments(segments as string[]);
  }, [segments]);

  useEffect(() => {
    if (!hydrated) {
      return;
    }
    const root = segments[0];
    const publicRoute = isPublicAuthRoute(segments);
    if (!isLoggedIn && !publicRoute) {
      router.replace('/sign-in');
      return;
    }
    if (isLoggedIn && (root === 'sign-in' || root === 'sign-up')) {
      router.replace('/(tabs)');
    }
  }, [hydrated, isLoggedIn, router, segments]);

  if (!hydrated) {
    return null;
  }

  return children;
}

export default function RootLayout() {
  useEffect(() => {
    if (Platform.OS === 'web') {
      return;
    }
    void ScreenOrientation.lockAsync(ScreenOrientation.OrientationLock.PORTRAIT_UP);
  }, []);

  return (
    <AppProviders>
      <StatusBar style="light" />
      <AuthGate>
        <AppUpdateGate />
        <Stack
          screenOptions={{
            headerShown: false,
            animation: Platform.OS === 'android' ? 'fade' : 'slide_from_right',
            animationDuration: 180,
            contentStyle: { backgroundColor: '#050B1C' },
          }}
        >
          <Stack.Screen name="index" />
          <Stack.Screen name="splash" />
          <Stack.Screen name="sign-in" />
          <Stack.Screen name="sign-up" />
          <Stack.Screen name="(tabs)" />
        </Stack>
      </AuthGate>
    </AppProviders>
  );
}
