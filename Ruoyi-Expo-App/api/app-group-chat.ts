import { request } from '@/api/request';
import type { AppGroupChatItem } from '@/api/types';
import { config } from '@/config';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
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
    const nested = res.data.list ?? res.data.rows ?? res.data.records;
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

/** 后台多为上传的二维码图片；若是普通链接则前端生成二维码 */
export function isGroupQrImageUrl(url: string): boolean {
  const raw = url.trim().toLowerCase();
  if (!raw) {
    return false;
  }
  if (raw.startsWith('data:image')) {
    return true;
  }
  if (/\.(png|jpe?g|gif|webp|bmp|svg)(\?|#|$)/i.test(raw)) {
    return true;
  }
  // 若依本地上传路径
  if (raw.includes('/profile/') || raw.includes('/upload/') || raw.includes('/common/')) {
    return true;
  }
  return false;
}

function mapGroupChat(raw: unknown, index: number): AppGroupChatItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickString(raw, ['groupId', 'id'], String(index + 1));
  if (!id) {
    return null;
  }
  const qrRaw = pickString(raw, ['qrUrl', 'qrCode', 'qrImg', 'imageUrl']);
  return {
    id,
    title: pickString(raw, ['title', 'name'], '--'),
    hint: pickString(raw, ['hint', 'remark'], '--'),
    qrUrl: resolveMediaUrl(qrRaw) || undefined,
    remark: pickString(raw, ['remark']),
    sort: toNumber(raw.sort, index + 1),
  };
}

export async function fetchAppGroupChat(): Promise<AppGroupChatItem[]> {
  const res = await request<unknown>('/app/group-chat', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapGroupChat)
    .filter((item): item is AppGroupChatItem => item !== null)
    .sort((a, b) => a.sort - b.sort);
}
