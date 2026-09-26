import { Image } from 'expo-image';
import { Pressable, StyleSheet, View } from 'react-native';

import { Text } from '@/components/ui/AppText';
import { metricAt, themeTitleColor, type ProductItem } from '@/types/product';
import { colors } from '@/theme/colors';

type Props = { item: ProductItem; onPress?: () => void };

export function ProductCardRow({ item, onPress }: Props) {
  const titleColor = themeTitleColor(item.theme);
  const price = metricAt(item, 0, '价格', item.mainAmountDisplay || '--');
  const sub = metricAt(item, 1, '', '');

  return (
    <Pressable style={styles.card} onPress={onPress}>
      <Image source={item.cover} style={styles.thumb} contentFit="cover" />
      <View style={styles.mid}>
        <Text style={[styles.name, { color: titleColor }]} numberOfLines={1}>
          {item.name}
        </Text>
        <Text style={styles.desc} numberOfLines={1}>
          {item.desc}
        </Text>
        {sub.display && sub.display !== '--' ? (
          <Text style={styles.sub}>
            {sub.label ? `${sub.label} ` : ''}
            {sub.display}
          </Text>
        ) : null}
      </View>
      <View style={styles.right}>
        <Text style={styles.price} numberOfLines={2}>
          {price.display}
        </Text>
        <Text style={styles.cta}>{item.ctaText || '参与'}</Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    borderRadius: 14,
    padding: 12,
    backgroundColor: '#0A1528',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
  },
  thumb: { width: 56, height: 56, borderRadius: 10 },
  mid: { flex: 1, gap: 2 },
  name: { fontSize: 15, fontWeight: '700' },
  desc: { color: '#7A9BC0', fontSize: 12 },
  sub: { color: 'rgba(180,200,230,0.65)', fontSize: 11, marginTop: 2 },
  right: { alignItems: 'flex-end', maxWidth: 110, gap: 4 },
  price: { color: colors.text, fontSize: 13, fontWeight: '700', textAlign: 'right' },
  cta: { color: colors.accent, fontSize: 12, fontWeight: '600' },
});
