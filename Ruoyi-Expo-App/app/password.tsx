import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { appLogout, fetchAppProfile } from '@/api/app-auth';
import { saveAppPayPassword, updateAppPassword } from '@/api/app-member';
import { ApiError } from '@/api/request';
import { useAuth } from '@/hooks/useAuth';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning, toastThenNavigate } from '@/utils/toast';

const LOGIN_PASSWORD_MIN = 5;
const LOGIN_PASSWORD_MAX = 20;
const PAY_PASSWORD_MIN = 4;
const PAY_PASSWORD_MAX = 20;

type Mode = 'menu' | 'login' | 'pay';

export default function PasswordScreen() {
  const router = useRouter();
  const { user } = useAuth();
  const [mode, setMode] = useState<Mode>('menu');
  // 注册必填支付密码；仅接口明确返回 false 时才走「首次设置」
  const [hasPayPassword, setHasPayPassword] = useState(user?.hasPayPassword !== false);

  const [oldPwd, setOldPwd] = useState('');
  const [nextPwd, setNextPwd] = useState('');
  const [confirm, setConfirm] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const refreshProfile = useCallback(async () => {
    try {
      const profile = await fetchAppProfile();
      setHasPayPassword(profile.hasPayPassword !== false);
    } catch {
      setHasPayPassword(user?.hasPayPassword !== false);
    }
  }, [user?.hasPayPassword]);

  useFocusEffect(
    useCallback(() => {
      void refreshProfile();
    }, [refreshProfile]),
  );

  const resetFields = () => {
    setOldPwd('');
    setNextPwd('');
    setConfirm('');
  };

  const goMenu = () => {
    resetFields();
    setMode('menu');
  };

  const openLogin = () => {
    resetFields();
    setMode('login');
  };

  const openPay = () => {
    resetFields();
    setMode('pay');
  };

  const onSubmitLogin = async () => {
    if (!oldPwd || !nextPwd || !confirm) {
      modalWarning('请填写完整信息');
      return;
    }
    if (nextPwd.length < LOGIN_PASSWORD_MIN || nextPwd.length > LOGIN_PASSWORD_MAX) {
      modalWarning(`新密码长度需为 ${LOGIN_PASSWORD_MIN}–${LOGIN_PASSWORD_MAX} 位`);
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
      resetFields();
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

  const onSubmitPay = async () => {
    if (!nextPwd || !confirm) {
      modalWarning('请填写完整信息');
      return;
    }
    if (hasPayPassword && !oldPwd) {
      modalWarning('请输入原支付密码');
      return;
    }
    if (nextPwd.length < PAY_PASSWORD_MIN || nextPwd.length > PAY_PASSWORD_MAX) {
      modalWarning(`支付密码长度需为 ${PAY_PASSWORD_MIN}–${PAY_PASSWORD_MAX} 位`);
      return;
    }
    if (nextPwd !== confirm) {
      modalWarning('两次密码不一致');
      return;
    }

    setSubmitting(true);
    try {
      const message = await saveAppPayPassword({
        oldPassword: hasPayPassword ? oldPwd : undefined,
        newPassword: nextPwd,
        confirmPassword: confirm,
      });
      resetFields();
      setHasPayPassword(true);
      modalSuccess(message || '支付密码修改成功');
      setMode('menu');
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '支付密码修改失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const title =
    mode === 'login' ? '修改登录密码' : mode === 'pay' ? '修改支付密码' : '密码设置';

  return (
    <AppBackground>
      <PageHeader title={title} onBack={mode === 'menu' ? undefined : goMenu} />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 28 }}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        onRefresh={async () => {
          await refreshProfile();
        }}
      >
        {mode === 'menu' ? (
          <View style={styles.menuList}>
            <MenuItem label="修改登录密码" onPress={openLogin} />
            <MenuItem label="修改支付密码" onPress={openPay} />
          </View>
        ) : null}

        {mode === 'login' ? (
          <>
            <GlassCard>
              <Field label="原密码" value={oldPwd} onChangeText={setOldPwd} />
              <Field label="新密码" value={nextPwd} onChangeText={setNextPwd} />
              <Field label="确认新密码" value={confirm} onChangeText={setConfirm} />
            </GlassCard>
            <View style={styles.submitWrap}>
              <PrimaryButton title="保存" onPress={() => void onSubmitLogin()} disabled={submitting} />
            </View>
          </>
        ) : null}

        {mode === 'pay' ? (
          <>
            <GlassCard>
              {hasPayPassword ? (
                <Field label="原支付密码" value={oldPwd} onChangeText={setOldPwd} />
              ) : null}
              <Field
                label={hasPayPassword ? '新支付密码' : '支付密码'}
                value={nextPwd}
                onChangeText={setNextPwd}
              />
              <Field label="确认支付密码" value={confirm} onChangeText={setConfirm} />
            </GlassCard>
            <View style={styles.submitWrap}>
              <PrimaryButton
                title="保存"
                onPress={() => void onSubmitPay()}
                disabled={submitting}
              />
            </View>
          </>
        ) : null}
      </RefreshableScrollView>
    </AppBackground>
  );
}

function MenuItem({ label, onPress }: { label: string; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={styles.menuItem}>
      <Text style={styles.menuLabel}>{label}</Text>
      <Text style={styles.menuChevron}>›</Text>
    </Pressable>
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
    <View style={styles.field}>
      <Text style={styles.fieldLabel}>{label}</Text>
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
  menuList: {
    gap: 12,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(120, 160, 210, 0.35)',
    backgroundColor: 'rgba(10, 22, 42, 0.72)',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 16,
  },
  menuLabel: {
    flex: 1,
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },
  menuChevron: {
    color: 'rgba(200, 215, 235, 0.7)',
    fontSize: 22,
    lineHeight: 22,
    marginTop: -2,
  },
  submitWrap: {
    marginTop: 16,
  },
  field: {
    marginBottom: 12,
  },
  fieldLabel: {
    color: colors.muted,
    marginBottom: 6,
  },
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
