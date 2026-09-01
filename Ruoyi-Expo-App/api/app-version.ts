import { Linking, Platform } from 'react-native';
import Constants from 'expo-constants';

import { request } from '@/api/request';
import type { AppVersion } from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickString(source: Record<string, unknown>, key: string, fallback = ''): string {
  const value = source[key];
  if (value !== undefined && value !== null && String(value).length > 0) {
    return String(value);
  }
  return fallback;
}

function toBool(value: unknown): boolean {
  return value === true || value === 1;
}

function mapVersion(raw: unknown): AppVersion | null {
  if (!isRecord(raw)) {
    return null;
  }
  const version = pickString(raw, 'version');
  if (!version) {
    return null;
  }
  return {
    id: toNumber(raw.id),
    platform: pickString(raw, 'platform'),
    version,
    downloadUrl: pickString(raw, 'downloadUrl'),
    description: pickString(raw, 'description'),
    forceUpdate: toBool(raw.forceUpdate),
    isLatest: toBool(raw.isLatest),
    isEnabled: toBool(raw.isEnabled),
  };
}

export function getAppRuntimeVersion(): string {
  return Constants.expoConfig?.version?.trim() || '1.0.0';
}

export function getAppStorePlatform(): 'android' | 'ios' | null {
  if (Platform.OS === 'android') {
    return 'android';
  }
  if (Platform.OS === 'ios') {
    return 'ios';
  }
  return null;
}

/** 1.0.11 > 1.0.0 → 正数 */
export function compareAppVersion(left: string, right: string): number {
  const a = (left.match(/\d+/g) ?? []).map((part) => Number.parseInt(part, 10));
  const b = (right.match(/\d+/g) ?? []).map((part) => Number.parseInt(part, 10));
  const len = Math.max(a.length, b.length);
  for (let i = 0; i < len; i += 1) {
    const diff = (a[i] ?? 0) - (b[i] ?? 0);
    if (diff !== 0) {
      return diff;
    }
  }
  return 0;
}

export function isNewerAppVersion(remote: string, local: string): boolean {
  return compareAppVersion(remote, local) > 0;
}

export type AppVersionCheckResult = {
  hasUpdate: boolean;
  version: AppVersion | null;
  currentVersion: string;
  isSupportedPlatform: boolean;
};

export async function checkNativeAppUpdate(): Promise<AppVersionCheckResult> {
  const currentVersion = getAppRuntimeVersion();
  const platform = getAppStorePlatform();
  if (!platform) {
    return {
      hasUpdate: false,
      version: null,
      currentVersion,
      isSupportedPlatform: false,
    };
  }
  try {
    const latest = await fetchAppVersionLatest(platform, currentVersion);
    const hasUpdate = Boolean(
      latest && isNewerAppVersion(latest.version, currentVersion),
    );
    return {
      hasUpdate,
      version: latest,
      currentVersion,
      isSupportedPlatform: true,
    };
  } catch {
    return {
      hasUpdate: false,
      version: null,
      currentVersion,
      isSupportedPlatform: true,
    };
  }
}

export async function openAppUpdateUrl(url: string): Promise<boolean> {
  if (!url) {
    return false;
  }
  try {
    await Linking.openURL(url);
    return true;
  } catch {
    return false;
  }
}

/**
 * POST /app/version/latest { platform, version } — 免登录
 * version 可空；有当前版本时带上，便于后台对比。
 */
export async function fetchAppVersionLatest(
  platform: string,
  version = getAppRuntimeVersion(),
): Promise<AppVersion | null> {
  const res = await request<unknown>('/app/version/latest', {
    method: 'POST',
    withToken: false,
    body: { platform, version },
  });
  const root = isRecord(res) ? res.data : null;
  const payload = isRecord(root) ? root.version : null;
  const mapped = mapVersion(payload);
  if (!mapped || mapped.isEnabled !== true) {
    return null;
  }
  return mapped;
}
