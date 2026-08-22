import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalWarning } from '@/utils/toast';

export default function ServiceScreen() {
  return (
    <AppBackground>
      <PageHeader title="客服中心" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 28 }}
        showsVerticalScrollIndicator={false}
        onRefresh={async () => {}}
      >
        <GlassCard>
          <Text style={styles.title}>在线客服</Text>
          <Text style={styles.p}>工作时间 09:00 - 21:00</Text>
          <View style={{ marginTop: 16 }}>
            <PrimaryButton title="联系客服" onPress={() => modalWarning('客服功能开发中')} />
          </View>
        </GlassCard>
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 18, fontWeight: '700' },
  p: { color: colors.muted, marginTop: 10, lineHeight: 22 },
});
