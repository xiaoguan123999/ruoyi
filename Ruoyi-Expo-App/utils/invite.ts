import { Platform } from 'react-native';

import { config } from '@/config';

/**
 * 邀请二维码指向 H5 注册页（非后端 API）：
 *   {EXPO_PUBLIC_H5_URL}/sign-up?inviteCode=xxx
 * 手机浏览器扫码打开后，注册页会回填邀请码。
 */
export function buildInviteRegisterUrl(inviteCode: string): string {
  const code = inviteCode.trim();
  if (!code || code === '--') {
    return '';
  }

  const base = resolveH5Base();
  if (!base) {
    return '';
  }
  return `${base}/sign-up?inviteCode=${encodeURIComponent(code)}`;
}

function resolveH5Base(): string {
  // 仅使用 H5 站点 origin，禁止用 API_URL
  if (config.H5_URL) {
    return config.H5_URL.replace(/\/+$/, '');
  }
  // Web 端未配置时，用当前页面 origin（同域部署的 H5）
  if (Platform.OS === 'web' && typeof window !== 'undefined' && window.location?.origin) {
    return window.location.origin.replace(/\/+$/, '');
  }
  return '';
}

/** 从路由/查询参数中解析邀请码 */
export function pickInviteCodeFromParams(params: Record<string, unknown>): string {
  const keys = ['inviteCode', 'invite', 'code'] as const;
  for (const key of keys) {
    const raw = params[key];
    const value = Array.isArray(raw) ? raw[0] : raw;
    if (typeof value === 'string' && value.trim()) {
      return value.trim();
    }
  }
  return '';
}
