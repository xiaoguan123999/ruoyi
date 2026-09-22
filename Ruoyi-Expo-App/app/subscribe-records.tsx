import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  View,
} from 'react-native';

import { Text } from '@/components/ui/AppText';
import { ApiError } from '@/api/request';
import { fetchAppOrders, formatMoneyLabel, settleOrderAccumulate } from '@/api/app-trade';
import type { AppOrderRecord } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess } from '@/utils/toast';

type RecordTab = 'all' | 'running' | 'expired';

const tabs: { key: RecordTab; label: string }[] = [
  { key: 'all', label: '全部' },
  { key: 'running', label: '进行中' },
  { key: 'expired', label: '已到期' },
];

export default function SubscribeRecordsScreen() {
  const [activeTab, setActiveTab] = useState<RecordTab>('all');
  const [loading, setLoading] = useState(true);
  const [orders, setOrders] = useState<AppOrderRecord[]>([]);

  const load = useCallback(async () => {
    try {
      const list = await fetchAppOrders();
      setOrders(list);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取认购记录失败');
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

  const list = useMemo(() => {
    if (activeTab === 'running') {
      return orders.filter((item) => item.statusLabel === '进行中');
    }
    if (activeTab === 'expired') {
      return orders.filter((item) => item.statusLabel === '已到期');
    }
    return orders;
  }, [activeTab, orders]);

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="认购记录" />
      <View style={styles.tabs}>
        {tabs.map((tab) => {
          const active = tab.key === activeTab;
          return (
            <Pressable key={tab.key} style={styles.tabItem} onPress={() => setActiveTab(tab.key)}>
              <Text style={styles.tabText}>{tab.label}</Text>
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
          {list.length === 0 ? (
            <Text style={styles.empty}>暂无认购记录</Text>
          ) : (
            list.map((item) => <RecordCard key={item.orderId} item={item} onSettled={load} />)
          )}
        </RefreshableScrollView>
      )}
    </AppBackground>
  );
}

function RecordCard({ item, onSettled }: { item: AppOrderRecord; onSettled: () => void }) {
  const [settling, setSettling] = useState(false);
  const running = item.statusLabel === '进行中';
  const accumulate = item.incomeMode === 'ACCUMULATE';
  const activateTone =
    item.activatedQty <= 0
      ? styles.activateIdle
      : item.activatedQty >= item.quantity
        ? styles.activateReady
        : styles.activateOn;

  const onSettle = async () => {
    if (settling) return;
    setSettling(true);
    try {
      await settleOrderAccumulate(item.orderId);
      modalSuccess('已结算到产品收益钱包');
      onSettled();
    } catch (error) {
      modalError(error instanceof ApiError ? error.message : '结算失败');
    } finally {
      setSettling(false);
    }
  };

  return (
    <GlassCard style={styles.card}>
      <View style={styles.row}>
        <Text style={styles.plan}>{item.planName || '认购订单'}</Text>
        <Text style={[styles.activate, activateTone]}>
          {item.activatedQty > 0 ? '已激活' : '未激活'} {item.activatedQty}/{item.quantity}
        </Text>
      </View>

      <View style={[styles.row, styles.midRow]}>
        <Text style={styles.product}>{item.productName}</Text>
        <Text style={styles.amount}>{formatMoneyLabel(item.amount, item.currency)}</Text>
      </View>

      {accumulate ? (
        <View style={styles.accumulateBox}>
          <View style={styles.row}>
            <Text style={styles.accLabel}>累计金额</Text>
            <Text style={styles.accValue}>
              {formatMoneyLabel(item.accumulatedAmount || 0, item.currency)}
            </Text>
          </View>
          <View style={[styles.row, styles.accMeta]}>
            <Text style={styles.accHint}>
              周期 {item.accumulateDays || 0}/{item.accumulateCycleDays || 0} 天
              {item.accumulatePaused ? ' · 已暂停' : ''}
            </Text>
            {item.relatedProductName ? (
              <Text style={styles.accHint}>
                {item.relatedProductOwned ? '已持有' : '需认购'}
                {item.relatedProductName}
              </Text>
            ) : null}
          </View>
          {item.canSettleAccumulate ? (
            <PrimaryButton
              title={settling ? '结算中…' : '结算到产品收益'}
              onPress={onSettle}
              compact
              disabled={settling}
            />
          ) : null}
        </View>
      ) : null}

      <View style={[styles.row, styles.bottomRow]}>
        <View style={[styles.tag, running ? styles.tagRunning : styles.tagExpired]}>
          <Text style={styles.tagText}>{item.statusLabel}</Text>
        </View>
        <Text style={styles.time}>购买时间：{item.createTime}</Text>
      </View>
    </GlassCard>
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
    marginTop: 40,
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
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
  },
  midRow: {
    marginTop: 12,
  },
  bottomRow: {
    marginTop: 14,
  },
  plan: {
    color: colors.muted,
    fontSize: 13,
    flex: 1,
    marginRight: 8,
  },
  activate: {
    fontSize: 12,
    fontWeight: '600',
  },
  activateIdle: { color: 'rgba(180,200,230,0.55)' },
  activateOn: { color: '#7EC8FF' },
  activateReady: { color: '#6BE3A0' },
  product: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    flex: 1,
    marginRight: 8,
  },
  amount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
  },
  accumulateBox: {
    marginTop: 12,
    marginHorizontal: 16,
    padding: 12,
    borderRadius: 10,
    backgroundColor: 'rgba(12, 28, 58, 0.85)',
    gap: 8,
  },
  accLabel: {
    color: 'rgba(180,200,230,0.75)',
    fontSize: 12,
  },
  accValue: {
    color: '#FFD56A',
    fontSize: 16,
    fontWeight: '700',
  },
  accMeta: {
    paddingHorizontal: 0,
  },
  accHint: {
    color: 'rgba(180,200,230,0.65)',
    fontSize: 11,
    flexShrink: 1,
  },
  tag: {
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  tagRunning: {
    backgroundColor: 'rgba(64, 158, 255, 0.2)',
  },
  tagExpired: {
    backgroundColor: 'rgba(140, 160, 190, 0.18)',
  },
  tagText: {
    color: colors.text,
    fontSize: 12,
  },
  time: {
    color: colors.muted,
    fontSize: 12,
  },
});
