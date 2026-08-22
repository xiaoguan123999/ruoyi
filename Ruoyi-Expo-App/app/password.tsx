import { useRouter } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { appLogout } from '@/api/app-auth';
import { updateAppPassword } from '@/api/app-member';
import { ApiError } from '@/api/request';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError, modalWarning, toastThenNavigate } from '@/utils/toast';

const PASSWORD_MIN = 5;
const PASSWORD_MAX = 20;

export default function PasswordScreen() {
  const router = useRouter();
  const [oldPwd, setOldPwd] = useState('');
  const [nextPwd, setNextPwd] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async () => {
    if (!oldPwd || !nextPwd || !confirm) {
      modalWarning('请填写完整信息');
      return;
    }
    if (nextPwd.length < PASSWORD_MIN || nextPwd.length > PASSWORD_MAX) {
      modalWarning(`新密码长度需为 ${PASSWORD_MIN}–${PASSWORD_MAX} 位`);
      return;
    }
    if (nextPwd !== confirm) {
      modalWarning('两次密码不一致');
      return;
    }

    setSubmitting(true);
    try {
      const message = await updateAppPassword({
        oldPassword: oldPwd,
        newPassword: nextPwd,
        confirmPassword: confirm,
      });
      setOldPwd('');
      setNextPwd('');
      setConfirm('');
      // 先轻提示，关闭后再退出并跳转，避免 AuthGate 抢先跳登录把 toast 清掉
      toastThenNavigate(
        message || '密码修改成功，请重新登录',
        () => {
          void appLogout().finally(() => {
            router.replace('/sign-in');
          });
        },
        { type: 'success', presentation: 'toast', duration: 2200 },
      );
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '密码修改失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground>
      <PageHeader title="密码设置" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 28 }}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        onRefresh={async () => {}}
      >
        <GlassCard>
          <Field label="原密码" value={oldPwd} onChangeText={setOldPwd} />
          <Field label="新密码" value={nextPwd} onChangeText={setNextPwd} />
          <Field label="确认新密码" value={confirm} onChangeText={setConfirm} />
        </GlassCard>
        <View style={{ marginTop: 16 }}>
          <PrimaryButton title="保存" onPress={() => void onSubmit()} disabled={submitting} />
        </View>
      </RefreshableScrollView>
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
        autoCapitalize="none"
        autoCorrect={false}
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
