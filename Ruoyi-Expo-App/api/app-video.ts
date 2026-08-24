import { request } from '@/api/request';
import type { AppVideoCarouselItem } from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    const v = source[key];
    if (v !== undefined && v !== null && String(v).length > 0) {
      return String(v);
    }
  }
  return fallback;
}

function pickNumber(source: Record<string, unknown>, keys: string[], fallback = 0): number {
  for (const key of keys) {
    const v = source[key];
    if (v !== undefined && v !== null) {
      return toNumber(v, fallback);
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

function mapVideoItem(raw: unknown): AppVideoCarouselItem | null {
  if (!isRecord(raw)) {
    return null;
  }

  const id = pickString(raw, ['carouselId', 'id', 'videoId', 'bannerId']);
  if (!id) {
    return null;
  }

  const coverUrl = pickString(raw, ['coverUrl', 'imageUrl', 'posterUrl', 'thumbUrl', 'thumbnailUrl']);
  const videoUrl = pickString(raw, ['videoUrl', 'url', 'video']);
  if (!coverUrl && !videoUrl) {
    return null;
  }

  return {
    id,
    title: pickString(raw, ['title', 'name']) || undefined,
    videoUrl: videoUrl || undefined,
    coverUrl: coverUrl || undefined,
    sort: pickNumber(raw, ['sort', 'order', 'priority'], 0),
  };
}

/** App-视频轮播：GET /app/video */
export async function fetchAppVideoCarousel(): Promise<AppVideoCarouselItem[]> {
  const res = await request<unknown>('/app/video', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapVideoItem)
    .filter((item): item is AppVideoCarouselItem => item !== null)
    .sort((a, b) => a.sort - b.sort);
}
