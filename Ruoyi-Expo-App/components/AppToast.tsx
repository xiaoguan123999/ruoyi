import { useEffect, useState } from 'react';
import { Text, YStack } from 'tamagui';

import { setToastHandler } from '@/utils/toast';

export function AppToast() {
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    setToastHandler((next) => {
      setMessage(next);
    });
    return () => setToastHandler(null);
  }, []);

  useEffect(() => {
    if (!message) {
      return;
    }
    const timer = setTimeout(() => setMessage(null), 2400);
    return () => clearTimeout(timer);
  }, [message]);

  if (!message) {
    return null;
  }

  return (
    <YStack
      background="$color12"
      style={{
        position: 'absolute',
        top: 56,
        left: 16,
        right: 16,
        zIndex: 1000,
        paddingVertical: 12,
        paddingHorizontal: 16,
        borderRadius: 12,
        alignItems: 'center',
      }}
    >
      <Text color="$background" style={{ textAlign: 'center' }}>
        {message}
      </Text>
    </YStack>
  );
}
