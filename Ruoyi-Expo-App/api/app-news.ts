import { stripNoticeHtml } from '@/api/app-notice';
import { request } from '@/api/request';
import type { AppNewsDetail, AppNewsItem } from '@/api/types';
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

function formatNewsDate(source: Record<string, unknown>): string {
  const raw = pickString(source, ['publishDate', 'publishTime', 'createTime']);
  if (!raw) {
    return '--';
  }
  if (raw.includes('T')) {
    return raw.slice(0, 10);
  }
  return raw.slice(0, 10);
}

function mapNewsItem(raw: unknown, index: number): AppNewsItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickString(raw, ['newsId', 'id'], String(index + 1));
  if (!id) {
    return null;
  }
  const coverUrl = resolveMediaUrl(pickString(raw, ['coverUrl', 'cover', 'imageUrl']));
  return {
    id,
    title: pickString(raw, ['title', 'name'], '--'),
    summary: pickString(raw, ['summary', 'remark', 'desc'], ''),
    coverUrl: coverUrl || undefined,
    publishDate: formatNewsDate(raw),
    sort: toNumber(raw.sort, index + 1),
  };
}

function mapNewsDetail(raw: unknown): AppNewsDetail | null {
  const base = mapNewsItem(raw, 0);
  if (!base || !isRecord(raw)) {
    return null;
  }
  return {
    ...base,
    content: stripNoticeHtml(pickString(raw, ['content', 'body', 'remark'])),
  };
}

export async function fetchAppNews(): Promise<AppNewsItem[]> {
  const res = await request<unknown>('/app/news', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapNewsItem)
    .filter((item): item is AppNewsItem => item !== null)
    .sort((a, b) => a.sort - b.sort);
}

export async function fetchAppNewsDetail(newsId: string): Promise<AppNewsDetail | null> {
  if (!newsId) {
    return null;
  }
  const res = await request<unknown>(`/app/news/${encodeURIComponent(newsId)}`, {
    withToken: false,
  });
  const data = (res as { data?: unknown }).data ?? res;
  return mapNewsDetail(data);
}
