import {
  clearCurrentUser,
  markAuthenticated,
  notifyAuthChanged,
  setCurrentUser,
} from '@/api/auth-state';
import { ApiError, request } from '@/api/request';
import type { AppLoginBody, AppMember, AppRegisterBody, RuoyiUser } from '@/api/types';
import { removeToken, setToken } from '@/utils/storage';

export type AppCaptchaResult = {
  enabled: boolean;
  uuid: string;
  /** 明文验证码，展示在输入框旁，点击可刷新 */
  text: string;
};

function extractMember(res: { data?: AppMember; user?: AppMember }): AppMember | undefined {
  if (res.data && typeof res.data === 'object' && 'memberId' in res.data) {
    return res.data;
  }
  if (res.user && typeof res.user === 'object' && 'memberId' in res.user) {
    return res.user;
  }
  if (res.data && typeof res.data === 'object') {
    const data = res.data as Record<string, unknown>;
    if ('cnyAvailable' in data || 'usdtAvailable' in data || 'phone' in data) {
      return res.data;
    }
  }
  return undefined;
}

function toBalanceNumber(value: unknown): number | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const next = Number(value);
  return Number.isFinite(next) ? next : undefined;
}

function mapMember(member: AppMember): RuoyiUser {
  const raw = member as AppMember & Record<string, unknown>;
  const kycRaw = raw.kycStatus ?? raw.realNameStatus ?? raw.authStatus ?? raw.isKyc;
  const realName = String(member.realName ?? raw.realName ?? '').trim() || undefined;
  const idCard = String(member.idCard ?? raw.idCard ?? raw.idNo ?? '').trim() || undefined;
  return {
    userId: member.memberId,
    userName: member.phone,
    nickName: realName || member.phone,
    realName,
    idCard,
    phone: member.phone,
    avatar: '',
    inviteCode: member.inviteCode,
    kycStatus: kycRaw === undefined || kycRaw === null ? undefined : String(kycRaw),
    withdrawStatus:
      raw.withdrawStatus === undefined || raw.withdrawStatus === null
        ? raw.withdrawForbidden === true || raw.withdrawForbidden === 'true' || raw.withdrawForbidden === 1
          ? '1'
          : '0'
        : String(raw.withdrawStatus),
    levelId: member.levelId,
    levelName: member.levelName,
    usdtAvailable: toBalanceNumber(member.usdtAvailable) ?? 0,
    cnyAvailable: toBalanceNumber(member.cnyAvailable) ?? 0,
    cnyFrozen: toBalanceNumber(member.cnyFrozen) ?? 0,
    teamCount: member.teamCount,
    cnyProductIncome:
      toBalanceNumber(raw.cnyProductIncome) ??
      toBalanceNumber(raw.productIncomeCny) ??
      toBalanceNumber(raw.productIncome) ??
      toBalanceNumber(raw.productEarnings) ??
      toBalanceNumber(raw.income) ??
      0,
    usdtProductIncome:
      toBalanceNumber(raw.usdtProductIncome) ??
      toBalanceNumber(raw.productIncomeUsdt) ??
      toBalanceNumber(raw.usdtIncome) ??
      0,
    cnyAssistValue:
      toBalanceNumber(raw.cnyAssistValue) ??
      toBalanceNumber(raw.assistValueCny) ??
      toBalanceNumber(raw.assistValue) ??
      toBalanceNumber(raw.boostValue) ??
      toBalanceNumber(raw.assistPoints) ??
      0,
    usdtAssistValue:
      toBalanceNumber(raw.usdtAssistValue) ??
      toBalanceNumber(raw.assistValueUsdt) ??
      toBalanceNumber(raw.usdtAssist) ??
      0,
    // 兼容旧单值字段
    productIncome:
      toBalanceNumber(raw.cnyProductIncome) ??
      toBalanceNumber(raw.productIncome) ??
      toBalanceNumber(raw.productEarnings) ??
      toBalanceNumber(raw.income) ??
      0,
    assistValue:
      toBalanceNumber(raw.cnyAssistValue) ??
      toBalanceNumber(raw.assistValue) ??
      toBalanceNumber(raw.boostValue) ??
      toBalanceNumber(raw.assistPoints) ??
      0,
    hasPayPassword: (() => {
      const flag = raw.hasPayPassword ?? raw.payPasswordSet ?? raw.hasTradePassword;
      if (typeof flag === 'boolean') {
        return flag;
      }
      if (flag === 1 || flag === '1' || flag === 'true' || flag === 'Y') {
        return true;
      }
      if (flag === 0 || flag === '0' || flag === 'false' || flag === 'N') {
        return false;
      }
      return undefined;
    })(),
    status: member.status,
  };
}

function extractToken(res: { token?: string; data?: unknown }): string | undefined {
  if (res.token) {
    return res.token;
  }
  if (res.data && typeof res.data === 'object' && res.data !== null && 'token' in res.data) {
    return String((res.data as { token: string }).token);
  }
  return undefined;
}

export async function fetchAppCaptcha(): Promise<AppCaptchaResult> {
  const res = await request('/app/auth/captcha', { withToken: false });
  const enabled = res.captchaEnabled ?? res.captchaOnOff ?? true;
  return {
    enabled,
    uuid: res.uuid ?? '',
    text: typeof res.text === 'string' ? res.text : '',
  };
}

export async function appLogin(body: AppLoginBody): Promise<RuoyiUser> {
  const res = await request<AppMember>('/app/auth/login', {
    method: 'POST',
    body,
    withToken: false,
  });

  const token = extractToken(res);
  if (!token) {
    throw new ApiError(res.msg || '登录失败', res.code);
  }

  await setToken(token);
  markAuthenticated();
  return fetchAppProfile();
}

export async function fetchAppProfile(): Promise<RuoyiUser> {
  const res = await request<AppMember>('/app/profile');
  const member = extractMember(res);
  if (!member) {
    throw new ApiError(res.msg || '获取用户信息失败', res.code);
  }
  const user = mapMember(member);
  setCurrentUser(user, [], []);
  return user;
}

export async function appRegister(body: AppRegisterBody): Promise<RuoyiUser> {
  const res = await request<AppMember>('/app/auth/register', {
    method: 'POST',
    body,
    withToken: false,
  });

  const token = extractToken(res);
  if (!token) {
    throw new ApiError(res.msg || '注册失败', res.code);
  }

  await setToken(token);
  markAuthenticated();
  return fetchAppProfile();
}

export async function appLogout(): Promise<void> {
  try {
    await request('/app/auth/logout', { method: 'POST' });
  } catch {
  } finally {
    await removeToken();
    clearCurrentUser();
    notifyAuthChanged();
  }
}

export const TEXT_PLACEHOLDER = '--';

export function displayText(value?: string | null): string {
  const next = value?.trim();
  return next ? next : TEXT_PLACEHOLDER;
}

export function toNumberOrZero(value?: number | null): number {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return 0;
  }
  return value;
}

export function maskPhone(phone?: string): string {
  if (!phone || phone.length < 7) {
    return phone ?? '';
  }
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

export function maskIdCard(idCard?: string): string {
  const raw = idCard?.trim() ?? '';
  if (raw.length < 8) {
    return raw;
  }
  return `${raw.slice(0, 4)}**********${raw.slice(-4)}`;
}

export function isKycVerified(kycStatus?: string | number | null): boolean {
  if (kycStatus === undefined || kycStatus === null || kycStatus === '') {
    return false;
  }
  const status = String(kycStatus).trim().toLowerCase();
  return ['1', 'approved', 'passed', 'pass', 'success', 'verified', '已认证', '已通过', '已实名', 'true', 'y'].includes(
    status,
  );
}

export function isWithdrawForbidden(withdrawStatus?: string | number | null): boolean {
  return String(withdrawStatus ?? '').trim() === '1';
}

export function kycStatusLabel(kycStatus?: string | number | null): string {
  return isKycVerified(kycStatus) ? '已认证' : '未认证';
}

export function formatBalance(value?: number | null): string {
  return String(toNumberOrZero(value));
}
