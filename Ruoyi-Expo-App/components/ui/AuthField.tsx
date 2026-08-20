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
        placeholderTextColor={colors.placeholder}
        secureTextEntry={secureTextEntry}
        keyboardType={keyboardType}
        autoCapitalize="none"
        autoCorrect={false}
        autoComplete="off"
        textContentType="none"
        underlineColorAndroid="transparent"
        selectionColor="#8BB8FF"
        style={styles.input}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    height: 46,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    gap: 10,
  },
  icon: { width: 18, height: 18 },
  input: {
    flex: 1,
    color: colors.text,
    fontSize: 14,
    paddingVertical: 0,
    paddingHorizontal: 0,
    margin: 0,
    borderWidth: 0,
    backgroundColor: 'transparent',
  },
});
