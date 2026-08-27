import { useFocusEffect, useLocalSearchParams } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { ApiError } from '@/api/request';
import { fetchAppWalletLogs } from '@/api/app-trade';
import type { AppWalletLogItem } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { FundRecordsPanel } from '@/components/ui/FundRecordsPanel';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

type FundTab = 'recharge' | 'product' | 'promo';

const tabs: { key: FundTab; label: string; bizType: string; emptyText: string }[] = [
  { key: 'recharge', label: '充值', bizType: 'RECHARGE', emptyText: '暂无充值记录' },
  { key: 'product', label: '产品收益', bizType: 'REBATE', emptyText: '暂无产品收益记录' },
  {
    key: 'promo',
    label: '推广收益',
    bizType: 'INVITE,COMMISSION,CHECKIN,KYC_REWARD,LEVEL_REWARD',
    emptyText: '暂无推广收益记录',
  },
];

function resolveTab(value: string | string[] | undefined): FundTab {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw === 'product' || raw === 'promo' || raw === 'recharge') {
    return raw;
  }
  // 兼容「我的」页「余额」入口
  if (raw === 'balance') {
    return 'recharge';
  }
  return 'recharge';
}

export default function FundDetailsScreen() {
  const { tab } = useLocalSearchParams<{ tab?: string }>();
  const [activeTab, setActiveTab] = useState<FundTab>(() => resolveTab(tab));
  const [loading, setLoading] = useState(true);
  const [records, setRecords] = useState<AppWalletLogItem[]>([]);

  useFocusEffect(
    useCallback(() => {
      setActiveTab(resolveTab(tab));
    }, [tab]),
  );

  const current = useMemo(
    () => tabs.find((item) => item.key === activeTab) ?? tabs[0],
    [activeTab],
  );

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const next = await fetchAppWalletLogs({
        pageNum: 1,
        pageSize: 50,
        bizType: current.bizType,
      });
      setRecords(next);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取资金明细失败');
      }
    } finally {
      setLoading(false);
    }
  }, [current.bizType]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="资金明细" />
      <View style={styles.tabs}>
        {tabs.map((item) => {
          const active = item.key === activeTab;
          return (
            <Pressable
              key={item.key}
              style={styles.tabItem}
              onPress={() => {
                if (item.key !== activeTab) {
                  setActiveTab(item.key);
                  setLoading(true);
                }
              }}
            >
              <Text style={styles.tabText}>{item.label}</Text>
              <View style={[styles.tabBar, active && styles.tabBarActive]} />
            </Pressable>
          );
        })}
      </View>
      <FundRecordsPanel
        loading={loading}
        records={records}
        showSummary={false}
        emptyText={current.emptyText}
        onRefresh={load}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tabs: {
    flexDirection: 'row',
    paddingHorizontal: 18,
    marginTop: 2,
    marginBottom: 10,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
  },
  tabText: {
    color: colors.text,
    fontSize: 15,
  },
  tabBar: {
    marginTop: 10,
    width: 36,
    height: 3,
    borderRadius: 2,
    backgroundColor: 'transparent',
  },
  tabBarActive: {
    backgroundColor: '#FF2A2A',
  },
});
