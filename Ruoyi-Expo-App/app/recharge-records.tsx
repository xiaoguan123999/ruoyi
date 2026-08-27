import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';

import { ApiError } from '@/api/request';
import { fetchAppFundRecords } from '@/api/app-trade';
import type { AppFundRecord } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { FundRecordsPanel } from '@/components/ui/FundRecordsPanel';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { modalError } from '@/utils/toast';

/** GET /app/fundRecords?bizType=RECHARGE */
export default function RechargeRecordsScreen() {
  const [loading, setLoading] = useState(true);
  const [records, setRecords] = useState<AppFundRecord[]>([]);

  const load = useCallback(async () => {
    try {
      const next = await fetchAppFundRecords({
        pageNum: 1,
        pageSize: 50,
        bizType: 'RECHARGE',
      });
      setRecords(next);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取充值记录失败');
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
      <PageHeader title="充值记录" />
      <FundRecordsPanel
        loading={loading}
        records={records}
        summaryLabel="累计充值"
        summaryMode="credit"
        emptyText="暂无充值记录"
        onRefresh={load}
      />
    </AppBackground>
  );
}
