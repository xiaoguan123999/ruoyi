import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';

import { ApiError } from '@/api/request';
import { fetchAppWalletLogs } from '@/api/app-trade';
import type { AppWalletLogItem } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { FundRecordsPanel } from '@/components/ui/FundRecordsPanel';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { modalError } from '@/utils/toast';

/** GET /app/walletLog?typeCode=ASSIST — 助力值明细 */
export default function AssistRecordsScreen() {
  const [loading, setLoading] = useState(true);
  const [records, setRecords] = useState<AppWalletLogItem[]>([]);

  const load = useCallback(async () => {
    try {
      const next = await fetchAppWalletLogs({
        pageNum: 1,
        pageSize: 50,
        typeCode: 'ASSIST',
      });
      setRecords(next);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取助力值明细失败');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="助力值明细" />
      <FundRecordsPanel
        loading={loading}
        records={records}
        showSummary={false}
        amountMode="points"
        emptyText="暂无助力值明细"
        onRefresh={load}
      />
    </AppBackground>
  );
}
