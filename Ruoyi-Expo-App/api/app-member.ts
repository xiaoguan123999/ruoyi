import { fetchAppProfile } from '@/api/app-auth';
import { request } from '@/api/request';
import type {
  AppInviteInfo,
  AppKycBody,
  AppKycRewardClaimResult,
  AppKycRewardInfo,
  AppLevel,
  AppLevelCurrent,
  AppLevelRewardClaimableItem,
  AppLevelRewardClaimPolicy,
  AppLevelRewardClaimResult,
  AppLevelRewardOption,
  AppLevelsView,
  AppPasswordBody,
  AppPayPasswordBody,
  KycRewardCurrency,
} from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function toString(value: unknown, fallback = ''): string {
  if (value === undefined || value === null) {
    return fallback;
  }
  return String(value);
}

function pickNumber(source: Record<string, unknown>, keys: string[]): number {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null) {
      return toNumber(source[key]);
    }
  }
  return 0;
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

function extractDataRoot(res: Record<string, unknown>): unknown {
  if (res.data !== undefined) {
    return res.data;
  }
  const { code, msg, token, img, uuid, captchaEnabled, captchaOnOff, roles, permissions, rows, total, ...rest } =
    res;
  return Object.keys(rest).length ? rest : null;
}

function extractList(res: Record<string, unknown>): unknown[] {
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  const root = extractDataRoot(res);
  if (Array.isArray(root)) {
    return root;
  }
  if (isRecord(root)) {
    const nested = root.list ?? root.rows ?? root.levels ?? root.records;
    if (Array.isArray(nested)) {
      return nested;
    }
  }
  return [];
}

function mapLevel(raw: unknown): AppLevel | null {
  if (!isRecord(raw)) {
    return null;
  }
  const levelId = pickNumber(raw, ['levelId', 'id']);
  if (!levelId) {
    return null;
  }
  return {
    levelId,
    levelName: pickString(raw, ['levelName', 'name'], `等级${levelId}`),
    minRechargeCny: pickNumber(raw, ['minRechargeCny', 'rechargeCny']),
    minRechargeUsdt: pickNumber(raw, ['minRechargeUsdt', 'rechargeUsdt']),
    minValidMembers: pickNumber(raw, ['minValidMembers', 'validMembers', 'teamCount']),
    teamDepth: pickString(raw, ['teamDepth', 'depth', 'teamRequirement']) || undefined,
    minTeamPerfCny: pickNumber(raw, ['minTeamPerfCny']) || undefined,
    minTeamPerfUsdt: pickNumber(raw, ['minTeamPerfUsdt']) || undefined,
    minTeamRechargeCny: pickNumber(raw, ['minTeamRechargeCny']) || undefined,
    minTeamRechargeUsdt: pickNumber(raw, ['minTeamRechargeUsdt']) || undefined,
    rewardCny: pickNumber(raw, ['rewardCny']) || undefined,
    rewardUsdt: pickNumber(raw, ['rewardUsdt']) || undefined,
    sort: pickNumber(raw, ['sort']),
    status: pickString(raw, ['status']),
    remark: pickString(raw, ['remark', 'desc', 'description']),
  };
}

function mapInvite(raw: unknown): AppInviteInfo {
  if (!isRecord(raw)) {
    return { inviteCode: '' };
  }
  let qrCode = pickString(raw, ['qrCode', 'qrcode', 'qrImg', 'inviteQr']);
  if (qrCode && !qrCode.startsWith('data:') && !qrCode.startsWith('http')) {
    qrCode = `data:image/png;base64,${qrCode}`;
  }
  return {
    inviteCode: pickString(raw, ['inviteCode', 'code', 'invite']),
    inviteUrl: pickString(raw, ['inviteUrl', 'url', 'link', 'inviteLink']) || undefined,
    qrCode: qrCode || undefined,
    inviteCount: pickNumber(raw, ['inviteCount', 'inviteNum', 'count']) || undefined,
    ruleText: pickString(raw, ['ruleText', 'rules', 'rule']) || undefined,
  };
}

export async function fetchAppInvite(): Promise<AppInviteInfo> {
  try {
    const res = await request<unknown>('/app/invite');
    const root = extractDataRoot(res as Record<string, unknown>);
    const invite = mapInvite(root ?? res);
    if (invite.inviteCode) {
      return invite;
    }
  } catch {
  }
  const profile = await fetchAppProfile();
  return {
    inviteCode: profile.inviteCode ?? '',
  };
}

/** POST /app/kyc — 提交即已实名，成功后资料 kycStatus=1 */
export async function submitAppKyc(body: AppKycBody): Promise<string> {
  const res = await request('/app/kyc', {
    method: 'POST',
    body: {
      realName: body.realName.trim(),
      idCard: body.idCard.trim().toUpperCase(),
    },
  });
  await fetchAppProfile().catch(() => {});
  return res.msg || '实名认证提交成功';
}

function pickBoolean(source: Record<string, unknown>, keys: string[]): boolean | undefined {
  for (const key of keys) {
    const value = source[key];
    if (value === undefined || value === null) {
      continue;
    }
    if (typeof value === 'boolean') {
      return value;
    }
    const text = String(value).trim().toLowerCase();
    if (text === 'true' || text === '1' || text === 'yes' || text === 'y') {
      return true;
    }
    if (text === 'false' || text === '0' || text === 'no' || text === 'n') {
      return false;
    }
  }
  return undefined;
}

function normalizeCurrency(value: unknown): KycRewardCurrency | undefined {
  const text = String(value ?? '')
    .trim()
    .toUpperCase();
  if (text === 'CNY' || text === 'USDT') {
    return text;
  }
  return undefined;
}

function mapKycRewardInfo(raw: unknown): AppKycRewardInfo {
  if (!isRecord(raw)) {
    return {
      kycRewardCny: 0,
      kycRewardUsdt: 0,
      kycRewardClaimable: false,
      kycRewardClaimed: false,
    };
  }
  const claimed = pickBoolean(raw, ['kycRewardClaimed', 'claimed']) === true;
  const claimable = pickBoolean(raw, ['kycRewardClaimable', 'claimable']);
  return {
    kycRewardCny: pickNumber(raw, ['kycRewardCny', 'rewardCny', 'cny']),
    kycRewardUsdt: pickNumber(raw, ['kycRewardUsdt', 'rewardUsdt', 'usdt']),
    kycRewardClaimed: claimed,
    kycRewardClaimable: claimable === true,
    claimedCurrency: normalizeCurrency(raw.claimedCurrency ?? raw.currency),
    claimedAmount: pickNumber(raw, ['claimedAmount', 'amount']) || undefined,
  };
}

/**
 * GET /app/kyc/reward — 实名奖励配置与是否可领
 */
export async function fetchAppKycReward(): Promise<AppKycRewardInfo> {
  const res = await request<unknown>('/app/kyc/reward');
  const root = extractDataRoot(res as Record<string, unknown>);
  return mapKycRewardInfo(root ?? res);
}

/**
 * POST /app/kyc/reward — 领取实名奖励（CNY / USDT 任选其一，每人一次）
 */
export async function claimAppKycReward(
  currency: KycRewardCurrency,
): Promise<AppKycRewardClaimResult> {
  const res = await request<unknown>('/app/kyc/reward', {
    method: 'POST',
    body: { currency },
  });
  const root = extractDataRoot(res as Record<string, unknown>);
  const data = isRecord(root) ? root : {};
  const claimedCurrency = normalizeCurrency(data.currency) ?? currency;
  const amount = pickNumber(data, ['amount', 'claimedAmount']);
  return {
    currency: claimedCurrency,
    amount,
    message: '领取成功，已到账',
  };
}

export function formatKycRewardLabel(currency: KycRewardCurrency, amount: number): string {
  return currency === 'CNY' ? `${amount}元` : `${amount}U`;
}

export async function updateAppPassword(body: AppPasswordBody): Promise<string> {
  const res = await request('/app/password', {
    method: 'PUT',
    body: {
      oldPassword: body.oldPassword,
      newPassword: body.newPassword,
      confirmPassword: body.confirmPassword,
    },
  });
  return res.msg || '密码修改成功';
}

/**
 * POST /app/payPassword（别名 /app/tradePassword）
 * 未设置：只需 newPassword / payPassword；已设置：需 oldPassword + newPassword + confirmPassword
 */
export async function saveAppPayPassword(body: AppPayPasswordBody): Promise<string> {
  const newPassword = body.newPassword.trim();
  const res = await request('/app/payPassword', {
    method: 'POST',
    body: {
      oldPassword: body.oldPassword?.trim() || undefined,
      newPassword,
      confirmPassword: body.confirmPassword?.trim() || undefined,
      payPassword: newPassword,
    },
  });
  await fetchAppProfile().catch(() => {});
  return res.msg || (body.oldPassword ? '支付密码修改成功' : '支付密码设置成功');
}

/** 老账号首次设置支付密码（认购等场景） */
export async function setAppPayPassword(payPassword: string): Promise<string> {
  return saveAppPayPassword({ newPassword: payPassword });
}

function mapLevelCurrent(raw: unknown): AppLevelCurrent {
  if (!isRecord(raw)) {
    return {};
  }
  // 会员对象上不要回退到 id（那是 memberId）
  const levelId = pickNumber(raw, ['levelId']);
  const levelName = pickString(raw, ['levelName', 'memberLevelName', 'level']);
  return {
    levelId: levelId || undefined,
    levelName: levelName || undefined,
  };
}

function mapLevelRewardOption(raw: unknown): AppLevelRewardOption | null {
  if (!isRecord(raw)) {
    return null;
  }
  const currency = normalizeCurrency(raw.currency);
  if (!currency) {
    return null;
  }
  return {
    currency,
    amount: pickNumber(raw, ['amount']),
  };
}

function mapLevelRewardClaimableItem(raw: unknown): AppLevelRewardClaimableItem | null {
  if (!isRecord(raw)) {
    return null;
  }
  const levelId = pickNumber(raw, ['levelId']);
  if (!levelId) {
    return null;
  }
  const options = (Array.isArray(raw.options) ? raw.options : [])
    .map(mapLevelRewardOption)
    .filter((item): item is AppLevelRewardOption => item !== null);
  if (options.length === 0) {
    return null;
  }
  const claimPolicyRaw = pickString(raw, ['claimPolicy']).toUpperCase();
  const claimPolicy: AppLevelRewardClaimPolicy = claimPolicyRaw === 'ALL' ? 'ALL' : 'ONE';
  const claimedCurrencies = (Array.isArray(raw.claimedCurrencies) ? raw.claimedCurrencies : [])
    .map((item) => String(item).trim().toUpperCase())
    .filter((item) => item.length > 0);
  return {
    levelId,
    levelName: pickString(raw, ['levelName']),
    claimPolicy,
    walletTypeCode: pickString(raw, ['walletTypeCode']),
    options,
    claimedCurrencies,
  };
}

function mapClaimableList(raw: unknown): AppLevelRewardClaimableItem[] {
  const rows = Array.isArray(raw)
    ? raw
    : isRecord(raw) && Array.isArray(raw.items)
      ? raw.items
      : [];
  return rows
    .map(mapLevelRewardClaimableItem)
    .filter((item): item is AppLevelRewardClaimableItem => item !== null);
}

export function emptyLevelsView(): AppLevelsView {
  return { current: {}, levels: [], claimable: [], hint: '', ruleText: '' };
}

/** GET /app/levelReward/claimable */
export async function fetchAppLevelRewardClaimable(): Promise<AppLevelRewardClaimableItem[]> {
  const res = await request<unknown>('/app/levelReward/claimable');
  const root = extractDataRoot(res as Record<string, unknown>);
  return mapClaimableList(root);
}

/** POST /app/levelReward/claim */
export async function claimAppLevelReward(
  levelId: number,
  currency: KycRewardCurrency,
): Promise<AppLevelRewardClaimResult> {
  const res = await request<unknown>('/app/levelReward/claim', {
    method: 'POST',
    body: { levelId, currency },
  });
  const root = extractDataRoot(res as Record<string, unknown>);
  const data = isRecord(root) ? root : {};
  return {
    levelId: pickNumber(data, ['levelId']) || levelId,
    levelName: pickString(data, ['levelName']),
    currency: normalizeCurrency(data.currency) ?? currency,
    amount: pickNumber(data, ['amount']),
    walletTypeCode: pickString(data, ['walletTypeCode']),
    message: res.msg || '领取成功，已到账',
  };
}

export async function fetchAppLevelsView(): Promise<AppLevelsView> {
  const res = await request<unknown>('/app/levels');
  const root = extractDataRoot(res as Record<string, unknown>);

  if (isRecord(root)) {
    const levels = (Array.isArray(root.levels) ? root.levels : extractList(res as Record<string, unknown>))
      .map(mapLevel)
      .filter((item): item is AppLevel => item !== null)
      .sort((a, b) => (a.sort ?? a.levelId) - (b.sort ?? b.levelId));

    return {
      current: mapLevelCurrent(root.current ?? root.member ?? root.profile),
      levels,
      claimable: mapClaimableList(root.claimable),
      hint: pickString(root, ['hint', 'note']),
      ruleText: pickString(root, ['ruleText', 'rules', 'rule']),
    };
  }

  return {
    current: {},
    levels: extractList(res as Record<string, unknown>)
      .map(mapLevel)
      .filter((item): item is AppLevel => item !== null)
      .sort((a, b) => (a.sort ?? a.levelId) - (b.sort ?? b.levelId)),
    claimable: [],
    hint: '',
    ruleText: '',
  };
}

export async function fetchAppLevels(): Promise<AppLevel[]> {
  const view = await fetchAppLevelsView();
  return view.levels;
}
