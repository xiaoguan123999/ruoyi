import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';

import { Text, TextInput } from '@/components/ui/AppText';
import { fetchAppProfile, formatBalance, toNumberOrZero } from '@/api/app-auth';
import { setAppPayPassword } from '@/api/app-member';
import { ApiError } from '@/api/request';
import {
  applyAppWalletExchange,
  fetchAppWalletExchangeConfig,
} from '@/api/app-trade';
import type { AppWalletExchangeConfig } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PayPasswordModal, type PayPasswordMode } from '@/components/ui/PayPasswordModal';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

type WalletTab = 'PRODUCT' | 'PROMO';
type Currency = 'CNY' | 'USDT';

const walletTabs: { key: WalletTab; label: string }[] = [
  { key: 'PRODUCT', label: '产品收益' },
  { key: 'PROMO', label: '推广收益' },
];

function parseAmount(value: string): number {
  const cleaned = value.replace(/[^\d.]/g, '');
  const parts = cleaned.split('.');
  const normalized =
    parts.length <= 1 ? cleaned : `${parts[0]}.${parts.slice(1).join('').slice(0, 4)}`;
  const next = Number.parseFloat(normalized);
  return Number.isFinite(next) ? next : 0;
}

function calcToAmount(from: Currency, amount: number, rate: number): number {
  if (amount <= 0 || rate <= 0) {
    return 0;
  }
  if (from === 'CNY') {
    return Math.floor((amount / rate) * 10000) / 10000;
  }
  return Math.floor(amount * rate * 10000) / 10000;
}

export default function WalletExchangeScreen() {
  const [activeTab, setActiveTab] = useState<WalletTab>('PRODUCT');
  const [fromCurrency, setFromCurrency] = useState<Currency>('CNY');
  const [amount, setAmount] = useState('');
  const [config, setConfig] = useState<AppWalletExchangeConfig | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [payVisible, setPayVisible] = useState(false);
  const [payMode, setPayMode] = useState<PayPasswordMode>('verify');
  const [hasPayPassword, setHasPayPassword] = useState(true);

  const load = useCallback(async () => {
    try {
      const [nextConfig, profile] = await Promise.all([
        fetchAppWalletExchangeConfig(),
        fetchAppProfile().catch(() => null),
      ]);
      setConfig(nextConfig);
      if (profile && typeof profile.hasPayPassword === 'boolean') {
        setHasPayPassword(profile.hasPayPassword);
      }
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '加载兑换信息失败');
      }
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  const toCurrency: Currency = fromCurrency === 'CNY' ? 'USDT' : 'CNY';
  const rate = toNumberOrZero(config?.usdtToCny) || 6.25;
  const available = useMemo(() => {
    if (!config) {
      return 0;
    }
    if (activeTab === 'PRODUCT') {
      return fromCurrency === 'CNY' ? config.productCny : config.productUsdt;
    }
    return fromCurrency === 'CNY' ? config.promoCny : config.promoUsdt;
  }, [activeTab, config, fromCurrency]);

  const amountValue = parseAmount(amount);
  const toAmount = calcToAmount(fromCurrency, amountValue, rate);
  const walletLabel = walletTabs.find((item) => item.key === activeTab)?.label ?? '';

  const swapDirection = () => {
    setFromCurrency((prev) => (prev === 'CNY' ? 'USDT' : 'CNY'));
    setAmount('');
  };

  const onFillAll = () => {
    if (available <= 0) {
      return;
    }
    setAmount(String(available));
  };

  const onPressSubmit = () => {
    if (amountValue <= 0) {
      modalWarning('请输入兑换金额');
      return;
    }
    if (amountValue > available) {
      modalWarning('可用余额不足');
      return;
    }
    if (toAmount <= 0) {
      modalWarning('换算后金额过小，请加大转出金额');
      return;
    }
    setPayMode(hasPayPassword ? 'verify' : 'set');
    setPayVisible(true);
  };

  const onConfirmPay = async (payPassword: string) => {
    if (submitting) {
      return;
    }
    if (payPassword.trim().length < 4) {
      modalWarning('请输入支付密码');
      return;
    }
    setSubmitting(true);
    try {
      if (payMode === 'set') {
        await setAppPayPassword(payPassword);
        setHasPayPassword(true);
      }
      const result = await applyAppWalletExchange({
        typeCode: activeTab,
        fromCurrency,
        amount: amountValue,
        payPassword,
      });
      setPayVisible(false);
      setAmount('');
      await load();
      modalSuccess(
        `${walletLabel}兑换成功：转出 ${formatBalance(result.fromAmount)} ${result.fromCurrency}，转入 ${formatBalance(result.toAmount)} ${result.toCurrency}`,
      );
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '兑换失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="兑换" />
      <RefreshableScrollView onRefresh={load} contentContainerStyle={styles.content}>
        <GlassCard style={styles.card}>
          <Text style={styles.sectionLabel}>选择钱包</Text>
          <View style={styles.tabs}>
            {walletTabs.map((tab) => {
              const active = tab.key === activeTab;
              return (
                <Pressable
                  key={tab.key}
                  onPress={() => {
                    setActiveTab(tab.key);
                    setAmount('');
                  }}
                  style={[styles.tab, active && styles.tabActive]}
                >
                  <Text style={[styles.tabText, active && styles.tabTextActive]}>{tab.label}</Text>
                </Pressable>
              );
            })}
          </View>
          <Text style={styles.hint}>仅支持同一钱包内 CNY 与 USDT 兑换，不能跨钱包</Text>
        </GlassCard>

        <GlassCard style={styles.card}>
          <View style={styles.rowBetween}>
            <Text style={styles.sectionLabel}>转出</Text>
            <Pressable onPress={onFillAll}>
              <Text style={styles.link}>
                可用 {fromCurrency === 'CNY' ? '¥' : ''}
                {formatBalance(available)}
                {fromCurrency === 'USDT' ? ' USDT' : ''}
              </Text>
            </Pressable>
          </View>
          <View style={styles.currencyRow}>
            <Pressable
              style={[styles.currencyChip, fromCurrency === 'CNY' && styles.currencyChipActive]}
              onPress={() => {
                setFromCurrency('CNY');
                setAmount('');
              }}
            >
              <Text style={styles.currencyText}>CNY</Text>
            </Pressable>
            <Pressable
              style={[styles.currencyChip, fromCurrency === 'USDT' && styles.currencyChipActive]}
              onPress={() => {
                setFromCurrency('USDT');
                setAmount('');
              }}
            >
              <Text style={styles.currencyText}>USDT</Text>
            </Pressable>
            <Pressable style={styles.swapBtn} onPress={swapDirection}>
              <Text style={styles.swapText}>⇄</Text>
            </Pressable>
          </View>
          <TextInput
            value={amount}
            onChangeText={setAmount}
            keyboardType="decimal-pad"
            placeholder={`请输入${fromCurrency}金额`}
            placeholderTextColor="rgba(180,200,230,0.45)"
            style={styles.input}
          />
        </GlassCard>

        <GlassCard style={styles.card}>
          <Text style={styles.sectionLabel}>转入预估（{toCurrency}）</Text>
          <Text style={styles.toAmount}>
            {toCurrency === 'CNY' ? '¥ ' : ''}
            {formatBalance(toAmount)}
            {toCurrency === 'USDT' ? ' USDT' : ''}
          </Text>
          <Text style={styles.rateText}>当前汇率：1 USDT = {formatBalance(rate)} CNY</Text>
          <Text style={styles.feeText}>无手续费</Text>
        </GlassCard>

        <PrimaryButton title="确认兑换" onPress={onPressSubmit} disabled={submitting} />
      </RefreshableScrollView>

      <PayPasswordModal
        visible={payVisible}
        mode={payMode}
        title={payMode === 'set' ? '设置支付密码' : '请输入支付密码'}
        hintText={
          payMode === 'set'
            ? '检测到您尚未设置支付密码，请先设置后再兑换'
            : '为保障资金安全，兑换前需验证支付密码'
        }
        confirmLabelText={payMode === 'set' ? '确认设置' : '确认兑换'}
        submitting={submitting}
        onCancel={() => setPayVisible(false)}
        onConfirm={(pwd) => void onConfirmPay(pwd)}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 16,
    paddingBottom: 40,
    gap: 14,
  },
  card: {
    padding: 16,
    gap: 12,
  },
  sectionLabel: {
    color: 'rgba(200, 220, 255, 0.85)',
    fontSize: 14,
    fontWeight: '600',
  },
  tabs: {
    flexDirection: 'row',
    gap: 10,
  },
  tab: {
    flex: 1,
    minHeight: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.06)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  tabActive: {
    backgroundColor: 'rgba(59,140,255,0.28)',
    borderColor: 'rgba(91,168,255,0.65)',
  },
  tabText: {
    color: 'rgba(200,220,255,0.7)',
    fontSize: 15,
    fontWeight: '600',
  },
  tabTextActive: {
    color: colors.text,
  },
  hint: {
    color: 'rgba(180,200,230,0.55)',
    fontSize: 12,
    lineHeight: 18,
  },
  rowBetween: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  link: {
    color: '#7EB6FF',
    fontSize: 13,
    fontWeight: '600',
  },
  currencyRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  currencyChip: {
    paddingHorizontal: 16,
    minHeight: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.06)',
  },
  currencyChipActive: {
    backgroundColor: 'rgba(59,140,255,0.35)',
  },
  currencyText: {
    color: colors.text,
    fontWeight: '700',
    fontSize: 14,
  },
  swapBtn: {
    marginLeft: 'auto',
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.08)',
  },
  swapText: {
    color: '#9CC7FF',
    fontSize: 18,
    fontWeight: '700',
  },
  input: {
    minHeight: 48,
    borderRadius: 12,
    paddingHorizontal: 14,
    color: colors.text,
    fontSize: 20,
    fontWeight: '700',
    backgroundColor: 'rgba(0,0,0,0.22)',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  toAmount: {
    color: colors.text,
    fontSize: 28,
    fontWeight: '800',
  },
  rateText: {
    color: 'rgba(200,220,255,0.75)',
    fontSize: 13,
  },
  feeText: {
    color: 'rgba(160,190,220,0.55)',
    fontSize: 12,
  },
});
