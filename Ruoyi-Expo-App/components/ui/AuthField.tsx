import type { ImageSource } from 'expo-image';
import { Image } from 'expo-image';
import { StyleSheet, TextInput, View } from 'react-native';

import { colors } from '@/theme/colors';

type Props = {
  icon: ImageSource;
  placeholder: string;
  value: string;
  onChangeText: (text: string) => void;
  secureTextEntry?: boolean;
  keyboardType?: 'default' | 'phone-pad' | 'number-pad';
};

export function AuthField({
  icon,
  placeholder,
  value,
  onChangeText,
  secureTextEntry,
  keyboardType = 'default',
}: Props) {
  return (
    <View style={styles.row}>
      <Image source={icon} style={styles.icon} contentFit="contain" />
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor="rgba(170, 198, 230, 0.55)"
        secureTextEntry={secureTextEntry}
        keyboardType={keyboardType}
        autoCapitalize="none"
        autoCorrect={false}
        style={styles.input}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    height: 46,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(140, 190, 255, 0.45)',
    backgroundColor: 'rgba(6, 22, 56, 0.42)',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    gap: 10,
  },
  icon: { width: 18, height: 18 },
  input: { flex: 1, color: colors.text, fontSize: 14, paddingVertical: 0 },
});
