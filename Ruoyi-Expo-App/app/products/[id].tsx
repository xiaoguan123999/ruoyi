import { useLocalSearchParams } from 'expo-router';
import { Image } from 'expo-image';
import { ScrollView, StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { mockProducts } from '@/constants/mock';
import { colors } from '@/theme/colors';
import { toast } from '@/utils/toast';

export default function ProductDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const item = mockProducts.find((p) => p.id === id) ?? mockProducts[0];

  return (
    <AppBackground>
      <PageHeader title={`「${item.name}」`} />
      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 32 }}>
        <GlassCard style={{ padding: 0, overflow: 'hidden' }}>
          <Image source={item.cover} style={styles.cover} contentFit="cover" />
          <View style={styles.badge}>
            <Text style={styles.badgeText}>{item.tag}</Text>
          </View>
          <View style={{ padding: 16 }}>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={styles.en}>{item.enName}</Text>
            <View style={styles.amountRow}>
              <Text style={styles.amount}>{item.amount}</Text>
              <Text style={styles.amountLabel}>参与金额 / USDT</Text>
            </View>
            <Text style={styles.desc}>{item.desc}</Text>
            <View style={styles.meta}>
              <Text style={styles.metaText}>每日收益 {item.daily} USDT</Text>
              <Text style={styles.metaText}>收益周期 {item.cycle}</Text>
            </View>
            <PrimaryButton title="立即参与" onPress={() => toast('演示环境，暂不提交订单')} />
          </View>
        </GlassCard>
      </ScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  cover: { width: '100%', height: 180 },
  badge: {
    position: 'absolute',
    right: 12,
    top: 12,
    backgroundColor: 'rgba(10,24,56,0.82)',
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: { color: '#9ECBFF', fontSize: 11 },
  name: { color: '#9ECBFF', fontSize: 22, fontWeight: '800' },
  en: { color: colors.muted, marginTop: 4 },
  amountRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 8, marginTop: 12 },
  amount: { color: colors.text, fontSize: 32, fontWeight: '800' },
  amountLabel: { color: colors.muted, marginBottom: 4 },
  desc: { color: colors.muted, marginVertical: 12 },
  meta: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 16 },
  metaText: { color: colors.text, fontSize: 13 },
});
