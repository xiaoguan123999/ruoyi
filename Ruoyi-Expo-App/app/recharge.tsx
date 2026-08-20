import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { fetchAppProfile, formatBalance } from '@/api/app-auth';
import { ApiError } from '@/api/request';
import { applyAppRecharge, fetchAppWallet, parseAmountInput } from '@/api/app-trade';
import type { AppWallet } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { useAuth } from '@/hooks/useAuth';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

const methods = [
  { key: 'wechat', label: '微信', icon: images.payWechat, currency: 'CNY' as const },
  { key: 'alipay', label: '支付宝', icon: images.payAlipay, currency: 'CNY' as const },
  { key: 'usdt', label: 'USDT', icon: images.payUsdt, currency: 'USDT' as const },
  { key: 'bank', label: '银行卡（客服）', icon: images.payCard, currency: 'CNY' as const },
];

export default function RechargeScreen() {
  const router = useRouter();
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState('wechat');
  const [submitting, setSubmitting] = useState(false);
  const [wallet, setWallet] = useState<AppWallet | null>(null);
  const { user } = useAuth();

  const load = useCallback(async () => {
    try {
      const [nextWallet] = await Promise.all([fetchAppWallet(), fetchAppProfile()]);
      setWallet(nextWallet);
    } catch {
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  const cny = wallet?.cnyAvailable ?? user?.cnyAvailable;
  const usdt = wallet?.usdtAvailable ?? user?.usdtAvailable;
  const selected = methods.find((item) => item.key === method) ?? methods[0];

  const onSubmit = async () => {
    const value = parseAmountInput(amount);
    if (value <= 0) {
      modalWarning('请输入有效充值金额');
      return;
    }
    setSubmitting(true);
    try {
      const message = await applyAppRecharge({
        amount: value,
        currency: selected.currency,
        remark: selected.label,
      });
      modalSuccess(message);
      setAmount('');
      await load();
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '充值申请失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground>
      <PageHeader title="充值" />
      <View style={{ paddingHorizontal: 16, gap: 12 }}>
        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>账户可用余额</Text>
            <Pressable onPress={() => router.push('/fund-details')}>
              <Text style={styles.link}>充值记录 ›</Text>
            </Pressable>
          </View>
          <Text style={styles.money}>¥ {formatBalance(cny)}</Text>
          <Text style={styles.sub}>USDT {formatBalance(usdt)}</Text>
        </GlassCard>
        <GlassCard>
          <Text style={styles.label}>
            充值金额 <Text style={{ color: colors.danger }}>（通道拥堵可联系在线客服充值）</Text>
          </Text>
          <TextInput
            value={amount}
            onChangeText={setAmount}
            keyboardType="numeric"
            style={styles.input}
            placeholder={selected.currency === 'USDT' ? 'USDT 0' : '¥ 0'}
            placeholderTextColor={colors.placeholder}
          />
        </GlassCard>
        <GlassCard>
          <Text style={styles.label}>充值方式</Text>
          {methods.map((item) => (
            <Pressable key={item.key} style={styles.method} onPress={() => setMethod(item.key)}>
              <View style={[styles.radio, method === item.key && styles.radioOn]} />
              <Image source={item.icon} style={styles.icon} contentFit="contain" />
              <Text style={styles.methodText}>{item.label}</Text>
            </Pressable>
          ))}
        </GlassCard>
        <PrimaryButton title="充 值" onPress={() => void onSubmit()} disabled={submitting} />
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  label: { color: colors.muted, fontSize: 13 },
  link: { color: colors.text, fontSize: 13 },
  money: { color: colors.text, fontSize: 28, fontWeight: '800', marginTop: 8 },
  sub: { color: colors.text, marginTop: 4 },
  input: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '700',
    borderBottomWidth: 1,
    borderBottomColor: '#fff',
    marginTop: 12,
    paddingVertical: 8,
  },
  method: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, gap: 10 },
  radio: { width: 16, height: 16, borderRadius: 8, borderWidth: 1, borderColor: colors.text },
  radioOn: { backgroundColor: colors.accent, borderColor: colors.accent },
  icon: { width: 22, height: 22 },
  methodText: { color: colors.text, fontSize: 15 },
});
