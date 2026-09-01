import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const OTA_RELOAD_FLAG_KEY = '__ota_reload_pending__';

/** reloadAsync 前写入标记，热重启后可用来规避过期会话提示 */
export async function markPendingOtaReload() {
  if (Platform.OS === 'web') {
    return;
  }
  await AsyncStorage.setItem(OTA_RELOAD_FLAG_KEY, String(Date.now()));
}

export async function consumePendingOtaReload(): Promise<boolean> {
  if (Platform.OS === 'web') {
    return false;
  }
  const flag = await AsyncStorage.getItem(OTA_RELOAD_FLAG_KEY);
  if (!flag) {
    return false;
  }
  await AsyncStorage.removeItem(OTA_RELOAD_FLAG_KEY);
  return true;
}
