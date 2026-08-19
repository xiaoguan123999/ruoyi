import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { toast } from '@/utils/toast';

export default function KycScreen() {
  const [name, setName] = useState('');
  const [idNo, setIdNo] = useState('');

  return (
    <AppBackground>
      <PageHeader title="实名认证" />
      <View style={{ paddingHorizontal: 16 }}>
        <GlassCard>
          <Text style={styles.tip}>请使用真实有效身份证信息认证</Text>
          <Text style={styles.section}>实名认证信息</Text>
          <TextInput
            value={name}
            onChangeText={setName}
            placeholder="请输入姓名"
            placeholderTextColor={colors.placeholder}
            style={styles.input}
          />
          <TextInput
            value={idNo}
            onChangeText={setIdNo}
            placeholder="请输入身份证号码"
            placeholderTextColor={colors.placeholder}
            style={styles.input}
          />
        </GlassCard>
        <View style={{ marginTop: 16 }}>
          <PrimaryButton title="立即实名" onPress={() => toast('演示环境，暂不提交认证')} />
        </View>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tip: { color: colors.text, marginBottom: 12 },
  section: { color: colors.muted, marginBottom: 10 },
  input: {
    backgroundColor: 'rgba(142,175,210,0.35)',
    borderRadius: 10,
    color: colors.text,
    paddingHorizontal: 12,
    paddingVertical: 12,
    marginBottom: 10,
  },
});
