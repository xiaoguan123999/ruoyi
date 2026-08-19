import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { colors } from '@/theme/colors';

export default function NoticeScreen() {
  return (
    <AppBackground>
      <PageHeader title="公告" />
      <View style={{ paddingHorizontal: 16 }}>
        <GlassCard>
          <Text style={styles.title}>这是一条公告</Text>
          <Text style={styles.p}>星帆智联平台正在进行 UI 演示，后续将对接真实公告接口。</Text>
        </GlassCard>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 18, fontWeight: '700' },
  p: { color: colors.muted, marginTop: 10, lineHeight: 22 },
});
