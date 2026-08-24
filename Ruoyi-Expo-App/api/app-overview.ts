import { request } from '@/api/request';
import type { AppOverviewItem } from '@/api/types';
import { config } from '@/config';
import { images } from '@/constants/images';

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

const FALLBACK_IMAGE: Record<string, number> = {
  satellite: images.statSatellite,
  coverage: images.statGlobe,
  terminal: images.statTerminal,
};

const FALLBACK_BY_INDEX = [images.statSatellite, images.statGlobe, images.statTerminal];

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

function mapOverviewItem(raw: unknown, index: number): AppOverviewItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const itemKey = pickString(raw, ['itemKey', 'key'], `item-${index}`);
  const itemId = pickString(raw, ['itemId', 'id'], itemKey);
  const imageUrl = resolveImageUrl(pickString(raw, ['imageUrl', 'image', 'cover']));
  const fallback =
    FALLBACK_IMAGE[itemKey] ?? FALLBACK_BY_INDEX[index % FALLBACK_BY_INDEX.length] ?? images.statSatellite;

  return {
    id: itemId,
    itemKey,
    title: pickString(raw, ['title', 'name'], '--'),
    displayValue: pickString(raw, ['displayValue', 'value'], '0'),
    statusText: pickString(raw, ['statusText', 'statusLabel'], '--'),
    statusColor: pickString(raw, ['statusColor', 'color'], '#4DA3FF'),
    imageUrl: imageUrl || undefined,
    imageFallback: fallback,
    sort: toNumber(raw.sort, index + 1),
  };
}

export async function fetchAppOverview(): Promise<AppOverviewItem[]> {
  const res = await request<unknown>('/app/overview', { withToken: false });
  return extractList(res as Record<string, unknown>)
    .map(mapOverviewItem)
    .filter((item): item is AppOverviewItem => item !== null)
    .sort((a, b) => a.sort - b.sort);
}
