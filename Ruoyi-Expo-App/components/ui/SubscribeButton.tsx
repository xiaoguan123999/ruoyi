import { Pressable, StyleSheet, Text } from 'react-native';

type Variant = 'cny' | 'usdt';

type Props = {
  title: string;
  variant: Variant;
  onPress?: () => void;
};

const VARIANT_STYLES: Record<Variant, { bg: string; text: string; border: string }> = {
  cny: {
    bg: '#E0B060',
    text: '#1A1208',
    border: 'rgba(255, 228, 170, 0.45)',
  },
  usdt: {
    bg: '#45D483',
    text: '#062012',
    border: 'rgba(180, 255, 210, 0.35)',
  },
};

export function SubscribeButton({ title, variant, onPress }: Props) {
  const palette = VARIANT_STYLES[variant];

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.btn,
        {
          backgroundColor: palette.bg,
          borderColor: palette.border,
        },
        pressed && styles.pressed,
      ]}
    >
      <Text style={[styles.text, { color: palette.text }]}>{title}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  btn: {
    height: 50,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
  },
  pressed: {
    opacity: 0.9,
  },
  text: {
    fontSize: 17,
    fontWeight: '800',
    letterSpacing: 1,
  },
});
