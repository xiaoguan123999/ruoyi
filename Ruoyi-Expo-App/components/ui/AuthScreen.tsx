import { Image } from 'expo-image';
import {
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  StyleSheet,
  useWindowDimensions,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

type Props = {
  children: React.ReactNode;
  /** 表单从屏幕高度的该比例处开始，随手机尺寸变化 */
  formStart?: number;
};

export function AuthScreen({ children, formStart = 0.45 }: Props) {
  const insets = useSafeAreaInsets();
  const { width, height } = useWindowDimensions();
  const padX = Math.min(40, Math.max(24, Math.round(width * 0.085)));
  const topGap = Math.round(height * formStart);

  return (
    <View style={styles.root}>
      <Image
        source={images.loginBg}
        style={StyleSheet.absoluteFill}
        contentFit="cover"
        contentPosition="top"
      />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
          contentContainerStyle={{
            paddingHorizontal: padX,
            paddingTop: topGap,
            paddingBottom: Math.max(insets.bottom, 28),
            flexGrow: 1,
          }}
        >
          <View style={styles.form}>{children}</View>
        </ScrollView>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  flex: {
    flex: 1,
  },
  form: {
    gap: 12,
  },
});
