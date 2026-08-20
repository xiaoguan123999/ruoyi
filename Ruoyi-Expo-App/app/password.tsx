import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { modalInfo, modalWarning } from '@/utils/toast';

export default function PasswordScreen() {
  const [oldPwd, setOldPwd] = useState('');
  const [nextPwd, setNextPwd] = useState('');
  const [confirm, setConfirm] = useState('');

  return (
    <AppBackground>
      <PageHeader title="密码设置" />
      <View style={{ paddingHorizontal: 16 }}>
        <GlassCard>
          <Field label="原密码" value={oldPwd} onChangeText={setOldPwd} />
          <Field label="新密码" value={nextPwd} onChangeText={setNextPwd} />
          <Field label="确认新密码" value={confirm} onChangeText={setConfirm} />
        </GlassCard>
        <View style={{ marginTop: 16 }}>
          <PrimaryButton
            title="保存"
            onPress={() => {
              if (!oldPwd || !nextPwd || !confirm) {
                modalWarning('请填写完整信息');
                return;
              }
              if (nextPwd !== confirm) {
                modalWarning('两次密码不一致');
                return;
              }
              modalInfo('演示环境，密码未真正修改');
            }}
          />
        </View>
      </View>
    </AppBackground>
  );
}

function Field({
  label,
  value,
  onChangeText,
}: {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
}) {
  return (
    <View style={{ marginBottom: 12 }}>
      <Text style={{ color: colors.muted, marginBottom: 6 }}>{label}</Text>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        secureTextEntry
        placeholder={`请输入${label}`}
        placeholderTextColor={colors.placeholder}
        style={styles.input}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  input: {
    borderWidth: 1,
    borderColor: colors.inputBorder,
    backgroundColor: colors.inputBg,
    borderRadius: 10,
    color: colors.text,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
});
