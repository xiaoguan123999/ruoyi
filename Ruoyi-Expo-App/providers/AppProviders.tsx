import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Platform } from 'react-native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { KeyboardProvider } from 'react-native-keyboard-controller';
import { SafeAreaProvider, initialWindowMetrics } from 'react-native-safe-area-context';
import { TamaguiProvider, Theme } from 'tamagui';

import { AppToast } from '@/components/AppToast';
import { WebShell } from '@/components/WebShell';
import '@/i18n';
import { tamaguiConfig } from '@/tamagui.config';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function KeyboardGate({ children }: { children: React.ReactNode }) {
  if (Platform.OS === 'web') {
    return children;
  }
  return <KeyboardProvider>{children}</KeyboardProvider>;
}

export function AppProviders({ children }: { children: React.ReactNode }) {
  const theme = 'dark';

  return (
    <SafeAreaProvider initialMetrics={initialWindowMetrics}>
      <GestureHandlerRootView style={{ flex: 1, position: 'relative' }}>
        <KeyboardGate>
          <QueryClientProvider client={queryClient}>
            <TamaguiProvider config={tamaguiConfig} defaultTheme={theme}>
              <Theme name={theme}>
                <WebShell>
                  {children}
                  <AppToast />
                </WebShell>
              </Theme>
            </TamaguiProvider>
          </QueryClientProvider>
        </KeyboardGate>
      </GestureHandlerRootView>
    </SafeAreaProvider>
  );
}
