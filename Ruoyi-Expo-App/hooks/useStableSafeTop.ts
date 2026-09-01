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
