import { fetchAppProfile } from '@/api/app-auth';
import { request } from '@/api/request';
import type { AppInviteInfo, AppKycBody, AppLevel } from '@/api/types';

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

export async function submitAppKyc(body: AppKycBody): Promise<string> {
  const res = await request('/app/kyc', {
    method: 'POST',
    body: {
      realName: body.realName.trim(),
      idCard: body.idCard.trim(),
    },
  });
  await fetchAppProfile().catch(() => {});
  return res.msg || '实名认证提交成功';
}

export async function fetchAppLevels(): Promise<AppLevel[]> {
  const res = await request<unknown>('/app/levels');
  return extractList(res as Record<string, unknown>)
    .map(mapLevel)
    .filter((item): item is AppLevel => item !== null)
    .sort((a, b) => (a.sort ?? a.levelId) - (b.sort ?? b.levelId));
}
