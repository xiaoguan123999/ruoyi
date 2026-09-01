import Constants from "expo-constants";
import * as Updates from "expo-updates";
import { markPendingOtaReload } from "@/utils/ota-reload-recovery";

export type OtaUpdateCheckResult =
  | { status: "disabled" }
  | { status: "upToDate" }
  | { status: "available" }
  | { status: "error"; message: string };

const OTA_NEWER_SKEW_MS = 60_000;

export function isOtaUpdatesEnabled() {
  return !__DEV__ && Updates.isEnabled;
}

function coerceTimeMs(value: unknown): number | null {
  if (value instanceof Date) {
    const time = value.getTime();
    return Number.isFinite(time) ? time : null;
  }

  if (typeof value === "number" && Number.isFinite(value) && value > 0) {
    return value < 1e12 ? value * 1000 : value;
  }

  if (typeof value === "string" && value.trim()) {
    const numeric = Number(value);
    if (Number.isFinite(numeric) && numeric > 0) {
      return numeric < 1e12 ? numeric * 1000 : numeric;
    }

    const parsed = Date.parse(value);
    return Number.isFinite(parsed) ? parsed : null;
  }

  return null;
}

function readRecord(value: unknown): Record<string, unknown> | null {
  if (!value || typeof value !== "object") {
    return null;
  }

  return value as Record<string, unknown>;
}

function getRemoteCreatedAtMs(manifest: unknown): number | null {
  const record = readRecord(manifest);
  if (!record) {
    return null;
  }

  return coerceTimeMs(record.createdAt) ?? coerceTimeMs(record.publishedTime);
}

function getRemoteUpdateId(manifest: unknown): string | null {
  const record = readRecord(manifest);
  const id = record?.id;

  return typeof id === "string" && id.trim() ? id.trim() : null;
}

function getRemoteUpdateGroup(manifest: unknown): string | null {
  const record = readRecord(manifest);
  if (!record) {
    return null;
  }

  const extra = readRecord(record.extra);
  const extraEas = readRecord(extra?.eas);
  const metadata = readRecord(record.metadata);

  const candidates = [record.group, extra?.group, extraEas?.group, extraEas?.updateGroup, metadata?.group];

  for (const candidate of candidates) {
    if (typeof candidate === "string" && candidate.trim()) {
      return candidate.trim();
    }
  }

  return null;
}

function getBundledOtaUpdateIds(): string[] {
  const extra = readRecord(Constants.expoConfig?.extra);
  const ids = extra?.bundledOtaUpdateIds;

  if (!Array.isArray(ids)) {
    return [];
  }

  return ids.filter((id): id is string => typeof id === "string" && id.length > 0);
}

function getBundledOtaUpdateGroup(): string | null {
  const extra = readRecord(Constants.expoConfig?.extra);
  const group = extra?.bundledOtaUpdateGroup;

  return typeof group === "string" && group.length > 0 ? group : null;
}

function isAlreadyBundledInApk(manifest: unknown): boolean {
  const remoteId = getRemoteUpdateId(manifest);
  const remoteGroup = getRemoteUpdateGroup(manifest);
  const bundledGroup = getBundledOtaUpdateGroup();
  const bundledIds = getBundledOtaUpdateIds();

  if (remoteId && bundledIds.includes(remoteId)) {
    return true;
  }

  return Boolean(remoteGroup && bundledGroup && remoteGroup === bundledGroup);
}

function getLocalCreatedAtMs(): number | null {
  const fromUpdatesCreatedAt = coerceTimeMs(Updates.createdAt);
  if (fromUpdatesCreatedAt !== null) {
    return fromUpdatesCreatedAt;
  }

  const manifest = readRecord(Updates.manifest);
  const fromCommitTime = coerceTimeMs(manifest?.commitTime);
  if (fromCommitTime !== null) {
    return fromCommitTime;
  }

  const extra = readRecord(Constants.expoConfig?.extra);
  const fromConstants = coerceTimeMs(extra?.nativeBuildTime);
  if (fromConstants !== null) {
    return fromConstants;
  }

  const manifestExtra = readRecord(manifest?.extra);
  const expoClient = readRecord(manifestExtra?.expoClient);
  const expoClientExtra = readRecord(expoClient?.extra);
  return coerceTimeMs(expoClientExtra?.nativeBuildTime);
}

/**
 * 不用 isEmbeddedLaunch：JS 侧是 `native || false`，拿不到时会变成 false，
 * 新装被当成已热更新；反过来又会把老用户挡住。
 *
 * 优先：APK 里写入的 bundledOtaUpdateIds / group 等于通道当前包 → 不弹（不靠时钟）。
 * 否则用打包时间 vs 通道发布时间：旧包更早 → 弹；新 APK 晚于刚发的 eas update → 不弹。
 */
function shouldApplyOta(checkResult: Awaited<ReturnType<typeof Updates.checkForUpdateAsync>>) {
  if (!checkResult.isAvailable) {
    return false;
  }

  if ("isRollBackToEmbedded" in checkResult && checkResult.isRollBackToEmbedded) {
    return false;
  }

  if (isAlreadyBundledInApk(checkResult.manifest)) {
    return false;
  }

  const remoteCreatedAt = getRemoteCreatedAtMs(checkResult.manifest);
  const localCreatedAt = getLocalCreatedAtMs();

  if (localCreatedAt !== null && remoteCreatedAt !== null) {
    return remoteCreatedAt > localCreatedAt + OTA_NEWER_SKEW_MS;
  }

  // 新包装了本地时间但通道没给时间：不弹。旧包两边都没有：弹，保证老用户能更新。
  if (localCreatedAt !== null) {
    return false;
  }

  return true;
}

/** 启动时静默拉取 OTA；有更新则 reload，调用方通常不会再往下执行 */
export async function syncOtaUpdateSilently(): Promise<boolean> {
  if (!isOtaUpdatesEnabled()) {
    return false;
  }

  try {
    const result = await Updates.checkForUpdateAsync();

    if (!shouldApplyOta(result)) {
      return false;
    }

    await Updates.fetchUpdateAsync();
    await markPendingOtaReload();
    await Updates.reloadAsync();
    return true;
  } catch (error) {
    console.warn("silent OTA sync failed", error);
    return false;
  }
}

export async function checkOtaUpdate(): Promise<OtaUpdateCheckResult> {
  if (!isOtaUpdatesEnabled()) {
    return { status: "disabled" };
  }

  try {
    const result = await Updates.checkForUpdateAsync();

    if (shouldApplyOta(result)) {
      return { status: "available" };
    }

    return { status: "upToDate" };
  } catch (error) {
    const message = error instanceof Error ? error.message : "OTA check failed";
    return { status: "error", message };
  }
}

/** 拉取 OTA 包并 reload；成功时不会 return（进程会重启） */
export async function fetchAndReloadOtaUpdate(): Promise<boolean> {
  if (!isOtaUpdatesEnabled()) {
    return false;
  }

  try {
    const checkResult = await Updates.checkForUpdateAsync();

    if (!shouldApplyOta(checkResult)) {
      return false;
    }

    await Updates.fetchUpdateAsync();
    await markPendingOtaReload();
    await Updates.reloadAsync();
    return true;
  } catch (error) {
    console.warn("fetch and reload OTA update failed", error);
    return false;
  }
}
