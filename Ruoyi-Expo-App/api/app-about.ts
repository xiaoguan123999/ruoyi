import { stripNoticeHtml } from '@/api/app-notice';
import { request } from '@/api/request';
import type { AppAboutItem } from '@/api/types';
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

function resolveImageUrl(raw: string): string {
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

function mapAboutItem(raw: unknown, index: number): AppAboutItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickString(raw, ['aboutId', 'id'], String(index + 1));
  if (!id) {
    return null;
  }
  const imageUrl = resolveImageUrl(pickString(raw, ['imageUrl', 'image', 'cover']));
  return {
    id,
    title: pickString(raw, ['title', 'name'], '--'),
    subtitle: pickString(raw, ['subtitle', 'subTitle', 'slogan']),
    content: stripNoticeHtml(pickString(raw, ['content', 'remark', 'desc', 'description'])),
    imageUrl: imageUrl || undefined,
    sort: toNumber(raw.sort, index + 1),
  };
}

export async function fetchAppAbout(): Promise<AppAboutItem[]> {
  const res = await request<unknown>('/app/about', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapAboutItem)
    .filter((item): item is AppAboutItem => item !== null)
    .sort((a, b) => a.sort - b.sort);
}
