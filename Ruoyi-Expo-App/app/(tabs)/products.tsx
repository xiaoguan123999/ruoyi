import { useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { images } from '@/constants/images';
import { mockProducts } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function ProductsScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  return (
    <AppBackground>
      <ScrollView contentContainerStyle={{ paddingTop: insets.top + 8, paddingBottom: 24 }}>
        <View style={styles.hero}>
          <Text style={styles.heroTitle}>连接星空 智联未来</Text>
          <Text style={styles.heroSub}>以科技连接万物 · 让星辰触手可及</Text>
        </View>
        <Pressable onPress={() => router.push('/products/dawn-1')} style={{ marginHorizontal: 16 }}>
          <GlassCard style={{ padding: 0, overflow: 'hidden' }}>
            <Image source={images.productHero} style={styles.heroImg} contentFit="cover" />
            <View style={styles.heroFooter}>
              <Text style={styles.plan}>「星帆·天启计划」</Text>
              <Text style={styles.more}>了解详情 ›</Text>
            </View>
          </GlassCard>
        </Pressable>

        <View style={{ paddingHorizontal: 16, marginTop: 16, gap: 14 }}>
          {mockProducts.map((item) => (
            <GlassCard key={item.id} style={{ padding: 0, overflow: 'hidden' }}>
              <View>
                <Image source={item.cover} style={styles.cover} contentFit="cover" />
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>{item.tag}</Text>
                </View>
              </View>
              <View style={{ padding: 14 }}>
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
                <PrimaryButton title="立即参与" onPress={() => router.push(`/products/${item.id}`)} />
              </View>
            </GlassCard>
          ))}
        </View>
      </ScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  hero: { paddingHorizontal: 16, paddingBottom: 12 },
  heroTitle: { color: colors.text, fontSize: 24, fontWeight: '800' },
  heroSub: { color: colors.muted, marginTop: 6, fontSize: 13 },
  heroImg: { width: '100%', height: 140 },
  heroFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  plan: { color: colors.text, fontWeight: '700' },
  more: { color: colors.muted, fontSize: 13 },
  cover: { width: '100%', height: 150 },
  badge: {
    position: 'absolute',
    right: 10,
    top: 10,
    backgroundColor: 'rgba(10,24,56,0.82)',
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: { color: '#9ECBFF', fontSize: 11 },
  name: { color: '#9ECBFF', fontSize: 20, fontWeight: '800' },
  en: { color: colors.muted, marginTop: 2, fontSize: 12 },
  amountRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 8, marginTop: 10 },
  amount: { color: colors.text, fontSize: 28, fontWeight: '800' },
  amountLabel: { color: colors.muted, marginBottom: 4, fontSize: 12 },
  desc: { color: colors.muted, marginVertical: 10, fontSize: 13 },
  meta: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 12 },
  metaText: { color: colors.text, fontSize: 12 },
});
