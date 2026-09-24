import { ApiError, request } from '@/api/request';
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

export type AppLotteryPrize = {
  prizeId: number;
  prizeName: string;
  prizeDesc: string;
  imageUrl: string;
  sectorBgColor: string;
  nameColor: string;
  descColor: string;
  prizeType: number;
  position: number;
  isFallback?: string;
};

export type AppLotteryCurrent = {
  activityId: number;
  title: string;
  intervalHours: number;
  ruleText: string;
  drawCount: number;
  chanceBalance: number;
  canDraw: boolean;
  nextDrawTime?: string;
  prizes: AppLotteryPrize[];
};

export type AppLotteryDrawResult = {
  prizeId?: number;
  prizeName?: string;
  prizeType?: number;
  position?: number;
  isMeltdown: boolean;
  isStrategy: boolean;
  msg: string;
};

export type AppLotteryRecord = {
  recordId: number;
  prizeId?: number;
  prizeName: string;
  prizeType?: number;
  drawCount: number;
  grantStatus: string;
  grantStatusLabel: string;
  createTime?: string;
  isMeltdown?: string;
};

const GRANT_STATUS_LABEL: Record<string, string> = {
  '0': '待处理',
  '1': '已入账',
  '2': '待领取',
  '3': '已关闭',
};

function normalizePrize(raw: Record<string, unknown>, index: number): AppLotteryPrize {
  return {
    prizeId: toNumber(raw.prizeId),
    prizeName: pickString(raw, ['prizeName'], `奖品${index + 1}`),
    prizeDesc: pickString(raw, ['prizeDesc']),
    imageUrl: resolveMediaUrl(pickString(raw, ['imageUrl'])),
    sectorBgColor: pickString(raw, ['sectorBgColor']),
    nameColor: pickString(raw, ['nameColor']),
    descColor: pickString(raw, ['descColor']),
    prizeType: toNumber(raw.prizeType),
    position: toNumber(raw.position, index + 1),
    isFallback: pickString(raw, ['isFallback']) || undefined,
  };
}

function isNoActiveLottery(error: unknown): boolean {
  return error instanceof ApiError && (error.message || '').includes('暂无进行中的抽奖活动');
}

/** GET /app/lottery/current；无进行中活动时返回 null */
export async function fetchLotteryCurrent(): Promise<AppLotteryCurrent | null> {
  let res: unknown;
  try {
    res = await request<unknown>('/app/lottery/current');
  } catch (error) {
    if (isNoActiveLottery(error)) {
      return null;
    }
    throw error;
  }
  if (!isRecord(res) || Number(res.code) !== 200 || !isRecord(res.data)) {
    return null;
  }
  const data = res.data;
  const rawPrizes = Array.isArray(data.prizes) ? data.prizes : [];
  const prizes: AppLotteryPrize[] = rawPrizes
    .filter(isRecord)
    .map((p, index) => normalizePrize(p, index))
    .sort((a, b) => a.position - b.position);

  return {
    activityId: toNumber(data.activityId),
    title: pickString(data, ['title']),
    intervalHours: toNumber(data.intervalHours, 72),
    ruleText: pickString(data, ['ruleText']),
    drawCount: toNumber(data.drawCount),
    chanceBalance: toNumber(data.chanceBalance),
    canDraw: Boolean(data.canDraw),
    nextDrawTime: pickString(data, ['nextDrawTime']) || undefined,
    prizes,
  };
}

/** POST /app/lottery/draw */
export async function drawLottery(): Promise<AppLotteryDrawResult> {
  const res = await request<unknown>('/app/lottery/draw', { method: 'POST' });
  if (!isRecord(res) || !isRecord(res.data)) {
    return {
      isMeltdown: false,
      isStrategy: false,
      msg: pickString(isRecord(res) ? res : {}, ['msg'], '抽奖失败'),
    };
  }
  const data = res.data;
  return {
    prizeId: data.prizeId != null ? toNumber(data.prizeId) : undefined,
    prizeName: pickString(data, ['prizeName']) || undefined,
    prizeType: data.prizeType != null ? toNumber(data.prizeType) : undefined,
    position: data.position != null ? toNumber(data.position) : undefined,
    isMeltdown: Boolean(data.isMeltdown),
    isStrategy: Boolean(data.isStrategy),
    msg: pickString(data, ['msg']),
  };
}

/** GET /app/lottery/records */
export async function fetchLotteryRecords(options?: {
  pageNum?: number;
  pageSize?: number;
}): Promise<AppLotteryRecord[]> {
  const pageNum = options?.pageNum ?? 1;
  const pageSize = options?.pageSize ?? 50;
  const qs = `pageNum=${pageNum}&pageSize=${pageSize}`;
  const res = await request<unknown>(`/app/lottery/records?${qs}`);
  if (!isRecord(res)) {
    return [];
  }
  const rows = Array.isArray(res.rows)
    ? res.rows
    : isRecord(res.data) && Array.isArray(res.data.rows)
      ? res.data.rows
      : Array.isArray(res.data)
        ? res.data
        : [];

  return rows.filter(isRecord).map((row) => {
    const grantStatus = pickString(row, ['grantStatus'], '0');
    return {
      recordId: toNumber(row.recordId),
      prizeId: row.prizeId != null ? toNumber(row.prizeId) : undefined,
      prizeName: pickString(row, ['prizeName'], '—'),
      prizeType: row.prizeType != null ? toNumber(row.prizeType) : undefined,
      drawCount: toNumber(row.drawCount),
      grantStatus,
      grantStatusLabel: GRANT_STATUS_LABEL[grantStatus] ?? grantStatus,
      createTime: pickString(row, ['createTime']) || undefined,
      isMeltdown: pickString(row, ['isMeltdown']) || undefined,
    };
  });
}
