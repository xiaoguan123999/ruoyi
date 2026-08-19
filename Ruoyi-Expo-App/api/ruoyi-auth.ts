import {
  clearCurrentUser,
  markAuthenticated,
  notifyAuthChanged,
  setCurrentUser,
} from '@/api/auth-state';
import { ApiError, request } from '@/api/request';
import type { LoginBody, RuoyiUser } from '@/api/types';
import { removeToken, setToken } from '@/utils/storage';

export type CaptchaResult = {
  enabled: boolean;
  uuid: string;
  img: string;
};

export async function fetchCaptcha(): Promise<CaptchaResult> {
  const res = await request('/captchaImage', { withToken: false });
  const enabled = res.captchaEnabled ?? res.captchaOnOff ?? true;
  const img = res.img ? `data:image/gif;base64,${res.img}` : '';
  return {
    enabled,
    uuid: res.uuid ?? '',
    img,
  };
}

export async function fetchUserInfo(): Promise<RuoyiUser> {
  const res = await request<RuoyiUser>('/getInfo');
  if (!res.user) {
    throw new ApiError(res.msg || '获取用户信息失败', res.code);
  }
  setCurrentUser(res.user, res.roles ?? [], res.permissions ?? []);
  return res.user;
}

export async function login(body: LoginBody): Promise<RuoyiUser | null> {
  const res = await request('/login', { method: 'POST', body, withToken: false });
  if (!res.token) {
    throw new ApiError(res.msg || '登录失败', res.code);
  }
  await setToken(res.token);
  markAuthenticated();
  try {
    return await fetchUserInfo();
  } catch (error) {
    if (error instanceof ApiError && error.code === 401) {
      throw error;
    }
    notifyAuthChanged();
    return null;
  }
}

export async function logout(): Promise<void> {
  try {
    await request('/logout', { method: 'POST' });
  } catch {
    // Always clear the local session.
  } finally {
    await removeToken();
    clearCurrentUser();
  }
}

export async function registerAccount(body: LoginBody) {
  return request('/register', { method: 'POST', body, withToken: false });
}
