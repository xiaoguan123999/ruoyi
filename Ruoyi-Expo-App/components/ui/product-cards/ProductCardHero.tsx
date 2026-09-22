import { Image } from 'expo-image';
import { StyleSheet, View } from 'react-native';

import { Text } from '@/components/ui/AppText';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { metricAt, themeTitleColor, type ProductItem } from '@/types/product';
import { colors } from '@/theme/colors';

type Props = { item: ProductItem; onPress?: () => void };

export function ProductCardHero({ item, onPress }: Props) {
  const titleColor = themeTitleColor(item.theme, item.titleTone);
  const m0 = metricAt(item, 0, '每日收益');
  const m1 = metricAt(item, 1, '收益周期', item.cycle || '--');
  const amount = item.mainAmountDisplay || (item.amount > 0 ? `${item.amount} USDT` : `${item.amountCny || '--'} 元`);

  return (
    <View style={styles.card}>
      <View style={styles.coverWrap}>
        <Image source={item.cover} style={styles.cover} contentFit="cover" />
        <View style={styles.coverFade} />
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{item.badgeText || item.tag}</Text>
        </View>
        <View style={styles.heroAmount}>
          <Text style={styles.heroAmountText}>{amount}</Text>
          <Text style={styles.heroAmountHint}>参与金额</Text>
        </View>
      </View>
      <View style={styles.body}>
        <Text style={[styles.name, { color: titleColor }]}>{item.name}</Text>
        <Text style={styles.tagline}>{item.desc}</Text>
        <View style={styles.metaRow}>
          <View style={styles.metaBox}>
            <Text style={styles.metaLabel}>{m0.label}</Text>
            <Text style={styles.metaValue}>{m0.display}</Text>
          </View>
          <View style={styles.metaBox}>
            <Text style={styles.metaLabel}>{m1.label}</Text>
            <Text style={styles.metaValue}>{m1.display}</Text>
          </View>
        </View>
        <PrimaryButton title={item.ctaText || '立即参与'} onPress={onPress} compact />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 18,
    overflow: 'hidden',
    backgroundColor: '#081222',
    borderWidth: 1,
    borderColor: 'rgba(120, 180, 255, 0.32)',
  },
  coverWrap: { position: 'relative' },
  cover: { width: '100%', aspectRatio: 16 / 10 },
  coverFade: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: '55%',
    backgroundColor: 'rgba(5,11,28,0.78)',
  },
  badge: {
    position: 'absolute',
    left: 12,
    top: 12,
    backgroundColor: 'rgba(8, 24, 56, 0.75)',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  badgeText: { color: '#9ECBFF', fontSize: 11, fontWeight: '600' },
  heroAmount: { position: 'absolute', left: 16, bottom: 14 },
  heroAmountText: { color: colors.text, fontSize: 32, fontWeight: '800' },
  heroAmountHint: { color: 'rgba(180,200,230,0.75)', fontSize: 12, marginTop: 2 },
  body: { padding: 16, gap: 10 },
  name: { fontSize: 22, fontWeight: '800' },
  tagline: { color: '#8AA4C6', fontSize: 13, lineHeight: 20 },
  metaRow: { flexDirection: 'row', gap: 10, marginVertical: 4 },
  metaBox: {
    flex: 1,
    backgroundColor: 'rgba(20,40,72,0.7)',
    borderRadius: 10,
    paddingVertical: 10,
    paddingHorizontal: 12,
  },
  metaLabel: { color: 'rgba(180,200,230,0.7)', fontSize: 11 },
  metaValue: { color: colors.text, fontSize: 14, fontWeight: '700', marginTop: 4 },
});
