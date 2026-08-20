import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { colors } from '@/theme/colors';

export default function AboutScreen() {
  return (
    <AppBackground>
      <PageHeader title="关于我们" />
      <View style={{ paddingHorizontal: 16 }}>
        <GlassCard>
          <Text style={styles.title}>星帆智联</Text>
          <Text style={styles.p}>连接星空 · 智联未来</Text>
          <Text style={styles.p}>
            星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。当前为 UI 演示版本，暂未对接真实业务接口。
          </Text>
        </GlassCard>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 22, fontWeight: '800' },
  p: { color: colors.muted, marginTop: 10, lineHeight: 22 },
});
