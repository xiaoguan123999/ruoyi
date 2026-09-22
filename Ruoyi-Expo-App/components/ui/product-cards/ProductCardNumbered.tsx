import { Pressable, StyleSheet, View } from 'react-native';
import { Image } from 'expo-image';

import { Text } from '@/components/ui/AppText';
import { metricAt, namedThemeKey, parseThemeHex, type ProductItem } from '@/types/product';

type Props = { item: ProductItem; onPress?: () => void };

type Skin = {
  border: string;
  line: string;
  no: string;
  en: string;
  title: string;
  slogan: string;
  label: string;
  value: string;
  unit: string;
  btn: string;
};

function hexRgb(hex: string) {
  return {
    r: parseInt(hex.slice(1, 3), 16),
    g: parseInt(hex.slice(3, 5), 16),
    b: parseInt(hex.slice(5, 7), 16),
  };
}

function hexRgba(hex: string, alpha: number) {
  const { r, g, b } = hexRgb(hex);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function hexLuma(hex: string) {
  const { r, g, b } = hexRgb(hex);
  return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
}

function skinFromHex(hex: string): Skin {
  const light = hexLuma(hex) > 0.68;
  if (light) {
    return {
      border: hexRgba(hex, 0.5),
      line: hexRgba(hex, 0.22),
      no: hexRgba(hex, 0.4),
      en: 'rgba(20, 70, 120, 0.88)',
      title: '#0B3A6E',
      slogan: 'rgba(40, 90, 130, 0.82)',
      label: 'rgba(50, 95, 135, 0.78)',
      value: '#0A2A4A',
      unit: 'rgba(60, 105, 145, 0.75)',
      btn: hexLuma(hex) > 0.78 ? '#2F7BFF' : hex,
    };
  }
  return {
    border: hexRgba(hex, 0.5),
    line: hexRgba(hex, 0.28),
    no: hexRgba(hex, 0.42),
    en: 'rgba(180, 215, 255, 0.92)',
    title: '#EAF4FF',
    slogan: 'rgba(170, 200, 235, 0.82)',
    label: 'rgba(160, 190, 225, 0.78)',
    value: '#FFFFFF',
    unit: 'rgba(180, 205, 235, 0.78)',
    btn: hex,
  };
}

const SKINS: Record<string, Skin> = {
  blue: {
    border: 'rgba(140, 190, 230, 0.55)',
    line: 'rgba(90, 140, 185, 0.2)',
    no: 'rgba(80, 140, 200, 0.45)',
    en: 'rgba(40, 90, 140, 0.88)',
    title: '#0B3A6E',
    slogan: 'rgba(50, 95, 135, 0.8)',
    label: 'rgba(70, 110, 150, 0.78)',
    value: '#0A2A4A',
    unit: 'rgba(70, 110, 150, 0.72)',
    btn: '#2F7BFF',
  },
  purple: {
    border: 'rgba(120, 170, 230, 0.4)',
    line: 'rgba(170, 200, 235, 0.14)',
    no: 'rgba(160, 195, 245, 0.42)',
    en: 'rgba(180, 215, 255, 0.92)',
    title: '#F4F8FF',
    slogan: 'rgba(170, 200, 235, 0.82)',
    label: 'rgba(160, 190, 225, 0.78)',
    value: '#FFFFFF',
    unit: 'rgba(180, 205, 235, 0.78)',
    btn: '#2F7BFF',
  },
  gold: {
    border: 'rgba(210, 175, 100, 0.45)',
    line: 'rgba(220, 185, 130, 0.16)',
    no: 'rgba(230, 190, 110, 0.45)',
    en: 'rgba(235, 205, 140, 0.92)',
    title: '#F5E6C8',
    slogan: 'rgba(220, 190, 140, 0.82)',
    label: 'rgba(210, 185, 145, 0.78)',
    value: '#FFF6E5',
    unit: 'rgba(220, 195, 155, 0.78)',
    btn: '#C9A227',
  },
  cyan: {
    border: 'rgba(80, 190, 190, 0.5)',
    line: 'rgba(40, 150, 150, 0.2)',
    no: 'rgba(40, 160, 160, 0.35)',
    en: 'rgba(20, 90, 100, 0.9)',
    title: '#0A4A4E',
    slogan: 'rgba(40, 100, 110, 0.8)',
    label: 'rgba(50, 110, 120, 0.75)',
    value: '#08383C',
    unit: 'rgba(50, 100, 110, 0.7)',
    btn: '#1AA7A0',
  },
  silver: {
    border: 'rgba(160, 170, 185, 0.5)',
    line: 'rgba(120, 130, 145, 0.2)',
    no: 'rgba(120, 130, 145, 0.4)',
    en: 'rgba(90, 100, 115, 0.9)',
    title: '#2A3140',
    slogan: 'rgba(80, 90, 105, 0.8)',
    label: 'rgba(90, 100, 115, 0.75)',
    value: '#1A2030',
    unit: 'rgba(90, 100, 115, 0.7)',
    btn: '#5B6B82',
  },
};

function themeSkin(theme?: string): Skin {
  const named = namedThemeKey(theme);
  if (named && SKINS[named]) {
    return SKINS[named];
  }
  const custom = parseThemeHex(theme);
  if (custom) {
    return skinFromHex(custom);
  }
  return SKINS.blue;
}

function stripZeros(n: number) {
  if (!Number.isFinite(n) || n <= 0) {
    return '';
  }
  return String(Number(n.toFixed(4)));
}

function DualMoney({
  cny,
  usdt,
  skin,
}: {
  cny: number;
  usdt: number;
  skin: Skin;
}) {
  const c = stripZeros(cny);
  const u = stripZeros(usdt);
  if (c && u) {
    return (
      <View>
        <Text style={styles.moneyLine} numberOfLines={1}>
          <Text style={[styles.moneyNum, { color: skin.value }]}>{c}</Text>
          <Text style={[styles.moneyUnit, { color: skin.unit }]}> 元/</Text>
        </Text>
        <Text style={styles.moneyLine} numberOfLines={1}>
          <Text style={[styles.moneyNum, { color: skin.value }]}>{u}</Text>
          <Text style={[styles.moneyUnit, { color: skin.unit }]}> USDT</Text>
        </Text>
      </View>
    );
  }
  if (c) {
    return (
      <Text style={styles.moneyLine} numberOfLines={1}>
        <Text style={[styles.moneyNum, { color: skin.value }]}>{c}</Text>
        <Text style={[styles.moneyUnit, { color: skin.unit }]}> 元</Text>
      </Text>
    );
  }
  if (u) {
    return (
      <Text style={styles.moneyLine} numberOfLines={1}>
        <Text style={[styles.moneyNum, { color: skin.value }]}>{u}</Text>
        <Text style={[styles.moneyUnit, { color: skin.unit }]}> USDT</Text>
      </Text>
    );
  }
  return <Text style={[styles.moneyNum, { color: skin.value }]}>--</Text>;
}

/** 封面铺满整卡；主题色只作用在文字和按钮上 */
export function ProductCardNumbered({ item, onPress }: Props) {
  const skin = themeSkin(item.theme);
  const m0 = metricAt(item, 0, '金额');
  const m1 = metricAt(item, 1, '每日收益');
  const m2 = metricAt(item, 2, '助力值');
  const m3 = metricAt(item, 3, '收益周期');
  const rawSlogan = (item.desc || '').trim();
  const slogan = rawSlogan && rawSlogan !== '--' ? rawSlogan : '';
  const enName = item.enName && item.enName !== '--' ? item.enName : '';
  const cardNo = String(item.cardNo || '').trim();

  return (
    <View style={styles.card}>
      <Image source={item.cover} style={styles.bg} contentFit="cover" contentPosition="top" />

      <View style={styles.heroText}>
        {cardNo ? (
          <Text style={[styles.no, { color: skin.no }]} numberOfLines={1}>
            {cardNo}
          </Text>
        ) : null}
        <View style={styles.titleCol}>
          {enName ? (
            <Text style={[styles.en, { color: skin.en }]} numberOfLines={1}>
              {enName}
            </Text>
          ) : null}
          <Text style={[styles.name, { color: skin.title }]} numberOfLines={1}>
            {item.name}
          </Text>
          {slogan ? (
            <Text style={[styles.slogan, { color: skin.slogan }]} numberOfLines={1}>
              {slogan}
            </Text>
          ) : null}
        </View>
      </View>

      <View style={styles.panel}>
        <View style={styles.gridRow}>
          <View style={[styles.metric, styles.gridLeft, styles.metricLine, { borderBottomColor: skin.line }]}>
            <Text style={[styles.label, { color: skin.label }]}>{m0.label}</Text>
            <DualMoney cny={item.amountCny} usdt={item.amount} skin={skin} />
          </View>
          <View style={[styles.metric, styles.gridRight]}>
            <Text style={[styles.labelWide, { color: skin.label }]}>{m1.label}</Text>
            <DualMoney cny={item.dailyCny} usdt={item.daily} skin={skin} />
          </View>
        </View>

        <View style={styles.gridRow}>
          <View style={styles.gridLeft}>
            <View style={[styles.metric, styles.metricLine, { borderBottomColor: skin.line }]}>
              <Text style={[styles.label, { color: skin.label }]}>{m2.label}</Text>
              <Text style={[styles.kvValue, { color: skin.value }]}>{m2.display}</Text>
            </View>
            <View style={[styles.metric, styles.cycleRow]}>
              <Text style={[styles.label, { color: skin.label }]}>{m3.label}</Text>
              <Text style={[styles.kvValue, { color: skin.value }]}>{m3.display}</Text>
            </View>
          </View>
          <View style={styles.btnCol}>
            <Pressable
              onPress={onPress}
              style={({ pressed }) => [
                styles.btn,
                { backgroundColor: skin.btn },
                pressed && styles.btnPressed,
              ]}
            >
              <Text style={styles.btnText}>{item.ctaText || '立即认购'}</Text>
            </Pressable>
          </View>
        </View>
      </View>

      <View pointerEvents="none" style={[styles.stroke, { borderColor: skin.border }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    overflow: 'hidden',
    width: '100%',
  },
  bg: {
    position: 'absolute',
    top: -4,
    left: -4,
    right: -4,
    bottom: -4,
  },
  stroke: {
    ...StyleSheet.absoluteFillObject,
    borderRadius: 16,
    borderWidth: 1,
  },
  heroText: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingTop: 12,
    paddingBottom: 8,
    paddingLeft: 12,
    paddingRight: 16,
  },
  no: {
    fontSize: 40,
    fontWeight: '800',
    lineHeight: 40,
    letterSpacing: -1,
    transform: [{ skewX: '-10deg' }],
    marginRight: 4,
  },
  titleCol: {
    flex: 1,
    minWidth: 0,
    paddingTop: 1,
  },
  en: {
    fontSize: 10,
    fontWeight: '700',
    letterSpacing: 1.6,
    lineHeight: 14,
  },
  name: {
    fontSize: 22,
    fontWeight: '800',
    lineHeight: 28,
    marginTop: 1,
    transform: [{ skewX: '-8deg' }],
  },
  slogan: {
    fontSize: 11,
    lineHeight: 16,
    marginTop: 4,
  },
  panel: {
    paddingHorizontal: 16,
    paddingTop: 10,
    paddingBottom: 16,
    gap: 0,
  },
  gridRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  gridLeft: {
    flex: 1.12,
    minWidth: 0,
  },
  gridRight: {
    flex: 1,
    minWidth: 0,
    paddingLeft: 8,
  },
  btnCol: {
    flex: 1,
    minWidth: 0,
    paddingLeft: 8,
    alignItems: 'flex-end',
    justifyContent: 'center',
    alignSelf: 'stretch',
  },
  metric: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 10,
  },
  metricLine: {
    paddingBottom: 8,
    marginBottom: 8,
    borderBottomWidth: 1,
  },
  cycleRow: {
    paddingTop: 2,
  },
  label: {
    width: 52,
    fontSize: 12,
    lineHeight: 18,
    marginTop: 1,
  },
  labelWide: {
    width: 56,
    fontSize: 12,
    lineHeight: 18,
    marginTop: 1,
  },
  moneyLine: {
    lineHeight: 18,
  },
  moneyNum: {
    fontSize: 14,
    fontWeight: '700',
  },
  moneyUnit: {
    fontSize: 12,
    fontWeight: '500',
    marginLeft: 2,
  },
  kvValue: {
    fontSize: 14,
    fontWeight: '700',
    lineHeight: 18,
  },
  btn: {
    alignSelf: 'flex-end',
    minWidth: 112,
    borderRadius: 22,
    paddingVertical: 10,
    paddingHorizontal: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnPressed: { opacity: 0.88 },
  btnText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
    letterSpacing: 1,
  },
});
