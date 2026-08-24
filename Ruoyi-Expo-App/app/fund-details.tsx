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
  fetchAppWalletLogs,
  fetchAppWithdrawRecords,
  formatMoneyLabel,
} from '@/api/app-trade';
import type { AppFundRecord, AppWalletLogItem } from '@/api/types';
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
      <Text style={styles.summaryAmount}>¥ {formatBalance(cny)}</Text>
      <Text style={styles.summaryAmount}>USDT {formatBalance(usdt)}</Text>
    </View>
  );
}

function formatRecordDate(value: string): string {
  const raw = value.replace(/：/g, ':').trim();
  if (!raw) {
    return '';
  }
  return raw.slice(0, 10);
}

function LedgerFooter() {
  return (
    <View style={styles.loadedRow}>
      <View style={styles.loadedLine} />
      <Text style={styles.loadedText}>已加载完毕</Text>
      <View style={styles.loadedLine} />
    </View>
  );
}

export default function FundDetailsScreen() {
  const { tab } = useLocalSearchParams<{ tab?: string }>();
  const [activeTab, setActiveTab] = useState<FundTab>(() => resolveTab(tab));
  const [loading, setLoading] = useState(true);
  const [recharges, setRecharges] = useState<AppFundRecord[]>([]);
  const [withdraws, setWithdraws] = useState<AppFundRecord[]>([]);
  const [balanceLogs, setBalanceLogs] = useState<AppWalletLogItem[]>([]);

  useFocusEffect(
    useCallback(() => {
      setActiveTab(resolveTab(tab));
    }, [tab]),
  );

  const load = useCallback(async () => {
    try {
      const [nextRecharges, nextWithdraws, nextLogs] = await Promise.all([
        fetchAppRechargeRecords(),
        fetchAppWithdrawRecords(),
        fetchAppWalletLogs({ pageNum: 1, pageSize: 50 }),
      ]);
      setRecharges(nextRecharges);
      setWithdraws(nextWithdraws);
      setBalanceLogs(nextLogs);
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

  const summary = useMemo(() => {
    if (activeTab === 'recharge') {
      const totals = sumByCurrency(recharges);
      return {
        label: '累计充值',
        node: <DualSummary cny={totals.cny} usdt={totals.usdt} />,
      };
    }
    if (activeTab === 'withdraw') {
      const totals = sumByCurrency(withdraws);
      return {
        label: '累计提现',
        node: <DualSummary cny={totals.cny} usdt={totals.usdt} />,
      };
    }
    return null;
  }, [activeTab, recharges, withdraws]);

  const fundRows = useMemo(() => {
    const source = activeTab === 'recharge' ? recharges : withdraws;
    return source.map((item) => ({
      id: item.id,
      title: item.title,
      date: formatRecordDate(item.createTime),
      amount: formatMoneyLabel(item.amount, item.currency),
      tone: item.title.includes('成功')
        ? styles.in
        : item.title.includes('失败')
          ? styles.fail
          : styles.pending,
    }));
  }, [activeTab, recharges, withdraws]);

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="资金明细" />
      <View style={styles.tabs}>
        {tabs.map((item) => {
          const active = item.key === activeTab;
          return (
            <Pressable key={item.key} style={styles.tabItem} onPress={() => setActiveTab(item.key)}>
              <Text style={[styles.tabText, active && styles.tabTextActive]}>{item.label}</Text>
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
          {activeTab === 'balance' ? (
            <GlassCard style={styles.ledgerCard}>
              <View style={styles.ledgerHead}>
                <View style={styles.summaryMark} />
                <Text style={styles.summaryLabel}>交易</Text>
              </View>

              {balanceLogs.length === 0 ? (
                <Text style={styles.emptyInCard}>暂无交易记录</Text>
              ) : (
                balanceLogs.map((item, index) => (
                  <View
                    key={item.id}
                    style={[styles.ledgerRow, index > 0 && styles.ledgerRowBorder]}
                  >
                    <View style={styles.ledgerLeft}>
                      <Text style={styles.ledgerTitle} numberOfLines={1}>
                        {item.title}
                      </Text>
                      <Text style={styles.ledgerDate}>{formatRecordDate(item.createTime)}</Text>
                    </View>
                    <Text style={styles.ledgerAmount}>
                      {formatMoneyLabel(item.amount, item.currency)}
                    </Text>
                  </View>
                ))
              )}

              <LedgerFooter />
            </GlassCard>
          ) : (
            <>
              {summary ? (
                <GlassCard style={styles.card}>
                  <View style={styles.summaryRow}>
                    <View style={styles.summaryLeft}>
                      <View style={styles.summaryMark} />
                      <Text style={styles.summaryLabel}>{summary.label}</Text>
                    </View>
                    {summary.node}
                  </View>
                </GlassCard>
              ) : null}

              {fundRows.length === 0 ? (
                <Text style={styles.empty}>暂无记录</Text>
              ) : (
                fundRows.map((item) => (
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
            </>
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
  emptyInCard: {
    color: colors.muted,
    textAlign: 'center',
    paddingVertical: 28,
    fontSize: 14,
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
  ledgerCard: {
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
    borderColor: 'rgba(98, 150, 220, 0.24)',
    borderRadius: 12,
    paddingTop: 16,
    paddingBottom: 12,
    paddingHorizontal: 0,
  },
  ledgerHead: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 4,
  },
  ledgerRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  ledgerRowBorder: {
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: 'rgba(160, 190, 230, 0.28)',
  },
  ledgerLeft: {
    flex: 1,
    paddingRight: 12,
  },
  ledgerTitle: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
  },
  ledgerDate: {
    marginTop: 8,
    color: 'rgba(190, 210, 235, 0.78)',
    fontSize: 13,
  },
  ledgerAmount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
  },
  loadedRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    paddingTop: 8,
    paddingBottom: 6,
    paddingHorizontal: 24,
  },
  loadedLine: {
    width: 36,
    height: StyleSheet.hairlineWidth,
    backgroundColor: 'rgba(160, 190, 230, 0.45)',
  },
  loadedText: {
    color: 'rgba(180, 200, 230, 0.7)',
    fontSize: 12,
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
