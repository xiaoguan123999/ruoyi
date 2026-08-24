import { request } from '@/api/request';
import type { AppNotice, AppNoticeDetail } from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toString(value: unknown, fallback = ''): string {
  if (value === undefined || value === null) {
    return fallback;
  }
  return String(value);
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

function pickId(source: Record<string, unknown>): string {
  const raw = source.noticeId ?? source.id;
  return raw === undefined || raw === null ? '' : String(raw);
}

function formatNoticeTime(value: unknown): string {
  const raw = toString(value).trim();
  if (!raw) {
    return '--';
  }
  // 2026-08-19T22:48:36.000+08:00 → 2026-08-19
  if (raw.includes('T')) {
    return raw.slice(0, 10);
  }
  return raw.slice(0, 19).replace('T', ' ');
}

/** RuoYi 公告内容可能是富文本 HTML，RN Text 仅展示纯文本 */
export function stripNoticeHtml(html: string): string {
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\n{3,}/g, '\n\n')
    .trim();
}

function mapNotice(raw: unknown): AppNotice | null {
  if (!isRecord(raw)) {
    return null;
  }
  const id = pickId(raw);
  if (!id) {
    return null;
  }
  return {
    id,
    title: pickString(raw, ['noticeTitle', 'title'], '--'),
    createTime: formatNoticeTime(raw.createTime ?? raw.create_time),
  };
}

function mapNoticeDetail(raw: unknown): AppNoticeDetail | null {
  const base = mapNotice(raw);
  if (!base || !isRecord(raw)) {
    return null;
  }
  const contentRaw = pickString(raw, ['noticeContent', 'content', 'remark']);
  return {
    ...base,
    content: stripNoticeHtml(contentRaw),
  };
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

export async function fetchAppNotices(): Promise<AppNotice[]> {
  const res = await request<unknown>('/app/notices', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapNotice)
    .filter((item): item is AppNotice => item !== null);
}

export async function fetchAppNoticeDetail(noticeId: string): Promise<AppNoticeDetail | null> {
  if (!noticeId) {
    return null;
  }
  const res = await request<unknown>(`/app/notices/${encodeURIComponent(noticeId)}`, {
    withToken: false,
  });
  const data = (res as { data?: unknown }).data ?? res;
  return mapNoticeDetail(data);
}
