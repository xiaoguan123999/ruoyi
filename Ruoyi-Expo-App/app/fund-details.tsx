import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
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
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

type FundTab = 'recharge' | 'withdraw' | 'balance';

const tabs: { key: FundTab; label: string }[] = [
  { key: 'recharge', label: '充值' },
  { key: 'withdraw', label: '提现' },
  { key: 'balance', label: '充值余额' },
];

export default function FundDetailsScreen() {
  const [activeTab, setActiveTab] = useState<FundTab>('withdraw');
  const [loading, setLoading] = useState(true);
  const [wallet, setWallet] = useState<AppWallet | null>(null);
  const [recharges, setRecharges] = useState<AppFundRecord[]>([]);
  const [withdraws, setWithdraws] = useState<AppFundRecord[]>([]);

  const load = useCallback(async () => {
    setLoading(true);
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
      const total = recharges.reduce((sum, item) => sum + item.amount, 0);
      return {
        summaryLabel: '累计充值',
        summaryAmount: `¥${formatBalance(total)}`,
        rows: recharges.map((item) => ({
          id: item.id,
          title: item.title,
          date: item.createTime.slice(0, 10),
          amount: formatMoneyLabel(item.amount, item.currency),
          tone: styles.in,
        })),
      };
    }
    if (activeTab === 'balance') {
      return {
        summaryLabel: '充值余额',
        summaryAmount: `¥${formatBalance(wallet?.cnyAvailable)}`,
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
    const total = withdraws.reduce((sum, item) => sum + item.amount, 0);
    return {
      summaryLabel: '累计提现',
      summaryAmount: `¥${formatBalance(total)}`,
      rows: withdraws.map((item) => ({
        id: item.id,
        title: item.title,
        date: item.createTime.slice(0, 10),
        amount: formatMoneyLabel(item.amount, item.currency),
        tone: styles.in,
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
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          <GlassCard style={styles.card}>
            <View style={styles.summaryRow}>
              <View style={styles.summaryLeft}>
                <View style={styles.summaryMark} />
                <Text style={styles.summaryLabel}>{content.summaryLabel}</Text>
              </View>
              <Text style={styles.summaryAmount}>{content.summaryAmount}</Text>
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
        </ScrollView>
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
    alignItems: 'center',
  },
  summaryLeft: {
    flexDirection: 'row',
    alignItems: 'center',
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
  },
  summaryAmount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '500',
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
});
