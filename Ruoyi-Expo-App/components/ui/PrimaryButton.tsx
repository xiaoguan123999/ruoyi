import { Pressable, StyleSheet, Text } from 'react-native';

type Props = {
  title: string;
  onPress?: () => void;
  disabled?: boolean;
  compact?: boolean;
};

export function PrimaryButton({ title, onPress, disabled, compact }: Props) {
  return (
    <Pressable
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [styles.btn, pressed && styles.pressed, disabled && styles.disabled]}
    >
      <Text style={[styles.text, compact && styles.textCompact]}>{title}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  btn: {
    height: 46,
    borderRadius: 8,
    backgroundColor: '#2F7BFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressed: { opacity: 0.88 },
  disabled: { opacity: 0.5 },
  text: {
    color: '#FFFFFF',
    fontSize: 17,
    fontWeight: '700',
    letterSpacing: 10,
  },
  textCompact: {
    letterSpacing: 2,
    fontSize: 16,
  },
});
