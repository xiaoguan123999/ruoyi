import AsyncStorage from '@react-native-async-storage/async-storage';

const OTA_SNOOZE_STORAGE_KEY = 'ota_update_snooze_until';

/** 用户选择「稍后提醒」后，15 分钟内不再自动弹 OTA */
export const OTA_SNOOZE_DURATION_MS = 15 * 60 * 1000;

export async function getOtaSnoozeUntil(): Promise<number | null> {
  try {
    const raw = await AsyncStorage.getItem(OTA_SNOOZE_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const value = Number(raw);
    return Number.isFinite(value) ? value : null;
  } catch {
    return null;
  }
}

export async function isOtaSnoozed(): Promise<boolean> {
  const until = await getOtaSnoozeUntil();
  return until !== null && Date.now() < until;
}

export async function setOtaSnooze(durationMs = OTA_SNOOZE_DURATION_MS): Promise<void> {
  try {
    await AsyncStorage.setItem(OTA_SNOOZE_STORAGE_KEY, String(Date.now() + durationMs));
  } catch {
    // 忽略存储失败
  }
}
