import { Image } from 'expo-image';
import { StyleSheet, View } from 'react-native';

import { Text } from '@/components/ui/AppText';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { metricAt, themeTitleColor, type ProductItem } from '@/types/product';
import { colors } from '@/theme/colors';

type Props = { item: ProductItem; onPress?: () => void };

export function ProductCardCompact({ item, onPress }: Props) {
  const titleColor = themeTitleColor(item.theme, item.titleTone);
  const grid = [0, 1, 2, 3].map((i) => metricAt(item, i));

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <Image source={item.cover} style={styles.strip} contentFit="cover" />
        <View style={styles.headerText}>
          <Text style={[styles.name, { color: titleColor }]} numberOfLines={1}>
            {item.name}
          </Text>
          <Text style={styles.badge} numberOfLines={1}>
            {item.badgeText || item.tag}
          </Text>
        </View>
      </View>
      <View style={styles.grid}>
        {grid.map((m, idx) => (
          <View key={idx} style={styles.cell}>
            <Text style={styles.label}>{m.label}</Text>
            <Text style={styles.value} numberOfLines={2}>
              {m.display}
            </Text>
          </View>
        ))}
      </View>
      <PrimaryButton title={item.ctaText || '立即参与'} onPress={onPress} compact />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 14,
    overflow: 'hidden',
    backgroundColor: '#0A1528',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
    padding: 12,
    gap: 12,
  },
  header: { flexDirection: 'row', gap: 10, alignItems: 'center' },
  strip: { width: 64, height: 40, borderRadius: 8 },
  headerText: { flex: 1, gap: 2 },
  name: { fontSize: 16, fontWeight: '800' },
  badge: { color: '#9ECBFF', fontSize: 11 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  cell: {
    width: '48%',
    flexGrow: 1,
    backgroundColor: 'rgba(16,32,58,0.9)',
    borderRadius: 8,
    paddingVertical: 8,
    paddingHorizontal: 10,
  },
  label: { color: 'rgba(180,200,230,0.65)', fontSize: 11 },
  value: { color: colors.text, fontSize: 13, fontWeight: '700', marginTop: 3 },
});
