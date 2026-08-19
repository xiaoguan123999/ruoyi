import { useState } from 'react';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { images } from '@/constants/images';
import { mockUser } from '@/constants/mock';
import { colors } from '@/theme/colors';
import { toast } from '@/utils/toast';

const methods = [
  { key: 'wechat', label: '微信', icon: images.payWechat },
  { key: 'alipay', label: '支付宝', icon: images.payAlipay },
  { key: 'usdt', label: 'USDT', icon: images.payUsdt },
  { key: 'bank', label: '银行卡（客服）', icon: images.payCard },
];

export default function RechargeScreen() {
  const [amount, setAmount] = useState('0');
  const [method, setMethod] = useState('wechat');

  return (
    <AppBackground>
      <PageHeader title="充值" />
      <View style={{ paddingHorizontal: 16, gap: 12 }}>
        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>账户可用余额</Text>
            <Text style={styles.link}>充值记录 ›</Text>
          </View>
          <Text style={styles.money}>¥ {mockUser.balanceCny}</Text>
          <Text style={styles.sub}>USDT {mockUser.balanceUsdt}</Text>
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
            placeholder="¥ 0"
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
        <PrimaryButton title="充 值" onPress={() => toast('演示环境，暂不发起支付')} />
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
