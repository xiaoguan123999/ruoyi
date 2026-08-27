import { request } from '@/api/request';
import type { AppAbout } from '@/api/types';
import { config } from '@/config';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
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

function mapAbout(raw: unknown): AppAbout | null {
  if (!isRecord(raw)) {
    return null;
  }
  const modeRaw = pickString(raw, 'mode', 'TEXT').toUpperCase();
  const mode: AppAbout['mode'] = modeRaw === 'PDF' ? 'PDF' : 'TEXT';
  const imageUrl = resolveMediaUrl(pickString(raw, 'imageUrl'));
  const pdfUrl = resolveMediaUrl(pickString(raw, 'pdfUrl'));
  return {
    mode,
    title: pickString(raw, 'title'),
    subtitle: pickString(raw, 'subtitle'),
    content: pickString(raw, 'content'),
    imageUrl: imageUrl || undefined,
    pdfUrl: pdfUrl || undefined,
  };
}

/** GET /app/about — 全局单条，免登录 */
export async function fetchAppAbout(): Promise<AppAbout | null> {
  const res = await request<unknown>('/app/about', { withToken: false });
  const root = isRecord(res) ? (res.data ?? res) : null;
  // 兼容旧数组：取首条
  if (Array.isArray(root)) {
    return mapAbout(root[0] ?? null);
  }
  return mapAbout(root);
}
