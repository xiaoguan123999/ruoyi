import type { ImageSource } from 'expo-image';
import { Image } from 'expo-image';
import { StyleSheet, TextInput, View } from 'react-native';

import { useAuthMetrics } from '@/components/ui/AuthScreen';
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
  const { rowHeight, fontSize, iconSize } = useAuthMetrics();

  return (
    <View style={[styles.row, { height: rowHeight }]}>
      <Image source={icon} style={{ width: iconSize, height: iconSize }} contentFit="contain" />
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
        style={[styles.input, { fontSize }]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    gap: 10,
  },
  input: {
    flex: 1,
    color: colors.text,
    paddingVertical: 0,
    paddingHorizontal: 0,
    margin: 0,
    borderWidth: 0,
    backgroundColor: 'transparent',
    // RN Web：去掉浏览器默认 focus 描边（+html 的 CSS 在 metro web 常不生效）
    outlineStyle: 'none',
    outlineWidth: 0,
  },
});
