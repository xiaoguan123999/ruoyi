import { Platform } from 'react-native';
import { YStack } from 'tamagui';

export function WebShell({ children }: { children: React.ReactNode }) {
  if (Platform.OS !== 'web') {
    return children;
  }

  return (
    <YStack
      style={{
        flex: 1,
        width: '100%',
        maxWidth: 480,
        alignSelf: 'center',
        minHeight: '100dvh' as unknown as number,
        backgroundColor: '#050B1C',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {children}
    </YStack>
  );
}
