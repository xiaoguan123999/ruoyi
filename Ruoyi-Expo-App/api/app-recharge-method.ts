import { request } from '@/api/request';
import type { AppPayChannel, AppRechargeMethod } from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

function toNumber(value: unknown): number | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const next = Number(value);
  return Number.isFinite(next) ? next : undefined;
}

function extractList(res: Record<string, unknown>): unknown[] {
  if (Array.isArray(res.data)) {
    return res.data;
  }
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  return [];
}

export function mapAppPayChannel(raw: unknown): AppPayChannel | null {
  if (!isRecord(raw)) {
    return null;
  }
  const channelCode = pickString(raw, ['channelCode']);
  if (!channelCode) {
    return null;
  }
  return {
    channelCode,
    name: pickString(raw, ['name', 'displayName', 'channelName']) || channelCode,
    scene: pickString(raw, ['scene']).toLowerCase(),
    fulfillType: pickString(raw, ['fulfillType'], 'ONLINE').toUpperCase(),
    providerCode: pickString(raw, ['providerCode']) || undefined,
    providerName: pickString(raw, ['providerName']) || undefined,
    currency: pickString(raw, ['currency']) || undefined,
    minAmount: toNumber(raw.minAmount),
    maxAmount: toNumber(raw.maxAmount),
    mock: raw.mock === true || raw.mock === 1 || raw.mock === '1' || raw.mock === 'true',
  };
}

function mapMethod(raw: unknown): AppRechargeMethod | null {
  if (!isRecord(raw)) {
    return null;
  }
  const methodCode = pickString(raw, ['methodCode', 'key', 'code']);
  const label = pickString(raw, ['label', 'name']);
  if (!methodCode || !label) {
    return null;
  }
  return {
    methodId: toNumber(raw.methodId),
    methodCode,
    label,
    iconUrl: pickString(raw, ['iconUrl', 'icon']) || undefined,
    isCs: raw.isCs === true || raw.isCs === 1 || raw.isCs === '1',
    sortOrder: toNumber(raw.sortOrder),
  };
}

export const DEFAULT_RECHARGE_METHODS: AppRechargeMethod[] = [
  { methodId: 1, methodCode: 'wechat', label: '微信', isCs: false, sortOrder: 10 },
  { methodId: 2, methodCode: 'alipay', label: '支付宝', isCs: false, sortOrder: 20 },
  { methodId: 3, methodCode: 'usdt', label: 'USDT', isCs: false, sortOrder: 30 },
  { methodId: 4, methodCode: 'bank_cs', label: '银行卡（客服）', isCs: true, sortOrder: 40 },
];

/** GET /app/recharge/methods */
export async function fetchAppRechargeMethods(): Promise<AppRechargeMethod[]> {
  const res = await request<unknown>('/app/recharge/methods');
  if (!isRecord(res)) {
    return DEFAULT_RECHARGE_METHODS;
  }
  const list = extractList(res).map(mapMethod).filter((item): item is AppRechargeMethod => item != null);
  return list.length > 0 ? list : DEFAULT_RECHARGE_METHODS;
}

/** GET /app/recharge/methods/{methodCode}/channels */
export async function fetchAppRechargeMethodChannels(methodCode: string): Promise<AppPayChannel[]> {
  const code = encodeURIComponent(String(methodCode || '').trim())
  const res = await request<unknown>(`/app/recharge/methods/${code}/channels`);
  if (!isRecord(res)) {
    return [];
  }
  return extractList(res).map(mapAppPayChannel).filter((item): item is AppPayChannel => item != null);
}
