import { ApiError, request } from '@/api/request';
import type { AppPayChannel, AppPayDeposit, AppPayDepositBody, AppPayOrder } from '@/api/types';

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

function extractList(res: Record<string, unknown>): unknown[] {
  if (Array.isArray(res.data)) {
    return res.data;
  }
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  return [];
}

function mapChannel(raw: unknown): AppPayChannel | null {
  if (!isRecord(raw)) {
    return null;
  }
  const channelCode = pickString(raw, ['channelCode']);
  const scene = pickString(raw, ['scene']).toLowerCase();
  if (!channelCode || !scene) {
    return null;
  }
  return {
    channelCode,
    name: pickString(raw, ['name', 'displayName', 'channelName']) || channelCode,
    scene,
    providerCode: pickString(raw, ['providerCode']) || undefined,
    providerName: pickString(raw, ['providerName']) || undefined,
    currency: pickString(raw, ['currency']) || undefined,
    minAmount: toNumber(raw.minAmount),
    maxAmount: toNumber(raw.maxAmount),
    mock: raw.mock === true || raw.mock === 1 || raw.mock === '1' || raw.mock === 'true',
  };
}

function mapDeposit(raw: unknown): AppPayDeposit | null {
  if (!isRecord(raw)) {
    return null;
  }
  const outTradeNo = pickString(raw, ['outTradeNo']);
  const payUrl = pickString(raw, ['payUrl', 'payurl', 'url']);
  if (!outTradeNo || !payUrl) {
    return null;
  }
  return {
    outTradeNo,
    rechargeId: toNumber(raw.rechargeId),
    payUrl,
    payType: pickString(raw, ['payType']) || undefined,
    amount: toNumber(raw.amount) ?? 0,
    currency: pickString(raw, ['currency']) || undefined,
    channelCode: pickString(raw, ['channelCode']) || undefined,
    channelName: pickString(raw, ['channelName']) || undefined,
    providerCode: pickString(raw, ['providerCode']) || undefined,
    mock: raw.mock === true || raw.mock === 1 || raw.mock === '1' || raw.mock === 'true',
    expireTime: pickString(raw, ['expireTime']) || undefined,
  };
}

function mapOrder(raw: unknown): AppPayOrder | null {
  if (!isRecord(raw)) {
    return null;
  }
  const outTradeNo = pickString(raw, ['outTradeNo']);
  if (!outTradeNo) {
    return null;
  }
  return {
    outTradeNo,
    status: pickString(raw, ['status']) || '0',
    amount: toNumber(raw.amount),
    currency: pickString(raw, ['currency']) || undefined,
    payUrl: pickString(raw, ['payUrl']) || undefined,
    channelName: pickString(raw, ['channelName']) || undefined,
    expireTime: pickString(raw, ['expireTime']) || undefined,
  };
}

/** GET /app/pay/channels?scene= */
export async function fetchAppPayChannels(scene?: string): Promise<AppPayChannel[]> {
  const query = scene ? `?scene=${encodeURIComponent(scene)}` : '';
  const res = await request<unknown>(`/app/pay/channels${query}`);
  return extractList(res as Record<string, unknown>)
    .map(mapChannel)
    .filter((item): item is AppPayChannel => item !== null);
}

export function listPayChannelsByScene(channels: AppPayChannel[], scene: string): AppPayChannel[] {
  return channels.filter((item) => item.scene === scene);
}

export function pickPayChannel(
  channels: AppPayChannel[],
  scene: string,
  channelCode?: string,
): AppPayChannel | undefined {
  const list = listPayChannelsByScene(channels, scene);
  if (channelCode) {
    return list.find((item) => item.channelCode === channelCode) ?? list[0];
  }
  return list[0];
}

/** POST /app/pay/deposit */
export async function createAppPayDeposit(body: AppPayDepositBody): Promise<AppPayDeposit> {
  const res = await request<unknown>('/app/pay/deposit', {
    method: 'POST',
    body: {
      amount: body.amount,
      scene: body.scene,
      channelCode: body.channelCode,
      returnUrl: body.returnUrl,
    },
  });
  const mapped = mapDeposit(res.data);
  if (!mapped) {
    throw new ApiError(res.msg || '充值下单失败，请稍后重试', res.code);
  }
  return mapped;
}

/** GET /app/pay/order?outTradeNo=  会向三方查单并补单 */
export async function fetchAppPayOrder(outTradeNo: string): Promise<AppPayOrder> {
  const res = await request<unknown>(`/app/pay/order?outTradeNo=${encodeURIComponent(outTradeNo)}`);
  const mapped = mapOrder(res.data);
  if (!mapped) {
    throw new ApiError(res.msg || '支付单不存在', res.code);
  }
  return mapped;
}

export function isPayOrderPaid(status: string): boolean {
  return status === '1';
}

export function isPayOrderPending(status: string): boolean {
  return status === '0';
}
