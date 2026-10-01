import * as Clipboard from 'expo-clipboard';
import { Pressable, StyleSheet } from 'react-native';
import Svg, { Path, Rect } from 'react-native-svg';

import { Text } from '@/components/ui/AppText';
import { toastSuccess, modalWarning } from '@/utils/toast';

type CopyButtonProps = {
  value: string;
  label?: string;
  size?: number;
  color?: string;
  showText?: boolean;
};

const DEFAULT_LABEL = '邀请码';

export function CopyButton({
  value,
  label = DEFAULT_LABEL,
  size = 16,
  color = 'rgba(190, 215, 245, 0.92)',
  showText = false,
}: CopyButtonProps) {
  const onCopy = async () => {
    const text = value.trim();
    if (!text || text === '--') {
      modalWarning(`暂无${label}`);
      return;
    }
    await Clipboard.setStringAsync(text);
    toastSuccess(`${label}已复制`);
  };

  return (
    <Pressable
      onPress={onCopy}
      hitSlop={10}
      accessibilityRole="button"
      accessibilityLabel={`复制${label}`}
      style={({ pressed }) => [styles.hit, showText && styles.chip, pressed && styles.pressed]}
    >
      <Svg width={size} height={size} viewBox="0 0 24 24" fill="none">
        <Rect x="9" y="9" width="13" height="13" rx="2" stroke={color} strokeWidth={1.8} />
        <Path
          d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
          stroke={color}
          strokeWidth={1.8}
        />
      </Svg>
      {showText ? <Text style={[styles.text, { color }]}>复制</Text> : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  hit: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  chip: {
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: 'rgba(140, 190, 255, 0.45)',
    backgroundColor: 'rgba(40, 80, 140, 0.35)',
  },
  text: {
    fontSize: 12,
    fontWeight: '600',
  },
  pressed: {
    opacity: 0.7,
  },
});
