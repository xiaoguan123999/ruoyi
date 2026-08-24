import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useRef, useState } from 'react';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { formatBalance, toNumberOrZero } from '@/api/app-auth';
import {
  fetchAppPayAccounts,
  formatPayAccountLabel,
  payAccountCurrency,
} from '@/api/app-pay-account';
import { ApiError } from '@/api/request';
import { applyAppWithdraw, fetchAppWallet, parseAmountInput } from '@/api/app-trade';
import type { AppPayAccount, AppWallet } from '@/api/types';
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

const tabs: { key: WithdrawTab; label: string }[] = [
  { key: 'income', label: '产品收益' },
  { key: 'assist', label: '助力值' },
];

function accountIcon(account: AppPayAccount): number {
  if (account.accountType === 'USDT') return images.payUsdt;
  if (account.accountType === 'BANK') return images.payCard;
  return images.payAlipay;
}

/** 与提现 UI 原型一致的短名称 */
function accountTypeLabel(account: AppPayAccount): string {
  if (account.accountType === 'USDT') return 'USDT';
  if (account.accountType === 'BANK') return '银行卡';
  return '支付宝';
}

export default function WithdrawScreen() {
  const router = useRouter();
  const amountRef = useRef<TextInput>(null);
  const [amount, setAmount] = useState('');
  const [activeTab, setActiveTab] = useState<WithdrawTab>('income');
  const [accounts, setAccounts] = useState<AppPayAccount[]>([]);
  const [accountId, setAccountId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [wallet, setWallet] = useState<AppWallet | null>(null);

  const load = useCallback(async () => {
    try {
      const [nextWallet, nextAccounts] = await Promise.all([
        fetchAppWallet(),
        fetchAppPayAccounts(),
      ]);
      setWallet(nextWallet);
      setAccounts(nextAccounts);
      setAccountId((prev) => {
        if (prev && nextAccounts.some((item) => item.accountId === prev)) {
          return prev;
        }
        const preferred =
          nextAccounts.find((item) => item.isDefault === '1') ?? nextAccounts[0];
        return preferred?.accountId ?? null;
      });
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        // wallet 失败时仍尝试展示空收款方式
      }
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
      const timer = setTimeout(() => amountRef.current?.focus(), 100);
      return () => clearTimeout(timer);
    }, [load]),
  );

  const selected = accounts.find((item) => item.accountId === accountId);
  const selectedCurrency = selected ? payAccountCurrency(selected) : null;
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
    selectedCurrency === 'USDT'
      ? availableUsdt
      : selectedCurrency === 'CNY'
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
      modalWarning('提现余额不足');
      return;
    }
    setSubmitting(true);
    try {
      const message = await applyAppWithdraw({
        amount: value,
        currency: selectedCurrency || undefined,
        accountId: selected.accountId,
        remark: `${tabLabel}-${formatPayAccountLabel(selected)}`,
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
            placeholder={selectedCurrency === 'CNY' ? '¥ 0' : 'USDT 0'}
            placeholderTextColor={colors.placeholder}
            autoFocus
          />
        </GlassCard>

        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>选择收款方式</Text>
            <Text style={styles.label}>可用余额</Text>
          </View>

          {accounts.length === 0 ? (
            <Pressable style={styles.emptyWrap} onPress={goAddPayMethod}>
              <Text style={styles.emptyText}>暂未添加收款方式</Text>
              <Text style={styles.emptyLink}>去添加 ›</Text>
            </Pressable>
          ) : (
            accounts.map((item, index) => {
              const currency = payAccountCurrency(item);
              const selectedMethod = accountId === item.accountId;
              return (
                <Pressable
                  key={item.accountId}
                  style={[styles.method, index > 0 && styles.methodBorder]}
                  onPress={() => setAccountId(item.accountId)}
                >
                  <View style={[styles.radio, selectedMethod && styles.radioOn]}>
                    {selectedMethod ? <View style={styles.radioDot} /> : null}
                  </View>
                  <Image source={accountIcon(item)} style={styles.icon} contentFit="contain" />
                  <Text style={styles.methodText} numberOfLines={1}>
                    {accountTypeLabel(item)}
                  </Text>
                  <Text style={styles.right}>
                    {formatBalance(currency === 'USDT' ? availableUsdt : availableCny)}
                  </Text>
                </Pressable>
              );
            })
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
    paddingVertical: 14,
    gap: 10,
  },
  methodBorder: {
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: 'rgba(160, 190, 230, 0.28)',
  },
  radio: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 1.5,
    borderColor: 'rgba(220, 230, 245, 0.85)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  radioOn: {
    borderColor: '#3D8BFF',
  },
  radioDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#3D8BFF',
  },
  icon: {
    width: 24,
    height: 24,
  },
  methodText: {
    color: colors.text,
    flex: 1,
    fontSize: 15,
  },
  right: {
    color: colors.text,
    fontSize: 15,
  },
});
