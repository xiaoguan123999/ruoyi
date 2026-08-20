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

import { ApiError } from '@/api/request';
import { fetchAppOrders, formatMoneyLabel } from '@/api/app-trade';
import type { AppOrderRecord } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

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
    setLoading(true);
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
        <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
          {list.length === 0 ? (
            <Text style={styles.empty}>暂无认购记录</Text>
          ) : (
            list.map((item) => <RecordCard key={item.orderId} item={item} />)
          )}
        </ScrollView>
      )}
    </AppBackground>
  );
}

function RecordCard({ item }: { item: AppOrderRecord }) {
  const running = item.statusLabel === '进行中';

  return (
    <GlassCard style={styles.card}>
      <View style={styles.row}>
        <Text style={styles.plan}>{item.planName || '认购订单'}</Text>
        <Text style={styles.activate}>{item.activateLabel}</Text>
      </View>

      <View style={[styles.row, styles.midRow]}>
        <Text style={styles.product}>{item.productName}</Text>
        <Text style={styles.amount}>{formatMoneyLabel(item.amount, item.currency)}</Text>
      </View>

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
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  plan: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
  },
  activate: {
    color: 'rgba(255, 255, 255, 0.85)',
    fontSize: 13,
  },
  midRow: {
    marginTop: 14,
  },
  product: {
    color: colors.text,
    fontSize: 15,
  },
  amount: {
    color: colors.text,
    fontSize: 15,
  },
  bottomRow: {
    marginTop: 16,
  },
  tag: {
    minWidth: 62,
    height: 26,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 10,
  },
  tagRunning: {
    backgroundColor: '#2FBF4A',
  },
  tagExpired: {
    backgroundColor: '#5A2E24',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.55)',
  },
  tagText: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
  },
  time: {
    color: colors.text,
    fontSize: 13,
  },
});
