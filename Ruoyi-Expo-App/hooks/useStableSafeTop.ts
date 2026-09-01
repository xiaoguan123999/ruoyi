import { Platform, StatusBar } from 'react-native';
import { initialWindowMetrics, useSafeAreaInsets } from 'react-native-safe-area-context';

/** 避免 Android 首帧 insets=0 再撑开，导致进子页标题栏往下跳 */
export function useStableSafeTop() {
  const insets = useSafeAreaInsets();
  const fallback =
    initialWindowMetrics?.insets.top ||
    (Platform.OS === 'android' ? StatusBar.currentHeight ?? 0 : 0) ||
    0;
  return Math.max(insets.top, fallback);
}

/** 真机底部 Home / 手势条；首帧同样可能为 0 */
export function useStableSafeBottom() {
  const insets = useSafeAreaInsets();
  const fallback = initialWindowMetrics?.insets.bottom || 0;
  return Math.max(insets.bottom, fallback);
}
