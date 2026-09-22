import { useState } from 'react';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, View } from 'react-native';
import Svg, {
  Defs,
  LinearGradient,
  Polygon,
  Rect,
  Stop,
  Text as SvgText,
} from 'react-native-svg';

import { Text } from '@/components/ui/AppText';
import { metricAt, namedThemeKey, parseThemeHex, type ProductItem } from '@/types/product';

type Props = {
  item: ProductItem;
  onPress?: () => void;
  onSubscribe?: (currency: 'CNY' | 'USDT') => void;
};

/** 主题色：角标实心色 / 助力值高亮（对齐原图三档） */
function themeAccent(theme?: string) {
  const key = namedThemeKey(theme) || (theme || 'blue').toLowerCase();
  if (key === 'purple') {
    return { badge: '#A890E0', assist: '#C4A8FF' };
  }
  if (key === 'gold') {
    return { badge: '#D4B06A', assist: '#E8C36A' };
  }
  const hex = parseThemeHex(theme);
  if (hex && !namedThemeKey(theme)) {
    return { badge: hex, assist: hex };
  }
  return { badge: '#5EB4F0', assist: '#7EC8FF' };
}

function stripZeros(n: number) {
  if (!Number.isFinite(n) || n <= 0) return '';
  return String(Number(n.toFixed(4)));
}

/** 原图金额：上行「500 元/」下行「71.4 USDT」，数字亮白、单位灰 */
function AmountValue({ item }: { item: ProductItem }) {
  const cny = item.amountCny > 0 ? stripZeros(item.amountCny) : '';
  const usdt = item.amount > 0 ? stripZeros(item.amount) : '';

  if (cny && usdt) {
    return (
      <View style={styles.amountBlock}>
        <Text style={styles.amountLine} numberOfLines={1}>
          <Text style={styles.amountNum}>{cny}</Text>
          <Text style={styles.amountUnit}> 元/</Text>
        </Text>
        <Text style={styles.amountLine} numberOfLines={1}>
          <Text style={styles.amountNum}>{usdt}</Text>
          <Text style={styles.amountUnit}> USDT</Text>
        </Text>
      </View>
    );
  }
  if (cny) {
    return (
      <Text style={styles.amountLine} numberOfLines={1}>
        <Text style={styles.amountNum}>{cny}</Text>
        <Text style={styles.amountUnit}> 元</Text>
      </Text>
    );
  }
  if (usdt) {
    return (
      <Text style={styles.amountLine} numberOfLines={1}>
        <Text style={styles.amountNum}>{usdt}</Text>
        <Text style={styles.amountUnit}> USDT</Text>
      </Text>
    );
  }
  return <Text style={styles.value}>--</Text>;
}

/**
 * 原图角标（特写）：贴齐卡片左上角
 * - 上边、左边贴边；底边水平
 * - 仅右边斜切（上宽下窄）
 * - 实心浅蓝，无渐变；白字略斜
 * - 整体不旋转
 */
function SplitBadge({
  text,
  color,
}: {
  text: string;
  color: string;
}) {
  const h = 26;
  const padX = 12;
  // 按字数估宽，右边斜切内收约 14px
  const textW = Math.max(48, text.length * 14);
  const w = padX + textW + 18;
  const cut = 14;
  // 0,0 → w,0 → w-cut,h → 0,h
  const points = `0,0 ${w},0 ${w - cut},${h} 0,${h}`;

  return (
    <View style={styles.badgeWrap} pointerEvents="none">
      <Svg width={w} height={h}>
        <Polygon points={points} fill={color} />
        <SvgText
          x={padX + textW / 2 - 2}
          y={h / 2 + 5}
          fill="#FFFFFF"
          fontSize="13"
          fontWeight="700"
          fontStyle="italic"
          textAnchor="middle"
        >
          {text}
        </SvgText>
      </Svg>
    </View>
  );
}

/** 原图 RMB：横向渐变 浅金(左) → 深金/橙(右) */
function RmbButton({ title, onPress }: { title: string; onPress?: () => void }) {
  const [size, setSize] = useState({ width: 0, height: 0 });
  return (
    <Pressable
      onPress={onPress}
      onLayout={(e) => {
        const { width, height } = e.nativeEvent.layout;
        setSize({ width, height });
      }}
      style={({ pressed }) => [styles.btnBase, pressed && styles.pressed]}
    >
      {size.width > 0 ? (
        <Svg width={size.width} height={size.height} style={StyleSheet.absoluteFill}>
          <Defs>
            <LinearGradient id="rmbHGrad" x1="0" y1="0" x2="1" y2="0">
              <Stop offset="0" stopColor="#F3E2B0" />
              <Stop offset="0.55" stopColor="#E0C078" />
              <Stop offset="1" stopColor="#C9954A" />
            </LinearGradient>
          </Defs>
          <Rect
            x="0"
            y="0"
            width={size.width}
            height={size.height}
            rx={size.height / 2}
            ry={size.height / 2}
            fill="url(#rmbHGrad)"
          />
        </Svg>
      ) : (
        <View style={[StyleSheet.absoluteFill, { backgroundColor: '#E0C078' }]} />
      )}
      <Text style={styles.rmbText}>{title}</Text>
    </Pressable>
  );
}

/** 原图 USDT：实心中蓝胶囊 */
function UsdtButton({ title, onPress }: { title: string; onPress?: () => void }) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [styles.btnBase, styles.usdtBtn, pressed && styles.pressed]}
    >
      <Text style={styles.usdtText}>{title}</Text>
    </Pressable>
  );
}

/** 助力左右卡 — 严格按「星航助力」原图 */
export function ProductCardSplit({ item, onPress, onSubscribe }: Props) {
  const accent = themeAccent(item.theme);
  const badge = item.badgeText || item.name;
  const assist = metricAt(item, 1, '助力值', '--');
  const limit = metricAt(item, 2, '限购份数', '--');
  const principal = metricAt(item, 3, '本金返还', '--');
  const supportCny = item.amountCny > 0;
  const supportUsdt = item.amount > 0;

  const handle = (currency: 'CNY' | 'USDT') => {
    if (onSubscribe) {
      onSubscribe(currency);
      return;
    }
    onPress?.();
  };

  return (
    <View style={styles.card}>
      {/* 左：封面 + 斜切角标 + RMB认购 */}
      <View style={styles.left}>
        <Image source={item.cover} style={styles.cover} contentFit="cover" />
        <SplitBadge text={badge} color={accent.badge} />
        {supportCny ? (
          <View style={styles.leftBtn}>
            <RmbButton title="RMB认购" onPress={() => handle('CNY')} />
          </View>
        ) : null}
      </View>

      {/* 右：四行指标 + USDT认购 */}
      <View style={styles.right}>
        <View style={styles.metrics}>
          <View style={styles.row}>
            <Text style={styles.label}>{metricAt(item, 0, '金额').label || '金额'}</Text>
            <AmountValue item={item} />
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>{assist.label}</Text>
            <Text style={[styles.value, { color: accent.assist }]} numberOfLines={1}>
              {assist.display}
            </Text>
          </View>
          <View style={styles.row}>
            <Text style={styles.label}>{limit.label || '限购份数'}</Text>
            <Text style={styles.value} numberOfLines={1}>
              {limit.display}
            </Text>
          </View>
          <View style={[styles.row, styles.rowLast]}>
            <Text style={styles.label}>{principal.label}</Text>
            <Text style={styles.value} numberOfLines={1}>
              {principal.display}
            </Text>
          </View>
        </View>
        {supportUsdt ? (
          <View style={styles.rightBtn}>
            <UsdtButton title="USDT认购" onPress={() => handle('USDT')} />
          </View>
        ) : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    minHeight: 196,
    borderRadius: 12,
    overflow: 'hidden',
    backgroundColor: '#071224',
    borderWidth: 1,
    borderColor: 'rgba(110, 170, 230, 0.5)',
  },
  left: {
    width: '45%',
    position: 'relative',
    backgroundColor: '#040A14',
  },
  cover: {
    ...StyleSheet.absoluteFillObject,
  },
  badgeWrap: {
    position: 'absolute',
    left: 0,
    top: 12,
    zIndex: 4,
  },
  leftBtn: {
    position: 'absolute',
    left: 10,
    right: 10,
    bottom: 12,
  },
  right: {
    flex: 1,
    paddingTop: 12,
    paddingHorizontal: 12,
    paddingBottom: 12,
    justifyContent: 'space-between',
    backgroundColor: 'rgba(8, 18, 38, 0.94)',
  },
  metrics: {
    flex: 1,
    justifyContent: 'center',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 8,
    paddingVertical: 7,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.25)',
  },
  rowLast: {
    borderBottomWidth: 0,
  },
  label: {
    color: 'rgba(170, 195, 225, 0.8)',
    fontSize: 12,
  },
  value: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '700',
    flexShrink: 1,
    textAlign: 'right',
  },
  amountBlock: {
    alignItems: 'flex-end',
    flexShrink: 1,
  },
  amountLine: {
    textAlign: 'right',
    lineHeight: 16,
  },
  amountNum: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '700',
  },
  amountUnit: {
    color: 'rgba(170, 195, 225, 0.75)',
    fontSize: 12,
    fontWeight: '500',
  },
  rightBtn: {
    marginTop: 8,
    alignSelf: 'center',
    width: '88%',
  },
  btnBase: {
    height: 36,
    borderRadius: 18,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
  },
  usdtBtn: {
    backgroundColor: '#3A6FE0',
  },
  rmbText: {
    color: '#1A1408',
    fontSize: 14,
    fontWeight: '800',
    zIndex: 1,
  },
  usdtText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '700',
  },
  pressed: {
    opacity: 0.9,
  },
});
