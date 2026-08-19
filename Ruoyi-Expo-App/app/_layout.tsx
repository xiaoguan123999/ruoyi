import 'react-native-gesture-handler';
import 'react-native-get-random-values';
import 'react-native-reanimated';

import { Stack, useRouter, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import { useEffect } from 'react';

import { useAuth } from '@/hooks/useAuth';
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
    if (!hydrated) {
      return;
    }
    const root = segments[0];
    const inTabs = root === '(tabs)';
    if (!isLoggedIn && inTabs) {
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
  return (
    <AppProviders>
      <StatusBar style="light" />
      <AuthGate>
        <Stack screenOptions={{ headerShown: false }}>
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
