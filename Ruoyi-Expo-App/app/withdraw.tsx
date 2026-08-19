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

export default function WithdrawScreen() {
  const [amount, setAmount] = useState('0');
  const [method, setMethod] = useState('usdt');

  return (
    <AppBackground>
      <PageHeader title="提现" />
      <View style={{ paddingHorizontal: 16, gap: 12 }}>
        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>账户可用余额</Text>
            <Text style={styles.link}>提现记录 ›</Text>
          </View>
          <Text style={styles.money}>¥ {mockUser.balanceCny}</Text>
          <Text style={styles.sub}>USDT {mockUser.balanceUsdt}</Text>
        </GlassCard>
        <GlassCard>
          <Text style={styles.label}>
            提现金额 <Text style={{ color: colors.danger }}>（通道拥堵可联系在线客服）</Text>
          </Text>
          <TextInput value={amount} onChangeText={setAmount} keyboardType="numeric" style={styles.input} />
        </GlassCard>
        <GlassCard>
          <Text style={styles.label}>选择收款方式</Text>
          {[
            { key: 'alipay', label: '支付宝', icon: images.payAlipay },
            { key: 'usdt', label: 'USDT', icon: images.payUsdt },
          ].map((item) => (
            <Pressable key={item.key} style={styles.method} onPress={() => setMethod(item.key)}>
              <View style={[styles.radio, method === item.key && styles.radioOn]} />
              <Image source={item.icon} style={styles.icon} contentFit="contain" />
              <Text style={styles.methodText}>{item.label}</Text>
              <Text style={styles.right}>{mockUser.balanceCny}</Text>
            </Pressable>
          ))}
        </GlassCard>
        <PrimaryButton title="提 现" onPress={() => toast('演示环境，暂不发起提现')} />
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  label: { color: colors.muted },
  link: { color: colors.text },
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
  methodText: { color: colors.text, flex: 1 },
  right: { color: colors.text },
});
