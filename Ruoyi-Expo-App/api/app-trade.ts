import { displayText, formatBalance } from '@/api/app-auth';
import { ApiError, request } from '@/api/request';
import type {
  AppAmountBody,
  AppCheckinRecord,
  AppFundRecord,
  AppOrderRecord,
  AppSubscribeBody,
  AppWallet,
} from '@/api/types';

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
    return displayText();
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

/** 认购：只传 productId + currency，金额以后台配置为准 */
export async function subscribeAppProduct(body: AppSubscribeBody): Promise<string> {
  const res = await request('/app/orders', {
    method: 'POST',
    body: {
      productId: body.productId,
      currency: body.currency,
    },
  });
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
    productName: pickString(raw, ['productName', 'name'], '--'),
    planName: pickString(raw, ['planName', 'seriesName', 'plan'], '--'),
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

function emptyWallet(): AppWallet {
  return {
    usdtAvailable: 0,
    cnyAvailable: 0,
    cnyFrozen: 0,
    cnyProductIncome: 0,
    usdtProductIncome: 0,
    cnyAssistValue: 0,
    usdtAssistValue: 0,
  };
}

function isUsdtCurrency(currency: string): boolean {
  const upper = currency.toUpperCase();
  return upper === 'USDT' || upper === 'USD' || upper === 'U';
}

function isCnyCurrency(currency: string): boolean {
  const upper = currency.toUpperCase();
  return upper === 'CNY' || upper === 'RMB' || upper === '¥';
}

/** 兼容对象形态 / 币种列表形态的钱包数据 */
function mapWallet(raw: unknown): AppWallet | null {
  if (Array.isArray(raw)) {
    return mapWalletList(raw);
  }
  if (!isRecord(raw)) {
    return null;
  }

  // data: { list: [...] } / { wallets: [...] }
  const nestedList = raw.list ?? raw.wallets ?? raw.records ?? raw.rows;
  if (Array.isArray(nestedList)) {
    const fromList = mapWalletList(nestedList);
    if (fromList) {
      return {
        ...fromList,
        ...pickIncomeFields(raw),
      };
    }
  }

  const nested = [raw.wallet, raw.member, raw.account, raw.balance].find(isRecord);
  const source = nested ?? raw;
  const hasBalanceKey = [
    'cnyAvailable',
    'usdtAvailable',
    'cnyBalance',
    'usdtBalance',
    'balanceCny',
    'balanceUsdt',
    'cny',
    'usdt',
    'cnyFrozen',
    'available',
    'currency',
  ].some((key) => source[key] !== undefined && source[key] !== null);

  // 单条 { currency, available, frozen }
  if (pickString(source, ['currency'])) {
    return mapWalletList([source]);
  }

  if (!hasBalanceKey) {
    return null;
  }

  return {
    ...emptyWallet(),
    usdtAvailable: pickNumber(source, ['usdtAvailable', 'usdtBalance', 'balanceUsdt', 'usdt']),
    cnyAvailable: pickNumber(source, ['cnyAvailable', 'cnyBalance', 'balanceCny', 'cny']),
    cnyFrozen: pickNumber(source, ['cnyFrozen', 'frozenCny']),
    ...pickIncomeFields(source),
  };
}

function pickIncomeFields(source: Record<string, unknown>): Pick<
  AppWallet,
  'cnyProductIncome' | 'usdtProductIncome' | 'cnyAssistValue' | 'usdtAssistValue'
> {
  return {
    cnyProductIncome: pickNumber(source, [
      'cnyProductIncome',
      'productIncomeCny',
      'productIncome',
      'cnyIncome',
    ]),
    usdtProductIncome: pickNumber(source, [
      'usdtProductIncome',
      'productIncomeUsdt',
      'usdtIncome',
    ]),
    cnyAssistValue: pickNumber(source, [
      'cnyAssistValue',
      'assistValueCny',
      'assistValue',
      'cnyAssist',
    ]),
    usdtAssistValue: pickNumber(source, [
      'usdtAssistValue',
      'assistValueUsdt',
      'usdtAssist',
    ]),
  };
}

function mapWalletList(list: unknown[]): AppWallet | null {
  const wallet = emptyWallet();
  let found = false;
  for (const item of list) {
    if (!isRecord(item)) {
      continue;
    }
    const currency = pickString(item, ['currency', 'coin', 'asset']);
    const available = pickNumber(item, ['available', 'balance', 'amount', 'cnyAvailable', 'usdtAvailable']);
    const frozen = pickNumber(item, ['frozen', 'cnyFrozen', 'freeze']);
    if (isCnyCurrency(currency)) {
      wallet.cnyAvailable = available;
      wallet.cnyFrozen = frozen;
      found = true;
    } else if (isUsdtCurrency(currency)) {
      wallet.usdtAvailable = available;
      found = true;
    } else if (!currency) {
      if (item.cnyAvailable !== undefined || item.cnyBalance !== undefined) {
        wallet.cnyAvailable = pickNumber(item, ['cnyAvailable', 'cnyBalance', 'cny']);
        found = true;
      }
      if (item.usdtAvailable !== undefined || item.usdtBalance !== undefined) {
        wallet.usdtAvailable = pickNumber(item, ['usdtAvailable', 'usdtBalance', 'usdt']);
        found = true;
      }
    }
  }
  return found ? wallet : null;
}

export async function fetchAppWallet(): Promise<AppWallet> {
  const res = await request<unknown>('/app/wallet');
  const root = extractDataRoot(res as Record<string, unknown>);
  const mapped = mapWallet(root) ?? mapWallet(res);
  return mapped ?? emptyWallet();
}

export async function applyAppRecharge(body: AppAmountBody): Promise<string> {
  const res = await request('/app/recharge', { method: 'POST', body });
  return res.msg || '充值申请已提交';
}

export async function applyAppWithdraw(body: AppAmountBody): Promise<string> {
  const res = await request('/app/withdraw', { method: 'POST', body });
  return res.msg || '提现申请已提交';
}

function mapFundStatusLabel(kind: 'recharge' | 'withdraw', status: string): string {
  const raw = status.trim();
  const lower = raw.toLowerCase();
  const prefix = kind === 'recharge' ? '充值' : '提现';

  if (
    ['1', 'success', 'approved', 'pass', 'passed', '成功', '已通过', '已成功'].includes(lower) ||
    raw === '成功' ||
    raw === '已通过'
  ) {
    return `${prefix}成功`;
  }
  if (['2', 'reject', 'rejected', 'fail', 'failed', '拒绝', '已拒绝', '失败'].includes(lower)) {
    return `${prefix}失败`;
  }
  if (
    ['0', 'pending', 'processing', 'audit', 'waiting', '申请中', '审核中', '处理中'].includes(lower) ||
    raw === '申请中' ||
    raw === '审核中'
  ) {
    return '申请中';
  }
  if (!raw) {
    return `${prefix}记录`;
  }
  // 已是中文文案则直接用，避免再拼出「充值0」这类状态码
  if (/[\u4e00-\u9fff]/.test(raw)) {
    return raw;
  }
  return '申请中';
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
  const title = mapFundStatusLabel(kind, status);

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
