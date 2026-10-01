import * as Clipboard from 'expo-clipboard';
import { Pressable, StyleSheet } from 'react-native';
import Svg, { Path, Rect } from 'react-native-svg';

import { toastSuccess, modalWarning } from '@/utils/toast';

type CopyButtonProps = {
  value: string;
  label?: string;
  size?: number;
  color?: string;
};

const DEFAULT_LABEL = '\u9080\u8bf7\u7801';

export function CopyButton({
  value,
  label = DEFAULT_LABEL,
  size = 16,
  color = 'rgba(190, 215, 245, 0.92)',
}: CopyButtonProps) {
  const onCopy = async () => {
    const text = value.trim();
    if (!text || text === '--') {
      modalWarning(`\u6682\u65e0${label}`);
      return;
    }
    await Clipboard.setStringAsync(text);
    toastSuccess(`${label}\u5df2\u590d\u5236`);
  };

  return (
    <Pressable
      onPress={onCopy}
      hitSlop={10}
      accessibilityRole="button"
      accessibilityLabel={`\u590d\u5236${label}`}
      style={({ pressed }) => [styles.hit, pressed && styles.pressed]}
    >
      <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
        <Rect x="9" y="9" width="13" height="13" rx="2" stroke={color} strokeWidth={1.8} />
        <Path
          d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
          stroke={color}
          strokeWidth={1.8}
        />
      </Svg>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  hit: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: {
    opacity: 0.7,
  },
});
