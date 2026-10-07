import { Stack, useRouter } from 'expo-router';
import { StyleSheet, View } from 'react-native';

import { Text } from '@/components/ui/AppText';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';

export default function NotFoundScreen() {
  const router = useRouter();
  const goHome = () => {
    router.replace('/');
  };

  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <AppBackground>
        <PageHeader title="页面不存在" onBack={goHome} />
        <View style={styles.body}>
          <View style={styles.mark}>
            <Text style={styles.markText}>404</Text>
          </View>
          <Text style={styles.title}>没有找到这个页面</Text>
          <Text style={styles.hint}>链接可能已失效，或地址输入有误</Text>
          <View style={styles.action}>
            <PrimaryButton title="返回首页" compact onPress={goHome} />
          </View>
        </View>
      </AppBackground>
    </>
  );
}

const styles = StyleSheet.create({
  body: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
    paddingBottom: 72,
  },
  mark: {
    width: 88,
    height: 88,
    borderRadius: 44,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.35)',
    marginBottom: 18,
  },
  markText: {
    color: colors.gold,
    fontSize: 28,
    fontWeight: '700',
  },
  title: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '600',
  },
  hint: {
    marginTop: 8,
    color: colors.muted,
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 20,
  },
  action: {
    marginTop: 28,
    width: 200,
  },
});
