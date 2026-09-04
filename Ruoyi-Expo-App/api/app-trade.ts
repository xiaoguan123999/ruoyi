import { displayText, formatBalance } from '@/api/app-auth';
import { request } from '@/api/request';
import type {
    AppAmountBody,
    AppCheckinInfo,
    AppCheckinPrizeRule,
    AppCheckinRecord,
    AppCheckinRule,
    AppFundRecord,
    AppOrderRecord,
    AppSubscribeBody,
    AppWallet,
    AppWalletLogItem,
    AppWithdrawConfig,
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

/** 认购：productId + currency + 交易密码 + 可选份数（上限由后端校验） */
export async function subscribeAppProduct(body: AppSubscribeBody): Promise<string> {
  const quantity = Math.max(1, Math.floor(body.quantity ?? 1));
  const res = await request('/app/orders', {
    method: 'POST',
    body: {
      productId: body.productId,
      currency: body.currency,
      payPassword: body.payPassword,
      quantity,
    },
  });
  return res.msg || '认购成功';
}

function mapOrderStatus(raw: Record<string, unknown>): {
  status: string;
  statusLabel: '进行中' | '已到期' | string;
  activateStatus: string;
  incomeReady: boolean;
  incomeStartTime?: string;
  activateLabel: string;
} {
  const status = pickString(raw, ['status'], '');
  const lower = status.toLowerCase();
  const expired =
    ['2', '3', 'expired', 'finished', 'closed', 'end', '已到期', '已结束', '已完成'].includes(lower) ||
    ['已到期', '已结束', '已完成'].includes(status);
  const running =
    !expired &&
    (['0', '1', 'running', 'active', 'processing', '进行中'].includes(lower) ||
      status === '' ||
      status === '进行中');

  const activateStatus = pickString(raw, ['activateStatus'], '');
  const incomeReady = raw.incomeReady === true;
  const incomeStartTime = pickString(raw, ['incomeStartTime']) || undefined;

  let activateLabel = '未激活';
  if (activateStatus === '1') {
    activateLabel = incomeReady ? '已开始返利' : '已激活';
  }

  return {
    status,
    statusLabel: expired ? '已到期' : running ? '进行中' : status || '进行中',
    activateStatus,
    incomeReady,
    incomeStartTime,
    activateLabel,
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
  const quantity = Math.max(1, Math.floor(toNumber(raw.quantity, 1)));
  const activatedQty = Math.max(0, Math.min(quantity, Math.floor(toNumber(raw.activatedQty, 0))));
  return {
    orderId,
    productId: pickNumber(raw, ['productId']) || undefined,
    productName: pickString(raw, ['productName', 'name'], '--'),
    planName: pickString(raw, ['planName', 'seriesName', 'plan'], '--'),
    amount: pickNumber(raw, ['amount', 'price', 'payAmount']),
    currency: normalizeCurrency(raw.currency),
    quantity,
    activatedQty,
    status: mapped.status,
    statusLabel: mapped.statusLabel,
    activateStatus: mapped.activateStatus,
    incomeReady: mapped.incomeReady,
    incomeStartTime: mapped.incomeStartTime
      ? formatDateTime(mapped.incomeStartTime)
      : undefined,
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
    cnyAssistWallet: 0,
    usdtAssistWallet: 0,
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

function pickTypedAssist(source: Record<string, unknown>): Pick<AppWallet, 'cnyAssistWallet' | 'usdtAssistWallet'> {
  const list = source.typedWallets;
  const result = { cnyAssistWallet: 0, usdtAssistWallet: 0 };
  if (!Array.isArray(list)) {
    return result;
  }
  for (const item of list) {
    if (!isRecord(item)) {
      continue;
    }
    const typeCode = pickString(item, ['typeCode', 'walletTypeCode']).toUpperCase();
    if (typeCode !== 'ASSIST') {
      continue;
    }
    const currency = normalizeCurrency(item.currency);
    const available = pickNumber(item, ['available', 'balance', 'amount']);
    if (currency === 'USDT') {
      result.usdtAssistWallet = available;
    } else {
      result.cnyAssistWallet = available;
    }
  }
  return result;
}

function pickIncomeFields(source: Record<string, unknown>): Pick<
  AppWallet,
  'cnyProductIncome' | 'usdtProductIncome' | 'cnyAssistValue' | 'usdtAssistValue' | 'cnyAssistWallet' | 'usdtAssistWallet'
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
    ...pickTypedAssist(source),
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

function mapWithdrawConfig(raw: unknown): AppWithdrawConfig | null {
  if (!isRecord(raw)) {
    return null;
  }
  return {
    minCny: pickNumber(raw, ['minCny']),
    maxCny: pickNumber(raw, ['maxCny']),
    minUsdt: pickNumber(raw, ['minUsdt']),
    maxUsdt: pickNumber(raw, ['maxUsdt']),
    usdtEnabled: raw.usdtEnabled === true || raw.usdtEnabled === 'true' || raw.usdtEnabled === 1,
    feeRate: pickNumber(raw, ['feeRate']),
    productWalletType: pickString(raw, ['productWalletType']) || undefined,
    promoWalletType: pickString(raw, ['promoWalletType']) || undefined,
    withdrawForbidden:
      raw.withdrawForbidden === true || raw.withdrawForbidden === 'true' || raw.withdrawForbidden === 1,
  };
}

/** GET /app/withdraw/config — 进入提现页先拉规则 */
export async function fetchAppWithdrawConfig(): Promise<AppWithdrawConfig> {
  const res = await request<unknown>('/app/withdraw/config');
  const root = extractDataRoot(res as Record<string, unknown>);
  return (
    mapWithdrawConfig(root) ??
    mapWithdrawConfig(res) ?? {
      minCny: 0,
      maxCny: 0,
      minUsdt: 0,
      maxUsdt: 0,
      usdtEnabled: true,
      feeRate: 0,
    }
  );
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
  if (['3', 'pay_pending', '待打款'].includes(lower) || raw === '待打款') {
    return '待打款';
  }
  if (
    ['0', 'pending', 'processing', 'audit', 'waiting', '申请中', '审核中', '处理中', '待审', '待审核'].includes(
      lower,
    ) ||
    raw === '申请中' ||
    raw === '审核中' ||
    raw === '待审' ||
    raw === '待审核'
  ) {
    return '审核中';
  }
  if (!raw) {
    return `${prefix}记录`;
  }
  // 已是中文文案则规范化常见别名
  if (/[\u4e00-\u9fff]/.test(raw)) {
    return normalizeFundStatusTitle(raw);
  }
  return '审核中';
}

/** 接口可能直接下发「待审」「申请中」等文案，统一成展示用状态 */
function normalizeFundStatusTitle(title: string): string {
  const raw = title.trim();
  if (!raw) {
    return raw;
  }
  if (/待审|待审核|申请中|处理中|审核中/.test(raw) && !/成功|拒绝|失败|通过/.test(raw)) {
    return '审核中';
  }
  return raw;
}

function mapFundRecord(
  raw: unknown,
  kindHint?: 'recharge' | 'withdraw',
): AppFundRecord | null {
  if (!isRecord(raw)) {
    return null;
  }
  const typeRaw = pickString(raw, ['bizType', 'type', 'recordType'], '').toUpperCase();
  const kind: 'recharge' | 'withdraw' =
    kindHint ||
    (typeRaw.includes('WITHDRAW') ? 'withdraw' : typeRaw.includes('RECHARGE') ? 'recharge' : 'recharge');
  const id = pickNumber(raw, ['rechargeId', 'withdrawId', 'recordId', 'id']);
  const amount = pickNumber(raw, ['amount']);
  const currency = normalizeCurrency(raw.currency);
  const status = pickString(raw, ['status', 'auditStatus', 'statusCode'], '');
  const titleFromApi = pickString(raw, ['statusLabel', 'title', 'statusName']);
  const title =
    kind === 'withdraw'
      ? mapWithdrawStatusLabel(status, titleFromApi || undefined)
      : normalizeFundStatusTitle(titleFromApi || mapFundStatusLabel(kind, status));

  return {
    id: String(id || `${kind}-${pickString(raw, ['createTime'])}-${amount}`),
    title,
    amount,
    currency,
    status,
    createTime: formatDateTime(raw.createTime ?? raw.auditTime ?? raw.updateTime),
  };
}

/** GET /app/fundRecords — 充值/提现申请单（含申请中/成功/拒绝） */
export async function fetchAppFundRecords(options?: {
  pageNum?: number;
  pageSize?: number;
  bizType?: 'RECHARGE' | 'WITHDRAW';
  status?: string | number;
  currency?: 'CNY' | 'USDT';
}): Promise<AppFundRecord[]> {
  const pageNum = options?.pageNum ?? 1;
  const pageSize = options?.pageSize ?? 50;
  const query = new URLSearchParams({
    pageNum: String(pageNum),
    pageSize: String(pageSize),
  });
  if (options?.bizType) {
    query.set('bizType', options.bizType);
  }
  if (options?.status !== undefined && options?.status !== null && `${options.status}` !== '') {
    query.set('status', String(options.status));
  }
  if (options?.currency) {
    query.set('currency', options.currency);
  }
  const qs = query.toString();
  const kindHint = options?.bizType === 'WITHDRAW' ? 'withdraw' : options?.bizType === 'RECHARGE' ? 'recharge' : undefined;

  const res = await request(`/app/fundRecords?${qs}`);
  return extractRows(res)
    .map((row) => mapFundRecord(row, kindHint))
    .filter((item): item is AppFundRecord => item !== null);
}

export async function fetchAppRechargeRecords(): Promise<AppFundRecord[]> {
  return fetchAppFundRecords({ bizType: 'RECHARGE', pageNum: 1, pageSize: 50 });
}

/** 提现申请单状态：0 审核中 / 3 待打款 / 1 提现成功 / 2 提现失败 */
function mapWithdrawStatusLabel(status: string, statusLabel?: string): string {
  const code = status.trim();
  if (code === '0') {
    return '审核中';
  }
  if (code === '3') {
    return '待打款';
  }
  if (code === '1') {
    return '提现成功';
  }
  if (code === '2') {
    return '提现失败';
  }
  const fromApi = statusLabel?.trim();
  if (fromApi === '已打款') {
    return '提现成功';
  }
  if (fromApi === '已拒绝') {
    return '提现失败';
  }
  if (fromApi) {
    return fromApi;
  }
  return '审核中';
}

function mapWithdrawRecord(raw: unknown): AppFundRecord | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickNumber(raw, ['withdrawId', 'id']);
  const amount = pickNumber(raw, ['amount']);
  const feeAmount = pickNumber(raw, ['feeAmount']);
  const arrivalAmount = pickNumber(raw, ['arrivalAmount']);
  const status = pickString(raw, ['status'], '');
  const statusLabel = pickString(raw, ['statusLabel']);
  const title = mapWithdrawStatusLabel(status, statusLabel || undefined);
  const auditRemark = pickString(raw, ['auditRemark', 'remark']);

  return {
    id: String(id || `withdraw-${pickString(raw, ['createTime'])}-${amount}`),
    title,
    amount,
    feeAmount: feeAmount > 0 ? feeAmount : undefined,
    arrivalAmount: arrivalAmount > 0 ? arrivalAmount : undefined,
    remark: status === '2' && auditRemark ? auditRemark : undefined,
    currency: normalizeCurrency(raw.currency),
    status,
    createTime: formatDateTime(raw.createTime ?? raw.auditTime ?? raw.updateTime),
  };
}

/** GET /app/withdraw — 提现记录（含手续费 / 到账金额） */
export async function fetchAppWithdrawRecords(options?: {
  pageNum?: number;
  pageSize?: number;
}): Promise<AppFundRecord[]> {
  const pageNum = options?.pageNum ?? 1;
  const pageSize = options?.pageSize ?? 10;
  const qs = new URLSearchParams({
    pageNum: String(pageNum),
    pageSize: String(pageSize),
    bizType: 'WITHDRAW',
  }).toString();
  const res = await request(`/app/withdraw?${qs}`);
  return extractRows(res)
    .map((row) => mapWithdrawRecord(row))
    .filter((item): item is AppFundRecord => item !== null);
}

const WALLET_BIZ_LABEL: Record<string, string> = {
  CHECKIN: '签到',
  SUBSCRIBE: '认购',
  REBATE: '产品收益',
  RECHARGE: '充值成功',
  WITHDRAW: '提现',
  WITHDRAW_FREEZE: '提现冻结',
  WITHDRAW_SUCCESS: '提现成功',
  WITHDRAW_REJECT: '提现退回',
  COMMISSION: '推广奖金',
  INVITE: '推广奖励',
  LEVEL_REWARD: '等级奖励',
  KYC_REWARD: '实名奖励',
};

function mapWalletLogTitle(raw: Record<string, unknown>, bizType: string): string {
  // 优先用接口下发的业务类型文案
  const bizTypeLabel = pickString(raw, ['bizTypeLabel', 'bizTypeName', 'typeLabel']);
  if (bizTypeLabel) {
    return bizTypeLabel;
  }

  const remark = pickString(raw, ['title', 'bizName', 'productName', 'remark', 'remarkInfo']);
  if (bizType === 'SUBSCRIBE' && remark) {
    // 常见备注：「认购产品:曙光一号」
    const matched = remark.match(/认购产品[:：]\s*(.+)$/);
    if (matched?.[1]) {
      return matched[1].trim();
    }
    return remark;
  }
  return remark || WALLET_BIZ_LABEL[bizType] || bizType || '交易';
}

function mapWalletLogItem(raw: unknown): AppWalletLogItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickNumber(raw, ['logId', 'id']);
  const amount = pickNumber(raw, ['amount', 'changeAmount']);
  const currency = normalizeCurrency(raw.currency);
  const bizType = pickString(raw, ['bizType', 'type'], '').toUpperCase();
  const title = mapWalletLogTitle(raw, bizType);
  const remark = pickString(raw, ['remark']);
  const createTime = formatDateTime(raw.createTime ?? raw.updateTime);
  if (!id && !createTime && !amount) {
    return null;
  }
  return {
    id: String(id || `${bizType}-${createTime}-${amount}`),
    title,
    remark: remark && remark !== title ? remark : undefined,
    amount,
    currency,
    createTime,
  };
}

/** GET /app/walletLog — 资金明细按钱包 typeCode 筛选 */
export async function fetchAppWalletLogs(options?: {
  pageNum?: number;
  pageSize?: number;
  currency?: 'CNY' | 'USDT';
  typeCode?: string;
}): Promise<AppWalletLogItem[]> {
  const pageNum = options?.pageNum ?? 1;
  const pageSize = options?.pageSize ?? 50;
  const query = new URLSearchParams({
    pageNum: String(pageNum),
    pageSize: String(pageSize),
  });
  if (options?.currency) {
    query.set('currency', options.currency);
  }
  if (options?.typeCode) {
    query.set('typeCode', options.typeCode);
  }
  const qs = query.toString();

  const paths = [
    `/app/walletLog?${qs}`,
    `/app/wallet/logs?${qs}`,
    `/app/funds?${qs}`,
  ];

  let lastError: unknown;
  for (const path of paths) {
    try {
      const res = await request(path);
      if (Number(res.code) === 200 || Array.isArray(res.rows) || res.data !== undefined) {
        return extractRows(res)
          .map(mapWalletLogItem)
          .filter((item): item is AppWalletLogItem => item !== null);
      }
    } catch (error) {
      lastError = error;
    }
  }

  if (lastError) {
    throw lastError;
  }
  return [];
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

function mapCheckinPrize(raw: unknown): AppCheckinPrizeRule | null {
  if (!isRecord(raw)) {
    return null;
  }
  const days = pickNumber(raw, ['days', 'streakDays']);
  const name = pickString(raw, ['name', 'prizeName']);
  if (!days || !name) {
    return null;
  }
  const enabledRaw = raw.enabled;
  const enabled =
    enabledRaw === true ||
    enabledRaw === 1 ||
    enabledRaw === '1' ||
    enabledRaw === 'true' ||
    enabledRaw === 'Y';
  return {
    days,
    name,
    rate: pickNumber(raw, ['rate', 'probability']),
    enabled,
  };
}

function mapCheckinRule(raw: unknown): AppCheckinRule {
  if (!isRecord(raw)) {
    return { amount: 0, oncePerDay: true, prizes: [] };
  }
  const prizesRaw = Array.isArray(raw.prizes) ? raw.prizes : [];
  return {
    amount: pickNumber(raw, ['amount', 'reward', 'checkinAmount']),
    oncePerDay: raw.oncePerDay !== false,
    prizes: prizesRaw
      .map(mapCheckinPrize)
      .filter((item): item is AppCheckinPrizeRule => item !== null),
  };
}

function mapCheckinInfo(raw: unknown): AppCheckinInfo {
  if (!isRecord(raw)) {
    return {
      amount: 0,
      currency: 'CNY',
      streakDays: 0,
      checkedToday: false,
      rule: { amount: 0, oncePerDay: true, prizes: [] },
    };
  }
  const checkedRaw = raw.checkedToday ?? raw.checked;
  const checkedToday =
    checkedRaw === true || checkedRaw === 1 || checkedRaw === '1' || checkedRaw === 'true';
  return {
    checkinId: pickNumber(raw, ['checkinId', 'id']) || undefined,
    checkinDate: formatDateOnly(raw.checkinDate ?? raw.date) || undefined,
    amount: pickNumber(raw, ['amount', 'reward']),
    currency: pickString(raw, ['currency'], 'CNY') || 'CNY',
    streakDays: pickNumber(raw, ['streakDays', 'streak', 'continuousDays']),
    checkedToday,
    rule: mapCheckinRule(raw.rule),
    ruleText:
      pickString(raw, ['ruleText', 'rules', 'ruleDesc']) ||
      (isRecord(raw.rule) ? pickString(raw.rule, ['ruleText', 'rules', 'ruleDesc']) : '') ||
      undefined,
    prizeDrawn: typeof raw.prizeDrawn === 'boolean' ? raw.prizeDrawn : undefined,
    prizeWon: typeof raw.prizeWon === 'boolean' ? raw.prizeWon : undefined,
    prizeName: pickString(raw, ['prizeName']) || undefined,
    prizeDays: pickNumber(raw, ['prizeDays']) || undefined,
  };
}

export async function fetchAppCheckinList(): Promise<AppCheckinRecord[]> {
  const res = await request('/app/checkin/list');
  return extractRows(res)
    .map(mapCheckin)
    .filter((item): item is AppCheckinRecord => item !== null);
}

/** GET /app/checkin/info — 签到状态与规则 */
export async function fetchAppCheckinInfo(): Promise<AppCheckinInfo> {
  const res = await request<unknown>('/app/checkin/info');
  const root = extractDataRoot(res as Record<string, unknown>);
  return mapCheckinInfo(root ?? res);
}

export async function appCheckin(): Promise<{
  message: string;
  amount?: number;
  streakDays?: number;
  prizeDrawn?: boolean;
  prizeWon?: boolean;
  prizeName?: string;
}> {
  const res = await request<unknown>('/app/checkin', { method: 'POST' });
  const root = extractDataRoot(res as Record<string, unknown>);
  const info = mapCheckinInfo(root ?? {});
  let message = res.msg || (info.amount ? `签到成功，获得 ${info.amount} 元` : '签到成功');
  if (info.prizeDrawn && info.prizeWon && info.prizeName) {
    message = `${message}，恭喜抽中「${info.prizeName}」`;
  } else if (info.prizeDrawn && info.prizeWon === false) {
    message = `${message}，本次未中奖`;
  }
  return {
    message,
    amount: info.amount || undefined,
    streakDays: info.streakDays,
    prizeDrawn: info.prizeDrawn,
    prizeWon: info.prizeWon,
    prizeName: info.prizeName,
  };
}

export function formatMoneyLabel(amount: number, currency: string): string {
  const unit = normalizeCurrency(currency) === 'USDT' ? 'USDT' : '¥';
  const abs = Math.abs(amount);
  const value = formatBalance(abs);
  const signed = amount < 0 ? '- ' : '';
  if (unit === 'USDT') {
    return `${signed}USDT ${value}`;
  }
  return `${signed}¥ ${value}`;
}

export function parseAmountInput(value: string): number {
  const next = Number(String(value).replace(/[^\d.]/g, ''));
  return Number.isFinite(next) ? next : 0;
}
