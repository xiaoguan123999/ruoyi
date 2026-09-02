import { Platform } from 'react-native';

/**
 * App：本次前台会话，回桌面再进会从头弹。
 * H5：看完后记到浏览器，切走/刷新不再弹，只有退出登录后才再弹。
 */
const WEB_DONE_KEY = 'xfzl_announcement_popup_done';

let queueFinishedThisForeground = false;

function readWebDone(): boolean {
  if (Platform.OS !== 'web' || typeof localStorage === 'undefined') {
    return false;
  }
  try {
    return localStorage.getItem(WEB_DONE_KEY) === '1';
  } catch {
    return false;
  }
}

function writeWebDone(done: boolean): void {
  if (Platform.OS !== 'web' || typeof localStorage === 'undefined') {
    return;
  }
  try {
    if (done) {
      localStorage.setItem(WEB_DONE_KEY, '1');
    } else {
      localStorage.removeItem(WEB_DONE_KEY);
    }
  } catch {
    // 隐私模式可能写不了
  }
}

if (Platform.OS === 'web') {
  queueFinishedThisForeground = readWebDone();
}

export function isAnnouncementPopupFinished(): boolean {
  return queueFinishedThisForeground || readWebDone();
}

export function finishAnnouncementPopupQueue(): void {
  queueFinishedThisForeground = true;
  writeWebDone(true);
}

export function resetAnnouncementPopupSession(): void {
  queueFinishedThisForeground = false;
  writeWebDone(false);
}
