import { useCallback, useState } from 'react';

/**
 * 下拉刷新状态封装：把页面 load 函数包一层即可。
 */
export function usePullRefresh(reload: () => void | Promise<void>) {
  const [refreshing, setRefreshing] = useState(false);

  const onRefresh = useCallback(async () => {
    if (refreshing) {
      return;
    }
    setRefreshing(true);
    try {
      await reload();
    } finally {
      setRefreshing(false);
    }
  }, [refreshing, reload]);

  return { refreshing, onRefresh };
}
