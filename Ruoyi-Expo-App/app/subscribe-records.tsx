import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { mockRecords } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function SubscribeRecordsScreen() {
  return (
    <AppBackground>
      <PageHeader title="认购记录" />
      <View style={{ paddingHorizontal: 16, gap: 10 }}>
        {mockRecords.map((item) => (
          <GlassCard key={item.id}>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.meta}>金额 {item.amount} USDT</Text>
            <Text style={styles.meta}>{item.time}</Text>
            <Text style={styles.status}>{item.status}</Text>
          </GlassCard>
        ))}
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  name: { color: colors.text, fontSize: 16, fontWeight: '700' },
  meta: { color: colors.muted, marginTop: 6 },
  status: { color: colors.success, marginTop: 8 },
});
