import { displayText, maskPhone } from '@/api/app-auth';
import { ApiError, request } from '@/api/request';
import type {
  AppTeamDepositSummary,
  AppTeamLevelNo,
  AppTeamLevelStats,
  AppTeamMemberItem,
  AppTeamMembersByLevel,
  AppTeamSummary,
  AppTeamView,
} from '@/api/types';

export const TEAM_LEVEL_NOS: AppTeamLevelNo[] = [1, 2, 3, 4, 5, 6, 7];

const EMPTY_STATS: AppTeamLevelStats = {
  register: 0,
  active: 0,
  subscribeUsd: 0,
  subscribeCny: 0,
  rechargeUsd: 0,
  rechargeCny: 0,
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

function emptySummary(): AppTeamSummary {
  return {
    level1: { ...EMPTY_STATS },
    level2: { ...EMPTY_STATS },
    level3: { ...EMPTY_STATS },
    level4: { ...EMPTY_STATS },
    level5: { ...EMPTY_STATS },
    level6: { ...EMPTY_STATS },
    level7: { ...EMPTY_STATS },
  };
}

function emptyMembers(): AppTeamMembersByLevel {
  return {
    1: [],
    2: [],
    3: [],
    4: [],
    5: [],
    6: [],
    7: [],
  };
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

function parseLevelNo(value: unknown): AppTeamLevelNo | null {
  const level = toNumber(value, 0);
  if (level >= 1 && level <= 7) {
    return level as AppTeamLevelNo;
  }
  return null;
}

function mapSummary(root: Record<string, unknown>): AppTeamSummary {
  const summary = root.summary ?? root.stats ?? root.statistics;
  const result = emptySummary();

  if (Array.isArray(summary)) {
    for (const item of summary) {
      if (!isRecord(item)) {
        continue;
      }
      const level = parseLevelNo(item.teamLevel ?? item.level ?? item.levelNo ?? item.depth);
      if (level) {
        result[`level${level}`] = mapLevelStats(item);
      }
    }
    return result;
  }

  if (isRecord(summary)) {
    for (const level of TEAM_LEVEL_NOS) {
      result[`level${level}`] = mapLevelStats(
        summary[`level${level}`] ?? summary[String(level)],
      );
    }
    return result;
  }

  // 兼容旧结构：无 summary 时从根节点读 level1…levelN 对象
  for (const level of TEAM_LEVEL_NOS) {
    const candidate =
      root[`level${level}`] ??
      root[`level${level}Stats`] ??
      root[`stats${level}`];
    if (isRecord(candidate) && !Array.isArray(candidate)) {
      result[`level${level}`] = mapLevelStats(candidate);
    }
  }
  return result;
}

function mapMemberItem(raw: unknown): AppTeamMemberItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const phone = String(raw.phone ?? raw.mobile ?? raw.userName ?? '').trim();
  const name = String(raw.realName ?? raw.name ?? raw.nickName ?? raw.userName ?? phone).trim();
  if (!phone && !name) {
    return null;
  }
  return {
    memberId: raw.memberId !== undefined ? toNumber(raw.memberId) : undefined,
    name: displayText(name),
    phone: displayText(phone ? maskPhone(phone) : undefined),
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
  level: AppTeamLevelNo,
  item: AppTeamMemberItem,
): void {
  bucket[level].push(item);
}

function mapMembers(root: Record<string, unknown>): AppTeamMembersByLevel {
  const members = emptyMembers();

  // 新结构：data.members["1"] … data.members["7"]
  const list = root.members ?? root.memberList ?? root.list ?? root.rows;
  if (Array.isArray(list)) {
    for (const raw of list) {
      const item = mapMemberItem(raw);
      if (!item || !isRecord(raw)) {
        continue;
      }
      const level = parseLevelNo(raw.teamLevel ?? raw.level ?? raw.levelNo ?? raw.depth);
      if (level) {
        pushMember(members, level, item);
      }
    }
    return members;
  }

  if (isRecord(list)) {
    for (const [key, value] of Object.entries(list)) {
      if (!Array.isArray(value)) {
        continue;
      }
      const level = parseLevelNo(key.replace(/\D/g, ''));
      if (!level) {
        continue;
      }
      for (const raw of value) {
        const item = mapMemberItem(raw);
        if (item) {
          pushMember(members, level, item);
        }
      }
    }
    return members;
  }

  // 兼容旧结构：data.level1 … data.level7 直接是成员数组
  for (const level of TEAM_LEVEL_NOS) {
    const keyed =
      root[`level${level}`] ??
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
    deposit: mapDepositSummary(root),
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

function emptyDepositSummary(): AppTeamDepositSummary {
  return {
    selfDepositAmountCny: 0,
    selfDepositAmountUsdt: 0,
    downlineDepositAmountCny: 0,
    downlineDepositAmountUsdt: 0,
    totalDepositAmountCny: 0,
    totalDepositAmountUsdt: 0,
  };
}

function mapDepositSummary(raw: unknown): AppTeamDepositSummary {
  const next = emptyDepositSummary();
  if (!isRecord(raw)) {
    return next;
  }
  next.selfDepositAmountCny = toNumber(raw.selfDepositAmountCny);
  next.selfDepositAmountUsdt = toNumber(raw.selfDepositAmountUsdt);
  next.downlineDepositAmountCny = toNumber(raw.downlineDepositAmountCny);
  next.downlineDepositAmountUsdt = toNumber(raw.downlineDepositAmountUsdt);
  next.totalDepositAmountCny = toNumber(raw.totalDepositAmountCny);
  next.totalDepositAmountUsdt = toNumber(raw.totalDepositAmountUsdt);
  return next;
}

export function emptyTeamView(): AppTeamView {
  return {
    summary: emptySummary(),
    members: emptyMembers(),
    deposit: emptyDepositSummary(),
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
