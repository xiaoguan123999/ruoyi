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
  img: string;
};

function mapMember(member: AppMember): RuoyiUser {
  return {
    userId: member.memberId,
    userName: member.phone,
    nickName: member.realName || member.phone,
    phone: member.phone,
    avatar: '',
    inviteCode: member.inviteCode,
    kycStatus: member.kycStatus,
    levelId: member.levelId,
    levelName: member.levelName,
    usdtAvailable: member.usdtAvailable,
    cnyAvailable: member.cnyAvailable,
    cnyFrozen: member.cnyFrozen,
    teamCount: member.teamCount,
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

function extractMember(res: { data?: AppMember; user?: AppMember }): AppMember | undefined {
  if (res.data && typeof res.data === 'object' && 'memberId' in res.data) {
    return res.data;
  }
  if (res.user && typeof res.user === 'object' && 'memberId' in res.user) {
    return res.user;
  }
  return undefined;
}

export async function fetchAppCaptcha(): Promise<AppCaptchaResult> {
  const res = await request('/app/auth/captcha', { withToken: false });
  const enabled = res.captchaEnabled ?? res.captchaOnOff ?? true;
  const raw = res.img ?? '';
  const img = raw.startsWith('data:') ? raw : raw ? `data:image/jpeg;base64,${raw}` : '';
  return {
    enabled,
    uuid: res.uuid ?? '',
    img,
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

export function maskPhone(phone?: string): string {
  if (!phone || phone.length < 7) {
    return phone ?? '';
  }
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`;
}

export function isKycVerified(kycStatus?: string): boolean {
  if (!kycStatus) {
    return false;
  }
  return ['1', 'approved', '已认证', '已通过'].includes(kycStatus);
}

export function formatBalance(value?: number): string {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return '0';
  }
  return String(value);
}
