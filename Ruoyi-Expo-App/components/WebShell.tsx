import { useEffect } from 'react';
import { Platform } from 'react-native';
import { YStack } from 'tamagui';

const WEB_INPUT_RESET_STYLE_ID = 'ruoyi-web-input-focus-reset';

const WEB_INPUT_RESET_CSS = `
input, textarea, [contenteditable="true"] {
  outline: none !important;
  box-shadow: none !important;
  -webkit-tap-highlight-color: transparent;
}
input:focus, textarea:focus, [contenteditable="true"]:focus,
input:focus-visible, textarea:focus-visible {
  outline: none !important;
  box-shadow: none !important;
}
`;

function ensureWebInputFocusReset() {
  if (typeof document === 'undefined') {
    return;
  }
  if (document.getElementById(WEB_INPUT_RESET_STYLE_ID)) {
    return;
  }
  const style = document.createElement('style');
  style.id = WEB_INPUT_RESET_STYLE_ID;
  style.textContent = WEB_INPUT_RESET_CSS;
  document.head.appendChild(style);
}

export function WebShell({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    if (Platform.OS === 'web') {
      ensureWebInputFocusReset();
    }
  }, []);

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
