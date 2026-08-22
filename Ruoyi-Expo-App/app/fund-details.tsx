import { useFocusEffect, useLocalSearchParams } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { formatBalance } from '@/api/app-auth';
import { ApiError } from '@/api/request';
import {
  fetchAppRechargeRecords,
  fetchAppWallet,
  fetchAppWithdrawRecords,
  formatMoneyLabel,
} from '@/api/app-trade';
import type { AppFundRecord, AppWallet } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

type FundTab = 'recharge' | 'withdraw' | 'balance';

const tabs: { key: FundTab; label: string }[] = [
  { key: 'recharge', label: '充值' },
  { key: 'withdraw', label: '提现' },
  { key: 'balance', label: '充值余额' },
];

function resolveTab(value: string | string[] | undefined): FundTab {
  const raw = Array.isArray(value) ? value[0] : value;
  if (raw === 'withdraw' || raw === 'balance' || raw === 'recharge') {
    return raw;
  }
  return 'recharge';
}

function isSuccessRecord(item: AppFundRecord) {
  return item.title.includes('成功');
}

function sumByCurrency(records: AppFundRecord[]) {
  return records.reduce(
    (acc, item) => {
      if (!isSuccessRecord(item)) {
        return acc;
      }
      const currency = (item.currency || 'CNY').toUpperCase();
      if (currency === 'USDT' || currency === 'USD' || currency === 'U') {
        acc.usdt += item.amount;
      } else {
        acc.cny += item.amount;
      }
      return acc;
    },
    { cny: 0, usdt: 0 },
  );
}

function DualSummary({ cny, usdt }: { cny: number; usdt: number }) {
  return (
    <View style={styles.summaryAmounts}>
      <Text style={styles.summaryAmount}>¥{formatBalance(cny)}</Text>
      <Text style={styles.summaryAmount}>USDT {formatBalance(usdt)}</Text>
    </View>
  );
}

export default function FundDetailsScreen() {
  const { tab } = useLocalSearchParams<{ tab?: string }>();
  const [activeTab, setActiveTab] = useState<FundTab>(() => resolveTab(tab));
  const [loading, setLoading] = useState(true);
  const [wallet, setWallet] = useState<AppWallet | null>(null);
  const [recharges, setRecharges] = useState<AppFundRecord[]>([]);
  const [withdraws, setWithdraws] = useState<AppFundRecord[]>([]);

  useFocusEffect(
    useCallback(() => {
      setActiveTab(resolveTab(tab));
    }, [tab]),
  );
  const load = useCallback(async () => {
    try {
      const [nextWallet, nextRecharges, nextWithdraws] = await Promise.all([
        fetchAppWallet(),
        fetchAppRechargeRecords(),
        fetchAppWithdrawRecords(),
      ]);
      setWallet(nextWallet);
      setRecharges(nextRecharges);
      setWithdraws(nextWithdraws);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取资金明细失败');
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

  const content = useMemo(() => {
    if (activeTab === 'recharge') {
      const totals = sumByCurrency(recharges);
      return {
        summaryLabel: '累计充值',
        summaryNode: <DualSummary cny={totals.cny} usdt={totals.usdt} />,
        rows: recharges.map((item) => ({
          id: item.id,
          title: item.title,
          date: item.createTime.slice(0, 10),
          amount: formatMoneyLabel(item.amount, item.currency),
          tone: item.title.includes('成功')
            ? styles.in
            : item.title.includes('失败')
              ? styles.fail
              : styles.pending,
        })),
      };
    }
    if (activeTab === 'balance') {
      return {
        summaryLabel: '充值余额',
        summaryNode: (
          <DualSummary
            cny={wallet?.cnyAvailable ?? 0}
            usdt={wallet?.usdtAvailable ?? 0}
          />
        ),
        rows: [
          {
            id: 'cny',
            title: '人民币可用',
            date: '当前余额',
            amount: `¥${formatBalance(wallet?.cnyAvailable)}`,
            tone: styles.textLight,
          },
          {
            id: 'usdt',
            title: 'USDT 可用',
            date: '当前余额',
            amount: `USDT ${formatBalance(wallet?.usdtAvailable)}`,
            tone: styles.textLight,
          },
        ],
      };
    }
    const totals = sumByCurrency(withdraws);
    return {
      summaryLabel: '累计提现',
      summaryNode: <DualSummary cny={totals.cny} usdt={totals.usdt} />,
      rows: withdraws.map((item) => ({
        id: item.id,
        title: item.title,
        date: item.createTime.slice(0, 10),
        amount: formatMoneyLabel(item.amount, item.currency),
        tone: item.title.includes('成功')
          ? styles.in
          : item.title.includes('失败')
            ? styles.fail
            : styles.pending,
      })),
    };
  }, [activeTab, recharges, wallet, withdraws]);

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="资金明细" />
      <View style={styles.tabs}>
        {tabs.map((tab) => {
          const active = tab.key === activeTab;
          return (
            <Pressable key={tab.key} style={styles.tabItem} onPress={() => setActiveTab(tab.key)}>
              <Text style={[styles.tabText, active && styles.tabTextActive]}>{tab.label}</Text>
              <View style={[styles.tabBar, active && styles.tabBarActive]} />
            </Pressable>
          );
        })}
      </View>
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          onRefresh={load}
        >
          <GlassCard style={styles.card}>
            <View style={styles.summaryRow}>
              <View style={styles.summaryLeft}>
                <View style={styles.summaryMark} />
                <Text style={styles.summaryLabel}>{content.summaryLabel}</Text>
              </View>
              {content.summaryNode}
            </View>
          </GlassCard>

          {content.rows.length === 0 ? (
            <Text style={styles.empty}>暂无记录</Text>
          ) : (
            content.rows.map((item) => (
              <GlassCard key={item.id} style={styles.card}>
                <View style={styles.detailRow}>
                  <View>
                    <Text style={[styles.title, item.tone]}>{item.title}</Text>
                    <Text style={styles.time}>{item.date}</Text>
                  </View>
                  <Text style={styles.summaryAmount}>{item.amount}</Text>
                </View>
              </GlassCard>
            ))
          )}
        </RefreshableScrollView>
      )}
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
  tabTextActive: {
    color: colors.text,
  },
  tabBar: {
    marginTop: 10,
    width: 32,
    height: 4,
    borderRadius: 2,
    backgroundColor: 'transparent',
  },
  tabBarActive: {
    backgroundColor: '#FF2A2A',
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 24,
    gap: 12,
  },
  card: {
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
    borderColor: 'rgba(98, 150, 220, 0.24)',
    borderRadius: 12,
    paddingVertical: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  summaryLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 1,
  },
  summaryMark: {
    width: 3,
    height: 18,
    borderRadius: 2,
    backgroundColor: '#FF2A2A',
    marginRight: 14,
  },
  summaryLabel: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
    lineHeight: 22,
  },
  summaryAmounts: {
    alignItems: 'flex-end',
    gap: 4,
  },
  summaryAmount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
    lineHeight: 22,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  title: {
    fontSize: 16,
    fontWeight: '600',
  },
  time: {
    color: colors.text,
    marginTop: 10,
    fontSize: 14,
  },
  textLight: {
    color: colors.text,
  },
  in: {
    color: '#7CFF3B',
  },
  pending: {
    color: '#FF9F43',
  },
  fail: {
    color: colors.danger,
  },
});
