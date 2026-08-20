import { maskPhone } from '@/api/app-auth';
import { ApiError, request } from '@/api/request';
import type {
  AppTeamLevelStats,
  AppTeamMemberItem,
  AppTeamMembersByLevel,
  AppTeamSummary,
  AppTeamView,
} from '@/api/types';

const EMPTY_STATS: AppTeamLevelStats = {
  register: 0,
  active: 0,
  subscribeUsd: 0,
  subscribeCny: 0,
  rechargeUsd: 0,
  rechargeCny: 0,
};

const EMPTY_SUMMARY: AppTeamSummary = {
  level1: { ...EMPTY_STATS },
  level2: { ...EMPTY_STATS },
  level3: { ...EMPTY_STATS },
};

const EMPTY_MEMBERS: AppTeamMembersByLevel = {
  1: [],
  2: [],
  3: [],
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickNumber(source: Record<string, unknown>, keys: string[]): number {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null) {
      return toNumber(source[key]);
    }
  }
  return 0;
}

function extractTeamRoot(res: Record<string, unknown>): Record<string, unknown> {
  if (isRecord(res.data)) {
    return res.data;
  }
  const { code, msg, token, img, uuid, captchaEnabled, captchaOnOff, roles, permissions, ...rest } =
    res;
  if (Object.keys(rest).length > 0) {
    return rest;
  }
  return {};
}

function mapLevelStats(raw: unknown): AppTeamLevelStats {
  if (!isRecord(raw)) {
    return { ...EMPTY_STATS };
  }
  return {
    register: pickNumber(raw, [
      'register',
      'registerCount',
      'regCount',
      'registerNum',
      'totalRegister',
      'registerTotal',
    ]),
    active: pickNumber(raw, [
      'active',
      'activeCount',
      'activateCount',
      'activeNum',
      'kycCount',
      'validCount',
    ]),
    subscribeUsd: pickNumber(raw, [
      'subscribeUsd',
      'subscribeUsdt',
      'orderUsdt',
      'subscribeAmountUsdt',
      'orderAmountUsdt',
    ]),
    subscribeCny: pickNumber(raw, [
      'subscribeCny',
      'orderCny',
      'subscribeAmountCny',
      'orderAmountCny',
    ]),
    rechargeUsd: pickNumber(raw, [
      'rechargeUsd',
      'rechargeUsdt',
      'totalRechargeUsdt',
      'rechargeAmountUsdt',
    ]),
    rechargeCny: pickNumber(raw, [
      'rechargeCny',
      'totalRechargeCny',
      'rechargeAmountCny',
    ]),
  };
}

function getLevelObject(root: Record<string, unknown>, level: 1 | 2 | 3): Record<string, unknown> {
  const candidates = [
    root[`level${level}`],
    root[`level${level}Stats`],
    root[`stats${level}`],
    root[String(level)],
  ];
  for (const item of candidates) {
    if (isRecord(item)) {
      return item;
    }
  }
  return {};
}

function mapSummary(root: Record<string, unknown>): AppTeamSummary {
  const summary = root.summary ?? root.stats ?? root.statistics;
  if (Array.isArray(summary)) {
    const byLevel: Partial<Record<1 | 2 | 3, AppTeamLevelStats>> = {};
    for (const item of summary) {
      if (!isRecord(item)) {
        continue;
      }
      const level = toNumber(item.teamLevel ?? item.level ?? item.levelNo ?? item.depth, 0) as
        | 1
        | 2
        | 3;
      if (level >= 1 && level <= 3) {
        byLevel[level] = mapLevelStats(item);
      }
    }
    return {
      level1: byLevel[1] ?? { ...EMPTY_STATS },
      level2: byLevel[2] ?? { ...EMPTY_STATS },
      level3: byLevel[3] ?? { ...EMPTY_STATS },
    };
  }

  if (isRecord(summary)) {
    return {
      level1: mapLevelStats(summary.level1 ?? summary['1'] ?? summary.one),
      level2: mapLevelStats(summary.level2 ?? summary['2'] ?? summary.two),
      level3: mapLevelStats(summary.level3 ?? summary['3'] ?? summary.three),
    };
  }

  return {
    level1: mapLevelStats(getLevelObject(root, 1)),
    level2: mapLevelStats(getLevelObject(root, 2)),
    level3: mapLevelStats(getLevelObject(root, 3)),
  };
}

function mapMemberItem(raw: unknown): AppTeamMemberItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const phone = String(raw.phone ?? raw.mobile ?? raw.userName ?? '').trim();
  const name = String(raw.realName ?? raw.name ?? raw.nickName ?? raw.userName ?? phone ?? '—').trim();
  if (!phone && !name) {
    return null;
  }
  return {
    memberId: raw.memberId !== undefined ? toNumber(raw.memberId) : undefined,
    name: name || '—',
    phone: phone ? maskPhone(phone) : '—',
    usd: pickNumber(raw, [
      'usd',
      'usdt',
      'rechargeUsdt',
      'totalRechargeUsdt',
      'rechargeAmountUsdt',
      'totalRechargeUsd',
    ]),
    cny: pickNumber(raw, [
      'cny',
      'rechargeCny',
      'totalRechargeCny',
      'rechargeAmountCny',
    ]),
  };
}

function pushMember(
  bucket: AppTeamMembersByLevel,
  level: 1 | 2 | 3,
  item: AppTeamMemberItem,
): void {
  if (level >= 1 && level <= 3) {
    bucket[level].push(item);
  }
}

function mapMembers(root: Record<string, unknown>): AppTeamMembersByLevel {
  const members: AppTeamMembersByLevel = {
    1: [],
    2: [],
    3: [],
  };

  const list = root.members ?? root.memberList ?? root.list ?? root.rows;
  if (Array.isArray(list)) {
    for (const raw of list) {
      const item = mapMemberItem(raw);
      if (!item || !isRecord(raw)) {
        continue;
      }
      const level = toNumber(raw.teamLevel ?? raw.level ?? raw.levelNo ?? raw.depth, 1) as 1 | 2 | 3;
      pushMember(members, level, item);
    }
    return members;
  }

  if (isRecord(list)) {
    for (const [key, value] of Object.entries(list)) {
      if (!Array.isArray(value)) {
        continue;
      }
      const level = toNumber(key.replace(/\D/g, ''), 0) as 1 | 2 | 3;
      for (const raw of value) {
        const item = mapMemberItem(raw);
        if (item) {
          pushMember(members, level, item);
        }
      }
    }
    return members;
  }

  for (const level of [1, 2, 3] as const) {
    const keyed =
      root[`members${level}`] ??
      root[`memberList${level}`] ??
      root[`list${level}`] ??
      root[`level${level}Members`];
    if (!Array.isArray(keyed)) {
      continue;
    }
    for (const raw of keyed) {
      const item = mapMemberItem(raw);
      if (item) {
        members[level].push(item);
      }
    }
  }

  return members;
}

function mapTeamView(res: Record<string, unknown>): AppTeamView {
  const root = extractTeamRoot(res);
  return {
    summary: mapSummary(root),
    members: mapMembers(root),
  };
}

export async function fetchAppTeam(): Promise<AppTeamView> {
  const res = await request<Record<string, unknown>>('/app/team');
  const root = extractTeamRoot(res as Record<string, unknown>);
  if (!Object.keys(root).length && !res.data) {
    throw new ApiError(res.msg || '获取团队数据失败', res.code);
  }
  return mapTeamView(res as Record<string, unknown>);
}

export function emptyTeamView(): AppTeamView {
  return {
    summary: {
      level1: { ...EMPTY_STATS },
      level2: { ...EMPTY_STATS },
      level3: { ...EMPTY_STATS },
    },
    members: {
      1: [...EMPTY_MEMBERS[1]],
      2: [...EMPTY_MEMBERS[2]],
      3: [...EMPTY_MEMBERS[3]],
    },
  };
}

export function formatTeamAmount(value: number): string {
  if (!Number.isFinite(value)) {
    return '0';
  }
  if (Number.isInteger(value)) {
    return String(value);
  }
  return value.toFixed(2).replace(/\.?0+$/, '');
}
