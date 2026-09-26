import { Image } from 'expo-image';
import { StyleSheet, View } from 'react-native';

import { Text } from '@/components/ui/AppText';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { ProductCycleIcon, ProductDailyIcon } from '@/components/ui/ProductStatIcons';
import { metricAt, themeTitleColor, type ProductItem } from '@/types/product';
import { colors } from '@/theme/colors';

type Props = {
  item: ProductItem;
  onPress?: () => void;
};

export function ProductCardClassic({ item, onPress }: Props) {
  const titleColor = themeTitleColor(item.theme);
  const m0 = metricAt(item, 0, '每日收益', item.daily > 0 ? `${item.daily} USDT` : item.dailyCny > 0 ? `${item.dailyCny} RMB` : '--');
  const m1 = metricAt(item, 1, '收益周期', item.cycle || '--');
  const amountText =
    item.mainAmountDisplay ||
    (item.amount > 0 ? String(item.amount) : item.amountCny > 0 ? String(item.amountCny) : '--');
  const unitHint = item.mainAmountDisplay
    ? ''
    : item.amount > 0
      ? 'USDT'
      : item.amountCny > 0
        ? 'RMB'
        : '';

  return (
    <View style={styles.card}>
      <View style={styles.coverWrap}>
        <Image source={item.cover} style={styles.cover} contentFit="cover" />
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{item.badgeText || item.tag}</Text>
        </View>
      </View>

      <View style={styles.body}>
        <Text style={[styles.name, { color: titleColor }]}>{item.name}</Text>
        <Text style={styles.en}>{item.enName}</Text>
        <View style={styles.divider} />

        <View style={styles.amountRow}>
          <Text style={styles.amount}>{amountText}</Text>
          {unitHint ? (
            <View style={styles.amountLabelWrap}>
              <Text style={styles.amountLabel}>参与金额</Text>
              <Text style={styles.amountUnit}>/{unitHint}</Text>
            </View>
          ) : (
            <View style={styles.amountLabelWrap}>
              <Text style={styles.amountLabel}>参与金额</Text>
            </View>
          )}
        </View>

        <Text style={styles.desc}>{item.desc}</Text>

        <View style={styles.metaRow}>
          <View style={styles.metaItem}>
            <ProductDailyIcon size={44} />
            <Text style={styles.metaText}>
              {m0.label} <Text style={styles.metaValue}>{m0.display}</Text>
            </Text>
          </View>
          <View style={styles.metaItem}>
            <ProductCycleIcon size={44} />
            <Text style={styles.metaText}>
              {m1.label} <Text style={styles.metaValue}>{m1.display}</Text>
            </Text>
          </View>
        </View>

        <PrimaryButton title={item.ctaText || '立即参与'} onPress={onPress} compact />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    overflow: 'hidden',
    backgroundColor: '#0A1528',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
  },
  coverWrap: { position: 'relative' },
  cover: { width: '100%', aspectRatio: 16 / 9 },
  badge: {
    position: 'absolute',
    right: 10,
    top: 10,
    backgroundColor: 'rgba(8, 24, 56, 0.82)',
    borderRadius: 14,
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderWidth: 1,
    borderColor: 'rgba(120, 180, 255, 0.25)',
  },
  badgeText: { color: '#9ECBFF', fontSize: 11, fontWeight: '600' },
  body: { paddingHorizontal: 16, paddingTop: 14, paddingBottom: 16 },
  name: { fontSize: 22, fontWeight: '800' },
  en: {
    color: 'rgba(180, 200, 230, 0.72)',
    marginTop: 2,
    fontSize: 12,
    letterSpacing: 0.5,
  },
  divider: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    marginVertical: 12,
  },
  amountRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 10 },
  amount: { color: colors.text, fontSize: 36, fontWeight: '800', lineHeight: 40 },
  amountLabelWrap: { paddingBottom: 6 },
  amountLabel: { color: 'rgba(180, 200, 230, 0.75)', fontSize: 12 },
  amountUnit: { color: 'rgba(180, 200, 230, 0.75)', fontSize: 12, marginTop: 1 },
  desc: { color: '#7A9BC0', marginTop: 10, marginBottom: 14, fontSize: 13, lineHeight: 20 },
  metaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 16,
  },
  metaItem: { flex: 1, alignItems: 'center', gap: 10 },
  metaText: { color: 'rgba(180, 200, 230, 0.75)', fontSize: 12, textAlign: 'center' },
  metaValue: { color: colors.text, fontWeight: '600' },
});
