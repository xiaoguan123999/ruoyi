import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useRef, useState } from 'react';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { formatBalance, toNumberOrZero } from '@/api/app-auth';
import { ApiError } from '@/api/request';
import { applyAppWithdraw, fetchAppWallet, parseAmountInput } from '@/api/app-trade';
import type { AppWallet } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { DualBalance } from '@/components/ui/DualBalance';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

type WithdrawTab = 'income' | 'assist';

type WithdrawMethod = {
  key: string;
  label: string;
  icon: number;
  currency: 'CNY' | 'USDT';
};

const tabs: { key: WithdrawTab; label: string }[] = [
  { key: 'income', label: '产品收益' },
  { key: 'assist', label: '助力值' },
];

export default function WithdrawScreen() {
  const router = useRouter();
  const amountRef = useRef<TextInput>(null);
  const [amount, setAmount] = useState('');
  const [activeTab, setActiveTab] = useState<WithdrawTab>('income');
  const [methods, setMethods] = useState<WithdrawMethod[]>([]);
  const [method, setMethod] = useState<string>('');
  const [submitting, setSubmitting] = useState(false);
  const [wallet, setWallet] = useState<AppWallet | null>(null);

  const load = useCallback(async () => {
    try {
      const nextWallet = await fetchAppWallet();
      setWallet(nextWallet);
    } catch {
    }
    // 收款账户接口未对接前保持空列表，不展示假收款方式
    setMethods([]);
    setMethod('');
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
      const timer = setTimeout(() => amountRef.current?.focus(), 100);
      return () => clearTimeout(timer);
    }, [load]),
  );

  const selected = methods.find((item) => item.key === method);
  const tabLabel = tabs.find((item) => item.key === activeTab)?.label ?? '产品收益';
  const availableCny =
    activeTab === 'income'
      ? toNumberOrZero(wallet?.cnyProductIncome)
      : toNumberOrZero(wallet?.cnyAssistValue);
  const availableUsdt =
    activeTab === 'income'
      ? toNumberOrZero(wallet?.usdtProductIncome)
      : toNumberOrZero(wallet?.usdtAssistValue);
  const availableAmount =
    selected?.currency === 'USDT'
      ? availableUsdt
      : selected?.currency === 'CNY'
        ? availableCny
        : 0;

  const goAddPayMethod = () => {
    router.push('/wallet');
  };

  const onSubmit = async () => {
    if (!selected) {
      modalWarning('请先添加收款方式');
      return;
    }
    const value = parseAmountInput(amount);
    if (value <= 0) {
      modalWarning('请输入有效提现金额');
      return;
    }
    if (value > availableAmount) {
      modalWarning(`提现金额不能超过${tabLabel}`);
      return;
    }
    setSubmitting(true);
    try {
      const message = await applyAppWithdraw({
        amount: value,
        currency: selected.currency,
        accountInfo: selected.label,
        remark: `${tabLabel}-${selected.label}`,
      });
      modalSuccess(message);
      setAmount('');
      await load();
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '提现申请失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="提现" />
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

      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        onRefresh={load}
      >
        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>账户可用余额</Text>
            <Pressable onPress={() => router.push('/fund-details?tab=withdraw')}>
              <Text style={styles.link}>提现记录 ›</Text>
            </Pressable>
          </View>
          <View style={styles.balanceWrap}>
            <DualBalance cny={availableCny} usdt={availableUsdt} />
          </View>
        </GlassCard>

        <GlassCard>
          <Text style={styles.label}>
            提现金额{' '}
            <Text style={styles.hint}>（通道拥堵可联系在线客服充值）</Text>
          </Text>
          <TextInput
            ref={amountRef}
            value={amount}
            onChangeText={setAmount}
            keyboardType="numeric"
            style={styles.input}
            placeholder={selected?.currency === 'CNY' ? '¥ 0' : 'USDT 0'}
            placeholderTextColor={colors.placeholder}
            autoFocus
          />
        </GlassCard>

        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>选择收款方式</Text>
            {methods.length > 0 ? <Text style={styles.label}>可用余额</Text> : null}
          </View>

          {methods.length === 0 ? (
            <Pressable style={styles.emptyWrap} onPress={goAddPayMethod}>
              <Text style={styles.emptyText}>暂未添加收款方式</Text>
              <Text style={styles.emptyLink}>去添加 ›</Text>
            </Pressable>
          ) : (
            methods.map((item) => (
              <Pressable key={item.key} style={styles.method} onPress={() => setMethod(item.key)}>
                <View style={[styles.radio, method === item.key && styles.radioOn]} />
                <Image source={item.icon} style={styles.icon} contentFit="contain" />
                <Text style={styles.methodText}>{item.label}</Text>
                <Text style={styles.right}>
                  {formatBalance(item.currency === 'USDT' ? availableUsdt : availableCny)}
                </Text>
              </Pressable>
            ))
          )}
        </GlassCard>

        <PrimaryButton title="提 现" onPress={() => void onSubmit()} disabled={submitting} />
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tabs: {
    flexDirection: 'row',
    paddingHorizontal: 48,
    marginBottom: 12,
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
  content: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 12,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  label: {
    color: colors.muted,
    fontSize: 13,
  },
  link: {
    color: colors.text,
    fontSize: 13,
  },
  balanceWrap: {
    marginTop: 14,
  },
  hint: {
    color: colors.danger,
  },
  input: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '700',
    borderBottomWidth: 1,
    borderBottomColor: '#fff',
    marginTop: 12,
    paddingVertical: 8,
  },
  emptyWrap: {
    marginTop: 16,
    paddingVertical: 18,
    alignItems: 'center',
    gap: 8,
  },
  emptyText: {
    color: colors.muted,
    fontSize: 14,
  },
  emptyLink: {
    color: colors.accent,
    fontSize: 14,
    fontWeight: '600',
  },
  method: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    gap: 10,
  },
  radio: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: colors.text,
  },
  radioOn: {
    backgroundColor: colors.accent,
    borderColor: colors.accent,
  },
  icon: {
    width: 22,
    height: 22,
  },
  methodText: {
    color: colors.text,
    flex: 1,
  },
  right: {
    color: colors.text,
  },
});
