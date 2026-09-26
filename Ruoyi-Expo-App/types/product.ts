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
  /** 序号/英文名强调色；空则跟 theme 推导 */
  accentColor?: string;
  titleColor?: string;
  remarkColor?: string;
  labelColor?: string;
  valueColor?: string;
  unitColor?: string;
  btnColor?: string;
  btnTextColor?: string;
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

/** Any #RGB or #RRGGBB from the admin color field. */
export function parseThemeHex(theme?: string): string | undefined {
  const raw = String(theme || '').trim();
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

export function themeTitleColor(theme?: string): string {
  return parseThemeHex(theme) || '#A8D8FF';
}

export function metricAt(item: ProductItem, index: number, fallbackLabel = '', fallbackDisplay = '--') {
  const m = item.metrics?.[index];
  return {
    label: m?.label || fallbackLabel,
    display: m?.display || fallbackDisplay,
  };
}
