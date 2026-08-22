import { useLocalSearchParams, useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';

type WalletTab = 'usdt' | 'bank' | 'alipay';

const TABS: { key: WalletTab; label: string }[] = [
  { key: 'usdt', label: 'USDT钱包' },
  { key: 'bank', label: '银行卡' },
  { key: 'alipay', label: '支付宝' },
];

const TAB_META: Record<
  WalletTab,
  {
    addLabel: string;
    hint: string;
    addRoute: '/wallet/add-usdt' | '/wallet/add-bank' | '/wallet/add-alipay';
  }
> = {
  usdt: {
    addLabel: '添加虚拟币账户',
    hint: '*最多添加1个虚拟账户',
    addRoute: '/wallet/add-usdt',
  },
  bank: {
    addLabel: '添加银行卡',
    hint: '*最多添加1个银行账户',
    addRoute: '/wallet/add-bank',
  },
  alipay: {
    addLabel: '添加支付宝',
    hint: '*最多添加1个支付宝账户',
    addRoute: '/wallet/add-alipay',
  },
};

function parseTab(raw?: string | string[]): WalletTab {
  const value = Array.isArray(raw) ? raw[0] : raw;
  if (value === 'bank' || value === 'alipay' || value === 'usdt') {
    return value;
  }
  return 'usdt';
}

export default function WalletManageScreen() {
  const router = useRouter();
  const { tab } = useLocalSearchParams<{ tab?: string }>();
  const [activeTab, setActiveTab] = useState<WalletTab>(() => parseTab(tab));
  const meta = TAB_META[activeTab];

  return (
    <AppBackground>
      <PageHeader title="钱包管理" />
      <View style={styles.tabs}>
        {TABS.map((item) => {
          const active = item.key === activeTab;
          return (
            <Pressable key={item.key} onPress={() => setActiveTab(item.key)} style={styles.tabItem}>
              <Text style={[styles.tabText, active && styles.tabTextActive]}>{item.label}</Text>
              {active ? <View style={styles.tabLine} /> : <View style={styles.tabLinePlaceholder} />}
            </Pressable>
          );
        })}
      </View>

      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.content}
        onRefresh={async () => {}}
      >
        <Pressable onPress={() => router.push(meta.addRoute)} style={styles.addBtn}>
          <Text style={styles.addText}>+  {meta.addLabel}</Text>
        </Pressable>
        <Text style={styles.hint}>{meta.hint}</Text>
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tabs: {
    flexDirection: 'row',
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 10,
  },
  tabText: {
    color: 'rgba(220, 230, 245, 0.72)',
    fontSize: 15,
  },
  tabTextActive: {
    color: colors.text,
    fontWeight: '700',
  },
  tabLine: {
    marginTop: 8,
    width: 28,
    height: 3,
    borderRadius: 2,
    backgroundColor: '#E85A5A',
  },
  tabLinePlaceholder: {
    marginTop: 8,
    height: 3,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 32,
  },
  addBtn: {
    height: 52,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(140, 180, 230, 0.35)',
    backgroundColor: 'rgba(8, 20, 44, 0.72)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  addText: {
    color: 'rgba(220, 232, 248, 0.92)',
    fontSize: 15,
    fontWeight: '600',
  },
  hint: {
    marginTop: 12,
    textAlign: 'center',
    color: 'rgba(170, 190, 220, 0.7)',
    fontSize: 12,
  },
});
