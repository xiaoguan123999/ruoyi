import { request } from '@/api/request';
import type { AppServiceCenter, AppServiceChannel } from '@/api/types';
import { config } from '@/config';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickString(source: Record<string, unknown>, key: string, fallback = ''): string {
  const value = source[key];
  if (value !== undefined && value !== null && String(value).length > 0) {
    return String(value);
  }
  return fallback;
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

function mapChannel(raw: unknown, index: number): AppServiceChannel | null {
  if (!isRecord(raw)) {
    return null;
  }
  const channelId = toNumber(raw.channelId, index + 1);
  const status = pickString(raw, 'status');
  if (status === '1') {
    return null;
  }
  const qrUrl = resolveMediaUrl(pickString(raw, 'qrUrl'));
  const linkUrl = pickString(raw, 'linkUrl');
  const channelType = pickString(raw, 'type') || pickString(raw, 'channelType', 'LINK');
  return {
    channelId,
    channelType,
    name: pickString(raw, 'name', '--'),
    value: pickString(raw, 'value') || undefined,
    linkUrl: linkUrl || undefined,
    qrUrl: qrUrl || undefined,
    remark: pickString(raw, 'remark') || undefined,
    sort: toNumber(raw.sort, index + 1),
  };
}

function mapServiceCenter(raw: unknown): AppServiceCenter | null {
  if (!isRecord(raw)) {
    return null;
  }
  const channelsRaw = Array.isArray(raw.channels) ? raw.channels : [];
  const channels = channelsRaw
    .map(mapChannel)
    .filter((item): item is AppServiceChannel => item !== null)
    .sort((a, b) => a.sort - b.sort);
  return {
    title: pickString(raw, 'title', '客服中心'),
    workTime: pickString(raw, 'workTime'),
    hint: pickString(raw, 'hint'),
    channels,
  };
}

/** GET /app/service — 免登录（登录页也可调） */
export async function fetchAppServiceCenter(): Promise<AppServiceCenter | null> {
  const res = await request<unknown>('/app/service', { withToken: false });
  const root = isRecord(res) ? (res.data ?? res) : null;
  return mapServiceCenter(root);
}
