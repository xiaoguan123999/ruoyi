import { Image } from 'expo-image';
import { StyleSheet, Text, View } from 'react-native';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { ProductCycleIcon, ProductDailyIcon } from '@/components/ui/ProductStatIcons';
import type { ProductItem } from '@/constants/mock';
import { colors } from '@/theme/colors';

export type { ProductItem };

type Props = {
  item: ProductItem;
  onPress?: () => void;
};

export function ProductCard({ item, onPress }: Props) {
  const titleColor = item.titleTone === 'purple' ? '#D8CCFF' : '#A8D8FF';

  return (
    <View style={styles.card}>
      <View style={styles.coverWrap}>
        <Image source={item.cover} style={styles.cover} contentFit="cover" />
        <View style={styles.badge}>
          <Text style={styles.badgeText}>{item.tag}</Text>
        </View>
      </View>

      <View style={styles.body}>
        <Text style={[styles.name, { color: titleColor }]}>{item.name}</Text>
        <Text style={styles.en}>{item.enName}</Text>
        <View style={styles.divider} />

        <View style={styles.amountRow}>
          <Text style={styles.amount}>{item.amount}</Text>
          <View style={styles.amountLabelWrap}>
            <Text style={styles.amountLabel}>参与金额</Text>
            <Text style={styles.amountUnit}>/USDT</Text>
          </View>
        </View>

        <Text style={styles.desc}>{item.desc}</Text>

        <View style={styles.metaRow}>
          <View style={styles.metaItem}>
            <ProductDailyIcon size={44} />
            <Text style={styles.metaText}>
              每日收益 <Text style={styles.metaValue}>{item.daily} USDT</Text>
            </Text>
          </View>
          <View style={styles.metaItem}>
            <ProductCycleIcon size={44} />
            <Text style={styles.metaText}>
              收益周期 <Text style={styles.metaValue}>{item.cycle}</Text>
            </Text>
          </View>
        </View>

        <PrimaryButton title="立即参与" onPress={onPress} compact />
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
  coverWrap: {
    position: 'relative',
  },
  cover: {
    width: '100%',
    aspectRatio: 16 / 9,
  },
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
  badgeText: {
    color: '#9ECBFF',
    fontSize: 11,
    fontWeight: '600',
  },
  body: {
    paddingHorizontal: 16,
    paddingTop: 14,
    paddingBottom: 16,
  },
  name: {
    fontSize: 22,
    fontWeight: '800',
  },
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
  amountRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 10,
  },
  amount: {
    color: colors.text,
    fontSize: 40,
    fontWeight: '800',
    lineHeight: 42,
  },
  amountLabelWrap: {
    paddingBottom: 6,
  },
  amountLabel: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 12,
  },
  amountUnit: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 12,
    marginTop: 1,
  },
  desc: {
    color: '#7A9BC0',
    marginTop: 10,
    marginBottom: 14,
    fontSize: 13,
    lineHeight: 20,
  },
  metaRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 16,
  },
  metaItem: {
    flex: 1,
    alignItems: 'center',
    gap: 10,
  },
  metaText: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 12,
    textAlign: 'center',
  },
  metaValue: {
    color: colors.text,
    fontWeight: '600',
  },
});
