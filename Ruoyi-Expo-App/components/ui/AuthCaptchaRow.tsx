import { Image } from 'expo-image';
import { Pressable, StyleSheet, View } from 'react-native';

import { Text, TextInput } from '@/components/ui/AppText';
import { useAuthMetrics } from '@/components/ui/AuthScreen';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

type Props = {
  value: string;
  onChangeText: (text: string) => void;
  /** 接口返回的明文验证码 */
  captchaText: string;
  onRefresh: () => void;
};

export function AuthCaptchaRow({ value, onChangeText, captchaText, onRefresh }: Props) {
  const { rowHeight, fontSize, iconSize } = useAuthMetrics();
  const captchaW = Math.round(rowHeight * 2.2);

  return (
    <View style={[styles.wrap, { gap: 10 }]}>
      <View style={[styles.field, { minHeight: rowHeight }]}>
        <Image source={images.iconCaptcha} style={{ width: iconSize, height: iconSize }} contentFit="contain" />
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
          style={[styles.input, { fontSize }]}
        />
      </View>
      <Pressable
        onPress={onRefresh}
        accessibilityLabel="点击刷新验证码"
        style={[styles.captchaBox, { width: captchaW, minHeight: rowHeight }]}
      >
        {captchaText ? (
          <Text
            adjustsFontSizeToFit
            minimumFontScale={0.7}
            numberOfLines={1}
            style={[styles.captchaText, { fontSize: Math.max(15, fontSize) }]}
          >
            {captchaText}
          </Text>
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
  },
  field: {
    flex: 1,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 6,
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
    ...({ outlineStyle: 'none', outlineWidth: 0 } as object),
  },
  captchaBox: {
    borderRadius: 6,
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
    paddingHorizontal: 6,
    paddingVertical: 6,
  },
  captchaText: {
    color: colors.text,
    fontWeight: '700',
    letterSpacing: 1,
  },
  captchaHint: {
    color: colors.muted,
    fontSize: 12,
  },
});
