import { ApiError, request } from '@/api/request';
import type {
  AppChainAddressSource,
  AppChainDepositConfig,
  AppChainDepositNetwork,
  AppChainDepositOrder,
  AppChainNetworkCode,
} from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown): number | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const next = Number(value);
  return Number.isFinite(next) ? next : undefined;
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

/** expireTime 仅展示，不算倒计时 */
function pickExpireTime(source: Record<string, unknown>): string {
  const value = source.expireTime ?? source.expiredTime ?? source.expire_time;
  if (value == null || value === '') {
    return '';
  }
  if (typeof value === 'number') {
    return '';
  }
  const text = String(value).trim();
  return /^\d{4}[-/]\d{1,2}[-/]\d{1,2}/.test(text) ? text : '';
}

/** UTC 毫秒。优先 expireAt，其次 expireTimeUtc / remainSeconds */
function pickExpireAt(source: Record<string, unknown>): number {
  const raw = source.expireAt;
  let ms = 0;
  if (typeof raw === 'number' && Number.isFinite(raw) && raw > 0) {
    ms = raw > 1e12 ? raw : raw > 1e9 ? raw * 1000 : 0;
  } else if (typeof raw === 'string' && /^\d+$/.test(raw.trim())) {
    const n = Number(raw);
    ms = n > 1e12 ? n : n > 1e9 ? n * 1000 : 0;
  }
  if (ms <= 0) {
    const utc = pickString(source, ['expireTimeUtc']);
    if (utc) {
      const parsed = Date.parse(utc);
      if (Number.isFinite(parsed) && parsed > 0) {
        ms = parsed;
      }
    }
  }
  if (ms <= 0) {
    const remain = toNumber(source.remainSeconds);
    if (remain != null) {
      ms = Date.now() + remain * 1000;
    }
  }
  return ms;
}

function isEnabled(value: unknown): boolean {
  if (value === true || value === 1 || value === '1' || value === 'true' || value === 'Y' || value === 'y') {
    return true;
  }
  if (value === false || value === 0 || value === '0' || value === 'false' || value === 'N' || value === 'n') {
    return false;
  }
  return false;
}

function mapAddressSource(value: unknown): AppChainAddressSource | undefined {
  const raw = String(value ?? '').trim().toUpperCase();
  if (raw === 'SYSTEM' || raw === 'TEAM') {
    return raw;
  }
  return undefined;
}

/** TRON→TRC20，BSC/BNB→BEP20 */
export function normalizeChainNetwork(value: unknown): AppChainNetworkCode | undefined {
  const raw = String(value ?? '').trim().toUpperCase().replace(/[\s-]/g, '');
  if (raw === 'TRC20' || raw === 'TRON' || raw === 'TRX') {
    return 'TRC20';
  }
  if (raw === 'BEP20' || raw === 'BSC' || raw === 'BNB') {
    return 'BEP20';
  }
  return undefined;
}

export function chainNetworkLabel(network?: string) {
  return network === 'BEP20' ? 'USDT-BEP20' : 'USDT-TRC20';
}

function mapNetwork(raw: unknown): AppChainDepositNetwork | null {
  if (!isRecord(raw)) {
    return null;
  }
  const network = normalizeChainNetwork(raw.network ?? raw.chain ?? raw.protocol);
  if (!network) {
    return null;
  }
  const enabledRaw = raw.enabled ?? raw.enable;
  return {
    network,
    name: pickString(raw, ['name']) || chainNetworkLabel(network),
    enabled: enabledRaw === undefined ? true : isEnabled(enabledRaw),
    address: pickString(raw, ['address']),
    addressSource: mapAddressSource(raw.addressSource),
    contractAddress: pickString(raw, ['contractAddress', 'contract']),
    decimals: toNumber(raw.decimals) ?? (network === 'BEP20' ? 18 : 6),
  };
}

function compatNetwork(nested: Record<string, unknown>): AppChainDepositNetwork | null {
  const network = normalizeChainNetwork(nested.network) || 'TRC20';
  const address = pickString(nested, ['address']);
  const enabledRaw = nested.enabled ?? nested.enable ?? nested.chainEnabled;
  return {
    network,
    name: pickString(nested, ['name']) || chainNetworkLabel(network),
    enabled: enabledRaw === undefined ? Boolean(address) : isEnabled(enabledRaw),
    address,
    addressSource: mapAddressSource(nested.addressSource),
    contractAddress: pickString(nested, ['contractAddress', 'contract']),
    decimals: toNumber(nested.decimals) ?? (network === 'BEP20' ? 18 : 6),
  };
}

function mapConfig(raw: unknown): AppChainDepositConfig {
  const source = isRecord(raw) ? raw : {};
  const nested = isRecord(source.data) ? source.data : source;
  const listRaw = nested.networks;
  let networks: AppChainDepositNetwork[] = Array.isArray(listRaw)
    ? listRaw.map(mapNetwork).filter((item): item is AppChainDepositNetwork => item != null)
    : [];
  if (networks.length === 0) {
    const fallback = compatNetwork(nested);
    networks = fallback ? [fallback] : [];
  }
  const enabledRaw = nested.enabled ?? nested.enable ?? nested.chainEnabled;
  const anyOn = networks.some((item) => item.enabled);
  return {
    enabled: enabledRaw === undefined ? anyOn : isEnabled(enabledRaw),
    networks,
    minAmount: toNumber(nested.minAmount),
    maxAmount: toNumber(nested.maxAmount),
    expireMinutes: toNumber(nested.expireMinutes),
    hint: pickString(nested, ['hint']),
  };
}

function mapOrder(raw: unknown): AppChainDepositOrder | null {
  if (!isRecord(raw)) {
    return null;
  }
  const nested = isRecord(raw.data) ? raw.data : raw;
  const outTradeNo = pickString(nested, ['outTradeNo']);
  const address = pickString(nested, ['address']);
  const payAmountText = pickString(nested, ['payAmountText']);
  if (!outTradeNo || !address || !payAmountText) {
    return null;
  }
  const network = normalizeChainNetwork(nested.network) || 'TRC20';
  return {
    outTradeNo,
    network,
    address,
    addressSource: mapAddressSource(nested.addressSource),
    contractAddress: pickString(nested, ['contractAddress', 'contract']) || undefined,
    payAmountText,
    expireAt: pickExpireAt(nested),
    expireTimeUtc: pickString(nested, ['expireTimeUtc']) || undefined,
    remainSeconds: toNumber(nested.remainSeconds),
    expireTime: pickExpireTime(nested),
    status: pickString(nested, ['status']) || '0',
    hint: pickString(nested, ['hint']) || undefined,
    amount: toNumber(nested.amount),
    currency: pickString(nested, ['currency']) || 'USDT',
    txHash: pickString(nested, ['txHash']) || undefined,
  };
}

/** SYSTEM 平台收款 / TEAM 团队收款 */
export function chainAddressSourceLabel(source?: AppChainAddressSource) {
  if (source === 'TEAM') {
    return '团队收款';
  }
  if (source === 'SYSTEM') {
    return '平台收款';
  }
  return '';
}

export function preferredChainNetwork(config?: AppChainDepositConfig | null): AppChainNetworkCode | undefined {
  const enabled = config?.networks.filter((item) => item.enabled) ?? [];
  return enabled.find((item) => item.network === 'TRC20')?.network ?? enabled[0]?.network;
}

function unwrapData(res: { data?: unknown }): unknown {
  return res.data;
}

/** GET /app/chain/deposit/config */
export async function fetchAppChainDepositConfig(): Promise<AppChainDepositConfig> {
  const res = await request<unknown>('/app/chain/deposit/config');
  return mapConfig(unwrapData(res));
}

/** POST /app/chain/deposit */
export async function createAppChainDeposit(amount: number, network: AppChainNetworkCode): Promise<AppChainDepositOrder> {
  const res = await request<unknown>('/app/chain/deposit', {
    method: 'POST',
    body: { amount, network },
  });
  const mapped = mapOrder(unwrapData(res));
  if (!mapped) {
    throw new ApiError(res.msg || '链上充值下单失败', res.code);
  }
  return mapped;
}

/** GET /app/chain/deposit/order?outTradeNo= */
export async function fetchAppChainDepositOrder(outTradeNo: string): Promise<AppChainDepositOrder> {
  const res = await request<unknown>(
    `/app/chain/deposit/order?outTradeNo=${encodeURIComponent(outTradeNo)}`,
  );
  const mapped = mapOrder(unwrapData(res));
  if (!mapped) {
    throw new ApiError(res.msg || '充值单不存在', res.code);
  }
  return mapped;
}

export function isChainDepositPaid(status: string) {
  return status === '1';
}

export function isChainDepositPending(status: string) {
  return status === '0';
}

export function isChainDepositExpired(status: string) {
  return status === '2';
}

/** 倒计时秒数。只用 expireAt / expireTimeUtc / remainSeconds，不解析 expireTime */
export function chainLeftSeconds(order: AppChainDepositOrder, now = Date.now()) {
  if (order.remainSeconds != null && order.remainSeconds <= 0) {
    return 0;
  }
  if (order.expireAt && order.expireAt > 0) {
    return Math.max(0, Math.floor((order.expireAt - now) / 1000));
  }
  if (order.expireTimeUtc) {
    const ms = Date.parse(order.expireTimeUtc);
    if (Number.isFinite(ms) && ms > 0) {
      return Math.max(0, Math.floor((ms - now) / 1000));
    }
  }
  if (order.remainSeconds != null) {
    return Math.max(0, Math.floor(order.remainSeconds));
  }
  return undefined;
}

export function formatChainRemainHms(remainSeconds: number) {
  const total = Math.max(0, Math.floor(remainSeconds));
  const hh = String(Math.floor(total / 3600)).padStart(2, '0');
  const mm = String(Math.floor((total % 3600) / 60)).padStart(2, '0');
  const ss = String(total % 60).padStart(2, '0');
  return `${hh}:${mm}:${ss}`;
}

/** 去掉小数点后无意义的尾零，仅展示用；复制仍用原串 */
export function displayPayAmountText(value: string) {
  const text = value.trim();
  if (!text.includes('.')) {
    return text;
  }
  return text.replace(/(\.\d*?)0+$/, '$1').replace(/\.$/, '');
}
