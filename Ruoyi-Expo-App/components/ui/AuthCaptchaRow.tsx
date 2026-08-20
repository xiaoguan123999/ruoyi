import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

type Props = {
  value: string;
  onChangeText: (text: string) => void;
  captchaUri: string;
  onRefresh: () => void;
};

export function AuthCaptchaRow({ value, onChangeText, captchaUri, onRefresh }: Props) {
  return (
    <View style={styles.wrap}>
      <View style={styles.field}>
        <Image source={images.iconCaptcha} style={styles.icon} contentFit="contain" />
        <TextInput
          value={value}
          onChangeText={onChangeText}
          placeholder="请输入验证码"
          placeholderTextColor={colors.placeholder}
          keyboardType="number-pad"
          autoCapitalize="none"
          autoCorrect={false}
          autoComplete="off"
          textContentType="none"
          underlineColorAndroid="transparent"
          selectionColor="#8BB8FF"
          style={styles.input}
        />
      </View>
      <Pressable onPress={onRefresh} accessibilityLabel="点击刷新验证码" style={styles.captchaBox}>
        {captchaUri ? (
          <Image source={{ uri: captchaUri }} style={styles.captcha} contentFit="contain" />
        ) : (
          <Text style={styles.captchaHint}>刷新</Text>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  field: {
    flex: 1,
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
  captchaBox: {
    width: 102,
    height: 46,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  captcha: {
    width: 90,
    height: 36,
  },
  captchaHint: {
    color: colors.muted,
    fontSize: 12,
  },
});
