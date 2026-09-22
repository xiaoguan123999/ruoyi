import type { ImageSource } from 'expo-image';

export type ProductMetric = {
  label: string;
  display: string;
};

export type ProductLayoutType =
  | 'CLASSIC'
  | 'HERO'
  | 'SPLIT'
  | 'NUMBERED'
  | 'ROW'
  | 'COMPACT'
  | 'BANNER'
  | 'PRICE_FOCUS'
  | 'MEDIA_LEFT'
  | string;

export type ProductItem = {
  id: string;
  name: string;
  enName: string;
  amount: number;
  amountCny: number;
  daily: number;
  dailyCny: number;
  cycle: string;
  termDays: number;
  tag: string;
  desc: string;
  cover: ImageSource;
  /** @deprecated 使用 theme */
  titleTone?: 'blue' | 'purple';
  theme?: string;
  layoutType?: ProductLayoutType;
  badgeText?: string;
  cardNo?: string;
  ctaText?: string;
  mainAmountDisplay?: string;
  metrics?: ProductMetric[];
  payoutMethod?: string;
  currencies?: string;
  riskLevel?: string;
  apiId?: number;
  onSaleFlag?: boolean;
  /** 1/true：列表直购，不进认购二级页 */
  skipDetailFlag?: boolean;
  unlockRuleText?: string;
  /** REBATE 日返 / ASSIST 助力退本 */
  bizMode?: string;
};

export type ProductSeries = {
  id: string;
  name: string;
  cover: ImageSource;
  /** 列表顶部说明文案 */
  tip?: string;
  items: ProductItem[];
};

export const THEME_PRESET_HEX = {
  blue: '#2F7BFF',
  purple: '#7B6BFF',
  gold: '#C9A227',
  cyan: '#1AA7A0',
  silver: '#8A94A6',
} as const;

export function parseThemeHex(theme?: string): string | undefined {
  const raw = String(theme || '').trim();
  if (!raw) {
    return undefined;
  }
  const named = THEME_PRESET_HEX[raw.toLowerCase() as keyof typeof THEME_PRESET_HEX];
  if (named) {
    return named;
  }
  const match = raw.match(/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/);
  if (!match) {
    return undefined;
  }
  let hex = match[1];
  if (hex.length === 3) {
    hex = `${hex[0]}${hex[0]}${hex[1]}${hex[1]}${hex[2]}${hex[2]}`;
  }
  return `#${hex.toUpperCase()}`;
}

export function namedThemeKey(theme?: string): keyof typeof THEME_PRESET_HEX | undefined {
  const raw = String(theme || '').trim().toLowerCase();
  if (raw in THEME_PRESET_HEX) {
    return raw as keyof typeof THEME_PRESET_HEX;
  }
  const hex = parseThemeHex(theme);
  if (!hex) {
    return undefined;
  }
  return (Object.keys(THEME_PRESET_HEX) as Array<keyof typeof THEME_PRESET_HEX>).find(
    (key) => THEME_PRESET_HEX[key].toUpperCase() === hex,
  );
}

export function themeTitleColor(theme?: string, fallbackTone?: 'blue' | 'purple'): string {
  const key = namedThemeKey(theme) || namedThemeKey(fallbackTone);
  if (key === 'purple') return '#D8CCFF';
  if (key === 'gold') return '#E8C36A';
  if (key === 'cyan') return '#7EE7E0';
  if (key === 'silver') return '#C8D0DC';
  if (key === 'blue') return '#A8D8FF';
  return parseThemeHex(theme) || '#A8D8FF';
}

export function metricAt(item: ProductItem, index: number, fallbackLabel = '', fallbackDisplay = '--') {
  const m = item.metrics?.[index];
  return {
    label: m?.label || fallbackLabel,
    display: m?.display || fallbackDisplay,
  };
}
