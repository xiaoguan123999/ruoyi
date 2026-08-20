import { fetchAppProfile, formatBalance } from '@/api/app-auth';
import { ApiError, request } from '@/api/request';
import type {
  AppAmountBody,
  AppCheckinRecord,
  AppFundRecord,
  AppOrderRecord,
  AppProduct,
  AppWallet,
} from '@/api/types';
import type { ProductItem } from '@/constants/mock';
import { images } from '@/constants/images';

const PRODUCT_COVERS = [images.product1, images.product2, images.product3, images.productIntro];

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function toString(value: unknown, fallback = ''): string {
  if (value === undefined || value === null) {
    return fallback;
  }
  return String(value);
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

function extractDataRoot(res: Record<string, unknown>): unknown {
  if (res.data !== undefined) {
    return res.data;
  }
  const { code, msg, token, img, uuid, captchaEnabled, captchaOnOff, roles, permissions, rows, total, ...rest } =
    res;
  return Object.keys(rest).length ? rest : null;
}

function extractRows(res: { rows?: unknown[]; data?: unknown; total?: number }): unknown[] {
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  if (Array.isArray(res.data)) {
    return res.data;
  }
  if (isRecord(res.data)) {
    const nested = res.data.rows ?? res.data.list ?? res.data.records;
    if (Array.isArray(nested)) {
      return nested;
    }
  }
  return [];
}

function formatDateTime(value: unknown): string {
  const raw = toString(value);
  if (!raw) {
    return '—';
  }
  return raw.replace('T', ' ').slice(0, 19).replace(/:/g, '：');
}

function formatDateOnly(value: unknown): string {
  const raw = toString(value);
  if (!raw) {
    return '';
  }
  return raw.slice(0, 10);
}

function normalizeCurrency(value: unknown): string {
  const raw = toString(value, 'CNY').toUpperCase();
  if (raw === 'USDT' || raw === 'USD' || raw === 'U') {
    return 'USDT';
  }
  return 'CNY';
}

function mapProduct(raw: unknown, index: number): AppProduct | null {
  if (!isRecord(raw)) {
    return null;
  }
  const productId = pickNumber(raw, ['productId', 'id']);
  if (!productId) {
    return null;
  }
  return {
    productId,
    productName: pickString(raw, ['productName', 'name', 'title'], `产品${productId}`),
    price: pickNumber(raw, ['price', 'amount', 'joinAmount']),
    currency: normalizeCurrency(raw.currency),
    dailyRebate: pickNumber(raw, ['dailyRebate', 'daily', 'dailyIncome']),
    durationDays: pickNumber(raw, ['durationDays', 'termDays', 'cycleDays']),
    remark: pickString(raw, ['remark', 'desc', 'description']),
    status: pickString(raw, ['status']),
    sort: pickNumber(raw, ['sort']),
    withdrawRequired: pickString(raw, ['withdrawRequired']),
  };
}

export function mapAppProductToItem(product: AppProduct, index = 0): ProductItem {
  const isUsdt = normalizeCurrency(product.currency) === 'USDT';
  const price = product.price;
  const daily = product.dailyRebate ?? 0;
  const days = product.durationDays ?? 0;
  const cycle =
    days >= 365 ? `${Math.round(days / 365)}年` : days > 0 ? `${days}天` : '—';

  return {
    id: String(product.productId),
    name: product.productName,
    enName: product.remark || `PRODUCT • ${String(product.productId).padStart(2, '0')}`,
    amount: isUsdt ? price : Number((price / 7).toFixed(2)),
    amountCny: isUsdt ? Number((price * 7).toFixed(2)) : price,
    daily: isUsdt ? daily : Number((daily / 7).toFixed(2)),
    dailyCny: isUsdt ? Number((daily * 7).toFixed(2)) : daily,
    cycle,
    termDays: days || 1825,
    tag: '商业航天参与计划',
    desc: product.remark || '参与计划 · 共享发展红利',
    cover: PRODUCT_COVERS[index % PRODUCT_COVERS.length],
    titleTone: index % 2 === 1 ? 'purple' : 'blue',
    payoutMethod: '每日回报（次日发放）',
    currencies: 'USDT / RMB',
    riskLevel: 'R2 中低风险',
  };
}

export async function fetchAppProducts(): Promise<AppProduct[]> {
  const res = await request<unknown>('/app/products');
  const root = extractDataRoot(res as Record<string, unknown>);
  let list: unknown[] = [];
  if (Array.isArray(root)) {
    list = root;
  } else if (isRecord(root)) {
    const nested = root.list ?? root.rows ?? root.products ?? root.records;
    if (Array.isArray(nested)) {
      list = nested;
    } else if (Array.isArray(root.data)) {
      list = root.data;
    }
  }
  if (!list.length && Array.isArray(res.rows)) {
    list = res.rows;
  }

  return list
    .map((item, index) => mapProduct(item, index))
    .filter((item): item is AppProduct => item !== null)
    .sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0));
}

export async function fetchAppProductItems(): Promise<ProductItem[]> {
  const products = await fetchAppProducts();
  return products.map((p, i) => mapAppProductToItem(p, i));
}

export async function subscribeAppProduct(body: AppAmountBody): Promise<string> {
  const res = await request('/app/orders', { method: 'POST', body });
  return res.msg || '认购成功';
}

function mapOrderStatus(raw: Record<string, unknown>): {
  status: string;
  statusLabel: '进行中' | '已到期' | string;
  activateLabel: string;
} {
  const status = pickString(raw, ['status', 'orderStatus'], '');
  const lower = status.toLowerCase();
  const expired =
    ['2', '3', 'expired', 'finished', 'closed', 'end', '已到期', '已结束', '已完成'].includes(lower) ||
    ['已到期', '已结束', '已完成'].includes(status);
  const running =
    !expired &&
    (['0', '1', 'running', 'active', 'processing', '进行中'].includes(lower) ||
      status === '' ||
      status === '进行中');

  const activated =
    ['1', 'active', 'activated', '已激活'].includes(lower) ||
    pickString(raw, ['activateStatus', 'activeStatus']) === '1';

  return {
    status,
    statusLabel: expired ? '已到期' : running ? '进行中' : status || '进行中',
    activateLabel: activated ? '已激活' : '暂未激活',
  };
}

function mapOrder(raw: unknown): AppOrderRecord | null {
  if (!isRecord(raw)) {
    return null;
  }
  const orderId = pickNumber(raw, ['orderId', 'id']);
  if (!orderId) {
    return null;
  }
  const mapped = mapOrderStatus(raw);
  return {
    orderId,
    productId: pickNumber(raw, ['productId']) || undefined,
    productName: pickString(raw, ['productName', 'name'], '认购产品'),
    planName: pickString(raw, ['planName', 'seriesName', 'plan'], '「星帆·天启计划」'),
    amount: pickNumber(raw, ['amount', 'price', 'payAmount']),
    currency: normalizeCurrency(raw.currency),
    status: mapped.status,
    statusLabel: mapped.statusLabel,
    activateLabel: mapped.activateLabel,
    createTime: formatDateTime(raw.createTime ?? raw.orderTime ?? raw.payTime),
  };
}

export async function fetchAppOrders(): Promise<AppOrderRecord[]> {
  const res = await request('/app/orders');
  return extractRows(res)
    .map(mapOrder)
    .filter((item): item is AppOrderRecord => item !== null);
}

function mapWallet(raw: unknown): AppWallet {
  if (!isRecord(raw)) {
    return { usdtAvailable: 0, cnyAvailable: 0, cnyFrozen: 0 };
  }
  return {
    usdtAvailable: pickNumber(raw, ['usdtAvailable', 'usdtBalance', 'balanceUsdt']),
    cnyAvailable: pickNumber(raw, ['cnyAvailable', 'cnyBalance', 'balanceCny']),
    cnyFrozen: pickNumber(raw, ['cnyFrozen', 'frozenCny']),
  };
}

export async function fetchAppWallet(): Promise<AppWallet> {
  try {
    const res = await request<unknown>('/app/wallet');
    const root = extractDataRoot(res as Record<string, unknown>);
    if (root) {
      return mapWallet(root);
    }
    return mapWallet(res);
  } catch (error) {
    if (error instanceof ApiError && error.code === 401) {
      throw error;
    }
    const profile = await fetchAppProfile();
    return {
      usdtAvailable: profile.usdtAvailable ?? 0,
      cnyAvailable: profile.cnyAvailable ?? 0,
      cnyFrozen: profile.cnyFrozen ?? 0,
    };
  }
}

export async function applyAppRecharge(body: AppAmountBody): Promise<string> {
  const res = await request('/app/recharge', { method: 'POST', body });
  return res.msg || '充值申请已提交';
}

export async function applyAppWithdraw(body: AppAmountBody): Promise<string> {
  const res = await request('/app/withdraw', { method: 'POST', body });
  return res.msg || '提现申请已提交';
}

function mapFundRecord(
  raw: unknown,
  kind: 'recharge' | 'withdraw',
): AppFundRecord | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickNumber(raw, ['rechargeId', 'withdrawId', 'id']);
  const amount = pickNumber(raw, ['amount']);
  const currency = normalizeCurrency(raw.currency);
  const status = pickString(raw, ['status', 'auditStatus'], '');
  const success =
    ['1', '2', 'success', 'approved', 'pass', '成功', '已通过'].includes(status.toLowerCase()) ||
    status === '成功' ||
    status === '已通过';
  const title =
    kind === 'recharge'
      ? success
        ? '充值成功'
        : status
          ? `充值${status}`
          : '充值记录'
      : success
        ? '提现成功'
        : status
          ? `提现${status}`
          : '提现记录';

  return {
    id: String(id || `${kind}-${pickString(raw, ['createTime'])}-${amount}`),
    title,
    amount,
    currency,
    status,
    createTime: formatDateTime(raw.createTime ?? raw.auditTime),
  };
}

export async function fetchAppRechargeRecords(): Promise<AppFundRecord[]> {
  const res = await request('/app/recharge');
  return extractRows(res)
    .map((row) => mapFundRecord(row, 'recharge'))
    .filter((item): item is AppFundRecord => item !== null);
}

export async function fetchAppWithdrawRecords(): Promise<AppFundRecord[]> {
  const res = await request('/app/withdraw');
  return extractRows(res)
    .map((row) => mapFundRecord(row, 'withdraw'))
    .filter((item): item is AppFundRecord => item !== null);
}

function mapCheckin(raw: unknown): AppCheckinRecord | null {
  if (!isRecord(raw)) {
    return null;
  }
  const date = formatDateOnly(raw.checkinDate ?? raw.createTime ?? raw.date);
  if (!date) {
    return null;
  }
  return {
    checkinId: pickNumber(raw, ['checkinId', 'id']) || undefined,
    checkinDate: date,
    amount: pickNumber(raw, ['amount', 'reward']),
  };
}

export async function fetchAppCheckinList(): Promise<AppCheckinRecord[]> {
  const res = await request('/app/checkin/list');
  return extractRows(res)
    .map(mapCheckin)
    .filter((item): item is AppCheckinRecord => item !== null);
}

export async function appCheckin(): Promise<{ message: string; amount?: number }> {
  const res = await request<unknown>('/app/checkin', { method: 'POST' });
  const root = extractDataRoot(res as Record<string, unknown>);
  let amount: number | undefined;
  if (isRecord(root)) {
    amount = pickNumber(root, ['amount', 'reward', 'cny']) || undefined;
  }
  return {
    message: res.msg || (amount ? `签到成功，获得 ${amount} 元` : '签到成功'),
    amount,
  };
}

export function formatMoneyLabel(amount: number, currency: string): string {
  const unit = normalizeCurrency(currency) === 'USDT' ? 'USDT' : '¥';
  if (unit === 'USDT') {
    return `USDT ${formatBalance(amount)}`;
  }
  return `¥${formatBalance(amount)}`;
}

export function parseAmountInput(value: string): number {
  const next = Number(String(value).replace(/[^\d.]/g, ''));
  return Number.isFinite(next) ? next : 0;
}
