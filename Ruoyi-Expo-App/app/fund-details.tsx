import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { mockFunds } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function FundDetailsScreen() {
  return (
    <AppBackground>
      <PageHeader title="资金明细" />
      <View style={{ paddingHorizontal: 16, gap: 10 }}>
        {mockFunds.map((item) => (
          <GlassCard key={item.id}>
            <View style={styles.row}>
              <View>
                <Text style={styles.title}>{item.title}</Text>
                <Text style={styles.time}>{item.time}</Text>
              </View>
              <Text style={[styles.amount, item.type === 'in' ? styles.in : styles.out]}>{item.amount}</Text>
            </View>
          </GlassCard>
        ))}
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  title: { color: colors.text, fontSize: 15, fontWeight: '600' },
  time: { color: colors.muted, marginTop: 6, fontSize: 12 },
  amount: { fontSize: 18, fontWeight: '800' },
  in: { color: colors.success },
  out: { color: colors.danger },
});
