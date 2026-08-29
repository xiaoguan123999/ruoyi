import { request } from '@/api/request';
import type { AppProduct, AppProductSeries } from '@/api/types';
import { images } from '@/constants/images';
import { config } from '@/config';
import type { ProductItem, ProductSeries } from '@/types/product';

const SERIES_COVERS = [images.productIntro, images.product1, images.product2];
const PRODUCT_COVERS = [images.product1, images.product2, images.product3, images.productIntro];

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickNumber(source: Record<string, unknown>, keys: string[]): number {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null) {
      return toNumber(source[key]);
    }
  }
  return 0;
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

function extractList(res: Record<string, unknown>): unknown[] {
  if (Array.isArray(res.data)) {
    return res.data;
  }
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  if (isRecord(res.data)) {
    const nested = res.data.list ?? res.data.rows ?? res.data.records ?? res.data.products;
    if (Array.isArray(nested)) {
      return nested;
    }
  }
  return [];
}

function resolveMediaUrl(raw: string): string {
  const url = raw.trim();
  if (!url) {
    return '';
  }
  if (/^(https?:)?\/\//i.test(url) || url.startsWith('data:')) {
    return url.startsWith('//') ? `https:${url}` : url;
  }
  if (!config.API_URL) {
    return url;
  }
  if (url.startsWith('/')) {
    return `${config.API_URL}${url}`;
  }
  return `${config.API_URL}/${url}`;
}

function normalizeCurrency(value: unknown): 'CNY' | 'USDT' {
  const raw = String(value ?? 'CNY').toUpperCase();
  if (raw === 'USDT' || raw === 'USD' || raw === 'U') {
    return 'USDT';
  }
  return 'CNY';
}

function formatCycle(days: number): string {
  if (days <= 0) {
    return '--';
  }
  if (days % 365 === 0) {
    return `${days / 365}年`;
  }
  if (days % 30 === 0) {
    return `${days / 30}个月`;
  }
  return `${days}天`;
}

function mapSeries(raw: unknown): AppProductSeries | null {
  if (!isRecord(raw)) {
    return null;
  }
  const seriesId = pickNumber(raw, ['seriesId', 'categoryId', 'id']);
  if (!seriesId) {
    return null;
  }
  const coverUrl = resolveMediaUrl(pickString(raw, ['coverUrl', 'cover', 'imageUrl']));
  return {
    seriesId,
    seriesName: pickString(raw, ['seriesName', 'categoryName', 'name', 'title'], '--'),
    coverUrl: coverUrl || undefined,
    sort: pickNumber(raw, ['sort']),
  };
}

function mapProduct(raw: unknown): AppProduct | null {
  if (!isRecord(raw)) {
    return null;
  }
  const productId = pickNumber(raw, ['productId', 'id']);
  if (!productId) {
    return null;
  }
  const coverUrl = resolveMediaUrl(pickString(raw, ['coverUrl', 'cover', 'imageUrl']));
  const seriesId = pickNumber(raw, ['seriesId', 'categoryId']);
  const legacyPrice = pickNumber(raw, ['price', 'amount', 'joinAmount']);
  const legacyDaily = pickNumber(raw, ['dailyRebate', 'daily', 'dailyIncome']);
  const priceCny = pickNumber(raw, ['priceCny']);
  const priceUsdt = pickNumber(raw, ['priceUsdt']);
  const dailyRebateCny = pickNumber(raw, ['dailyRebateCny']);
  const dailyRebateUsdt = pickNumber(raw, ['dailyRebateUsdt']);

  // 兼容旧单币字段：无双币价时按 currency 落到对应侧
  let nextPriceCny = priceCny;
  let nextPriceUsdt = priceUsdt;
  let nextDailyCny = dailyRebateCny;
  let nextDailyUsdt = dailyRebateUsdt;
  if (priceCny <= 0 && priceUsdt <= 0 && legacyPrice > 0) {
    if (normalizeCurrency(raw.currency) === 'USDT') {
      nextPriceUsdt = legacyPrice;
      nextDailyUsdt = legacyDaily;
    } else {
      nextPriceCny = legacyPrice;
      nextDailyCny = legacyDaily;
    }
  }

  return {
    productId,
    productName: pickString(raw, ['productName', 'name', 'title'], '--'),
    nameEn: pickString(raw, ['nameEn', 'enName', 'englishName']) || undefined,
    price: legacyPrice || nextPriceCny || nextPriceUsdt,
    priceCny: nextPriceCny,
    priceUsdt: nextPriceUsdt,
    currency: normalizeCurrency(raw.currency),
    dailyRebate: legacyDaily || nextDailyCny || nextDailyUsdt,
    dailyRebateCny: nextDailyCny,
    dailyRebateUsdt: nextDailyUsdt,
    durationDays: pickNumber(raw, ['durationDays', 'termDays', 'cycleDays']),
    remark: pickString(raw, ['remark', 'desc', 'description']) || undefined,
    status: pickString(raw, ['status']) || undefined,
    sort: pickNumber(raw, ['sort']),
    withdrawRequired: pickString(raw, ['withdrawRequired']) || undefined,
    seriesId: seriesId || undefined,
    categoryId: pickNumber(raw, ['categoryId']) || seriesId || undefined,
    categoryName: pickString(raw, ['categoryName', 'seriesName']) || undefined,
    coverUrl: coverUrl || undefined,
    riskLevel: pickString(raw, ['riskLevel']) || undefined,
    payoutMethod: pickString(raw, ['payoutMethod']) || undefined,
    onSaleFlag: raw.onSaleFlag === true,
  };
}

export function mapAppProductToItem(product: AppProduct, index = 0): ProductItem {
  const amountCny = product.priceCny && product.priceCny > 0 ? product.priceCny : 0;
  const amountUsdt = product.priceUsdt && product.priceUsdt > 0 ? product.priceUsdt : 0;
  const dailyCny = product.dailyRebateCny && product.dailyRebateCny > 0 ? product.dailyRebateCny : 0;
  const dailyUsdt = product.dailyRebateUsdt && product.dailyRebateUsdt > 0 ? product.dailyRebateUsdt : 0;
  const days = product.durationDays && product.durationDays > 0 ? product.durationDays : 0;
  const cover = product.coverUrl
    ? { uri: product.coverUrl }
    : PRODUCT_COVERS[index % PRODUCT_COVERS.length];

  const support: string[] = [];
  if (amountUsdt > 0) {
    support.push('USDT');
  }
  if (amountCny > 0) {
    support.push('RMB');
  }

  return {
    id: String(product.productId),
    apiId: product.productId,
    name: product.productName || '--',
    enName: product.nameEn || '--',
    amount: amountUsdt,
    amountCny,
    daily: dailyUsdt,
    dailyCny,
    cycle: formatCycle(days),
    termDays: days,
    tag: product.categoryName || '--',
    desc: product.remark || '--',
    cover,
    titleTone: index % 2 === 1 ? 'purple' : 'blue',
    payoutMethod: product.payoutMethod || undefined,
    currencies: support.length ? support.join(' / ') : '--',
    riskLevel: product.riskLevel || undefined,
    onSaleFlag: product.onSaleFlag === true,
  };
}

function toUiSeries(series: AppProductSeries, index: number, items: ProductItem[] = []): ProductSeries {
  return {
    id: String(series.seriesId),
    name: series.seriesName || '--',
    cover: series.coverUrl ? { uri: series.coverUrl } : SERIES_COVERS[index % SERIES_COVERS.length],
    items,
  };
}

/** GET /app/product/series */
export async function fetchAppProductSeriesList(): Promise<ProductSeries[]> {
  const res = await request<unknown>('/app/product/series');
  return extractList(res as Record<string, unknown>)
    .map(mapSeries)
    .filter((item): item is AppProductSeries => item !== null)
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    .map((item, index) => toUiSeries(item, index));
}

/** GET /app/product/series/{seriesId} */
export async function fetchAppProductSeriesDetail(seriesId: string | number): Promise<ProductSeries | null> {
  const id = Number(seriesId);
  if (!Number.isFinite(id) || id <= 0) {
    return null;
  }
  const res = await request<unknown>(`/app/product/series/${id}`);
  const root = (res as { data?: unknown }).data ?? res;
  const mapped = mapSeries(root);
  if (!mapped) {
    return null;
  }
  return toUiSeries(mapped, 0);
}

/** GET /app/products?seriesId= */
export async function fetchAppProducts(seriesId?: string | number): Promise<AppProduct[]> {
  const query =
    seriesId !== undefined && seriesId !== null && String(seriesId).length > 0
      ? `?seriesId=${encodeURIComponent(String(seriesId))}`
      : '';
  const res = await request<unknown>(`/app/products${query}`);
  return extractList(res as Record<string, unknown>)
    .map(mapProduct)
    .filter((item): item is AppProduct => item !== null)
    .filter((item) => item.status !== '1')
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
}

/** 系列详情页：系列信息 + 该系列产品列表 */
export async function fetchAppProductSeriesWithItems(
  seriesId: string | number,
): Promise<ProductSeries | null> {
  const [detail, products] = await Promise.all([
    fetchAppProductSeriesDetail(seriesId).catch(() => null),
    fetchAppProducts(seriesId),
  ]);
  const items = products.map((product, index) => mapAppProductToItem(product, index));
  if (detail) {
    return { ...detail, items };
  }
  if (!items.length) {
    return null;
  }
  return {
    id: String(seriesId),
    name: products[0]?.categoryName || '--',
    cover: SERIES_COVERS[0],
    items,
  };
}

/** GET /app/products/{productId} — 产品详情（认购页） */
export async function fetchAppProductById(productId?: string | number): Promise<ProductItem | undefined> {
  if (productId === undefined || productId === null || String(productId).length === 0) {
    return undefined;
  }
  const id = Number(productId);
  if (!Number.isFinite(id) || id <= 0) {
    return undefined;
  }
  const res = await request<unknown>(`/app/products/${id}`);
  const root = (res as { data?: unknown }).data ?? res;
  const mapped = mapProduct(root);
  if (!mapped) {
    return undefined;
  }
  return mapAppProductToItem(mapped, 0);
}
