import { useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { appRegister, fetchAppCaptcha } from '@/api/app-auth';
import { ApiError } from '@/api/request';
import { AuthCaptchaRow } from '@/components/ui/AuthCaptchaRow';
import { AuthField } from '@/components/ui/AuthField';
import { AuthScreen } from '@/components/ui/AuthScreen';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { pickInviteCodeFromParams } from '@/utils/invite';
import { modalError, modalWarning, toastThenNavigate } from '@/utils/toast';

export default function SignUpScreen() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [payPassword, setPayPassword] = useState('');
  const [invite, setInvite] = useState(() => pickInviteCodeFromParams(params as Record<string, unknown>));
  const [code, setCode] = useState('');
  const [uuid, setUuid] = useState('');
  const [captchaUri, setCaptchaUri] = useState('');
  const [captchaEnabled, setCaptchaEnabled] = useState(true);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fromQuery = pickInviteCodeFromParams(params as Record<string, unknown>);
    if (fromQuery) {
      setInvite(fromQuery);
    }
  }, [params]);

  const loadCaptcha = useCallback(async () => {
    try {
      const res = await fetchAppCaptcha();
      setCaptchaEnabled(res.enabled);
      setUuid(res.uuid);
      setCaptchaUri(res.img);
      setCode('');
    } catch (error) {
      modalError(error instanceof ApiError ? error.message : '验证码加载失败');
    }
  }, []);

  useEffect(() => {
    void loadCaptcha();
  }, [loadCaptcha]);

  const canSubmit = useMemo(() => {
    const base =
      phone.trim().length >= 6 &&
      password.length >= 4 &&
      confirm.length >= 4 &&
      payPassword.length >= 4 &&
      invite.trim().length > 0;
    if (!captchaEnabled) {
      return base;
    }
    return base && code.trim().length >= 1 && uuid.length > 0;
  }, [phone, password, confirm, payPassword, invite, code, uuid, captchaEnabled]);

  const onSubmit = async () => {
    if (!phone || !password || !confirm || !payPassword || !invite) {
      modalWarning('请填写完整信息');
      return;
    }
    if (password !== confirm) {
      modalWarning('两次密码不一致');
      return;
    }
    if (captchaEnabled && (!code.trim() || !uuid)) {
      modalWarning('请填写验证码');
      return;
    }
    setLoading(true);
    try {
      await appRegister({
        phone: phone.trim(),
        password,
        code: code.trim(),
        uuid,
        inviteCode: invite.trim() || undefined,
      });
      toastThenNavigate('注册成功', () => router.replace('/(tabs)'), { type: 'success' });
    } catch (error) {
      modalError(error instanceof ApiError ? error.message : '注册失败');
      void loadCaptcha();
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthScreen formStart={0.32} rows={8}>
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.formContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        onRefresh={loadCaptcha}
      >
        <AuthField
          icon={images.iconPhone}
          placeholder="请输入手机号码"
          value={phone}
          onChangeText={setPhone}
          keyboardType="phone-pad"
        />
        <AuthField
          icon={images.iconPassword}
          placeholder="请设置登录密码"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />
        <AuthField
          icon={images.iconPassword}
          placeholder="请确认登录密码"
          value={confirm}
          onChangeText={setConfirm}
          secureTextEntry
        />
        <AuthField
          icon={images.iconPassword}
          placeholder="请设置交易密码"
          value={payPassword}
          onChangeText={setPayPassword}
          secureTextEntry
        />
        <AuthField
          icon={images.iconLock}
          placeholder="请输入邀请码"
          value={invite}
          onChangeText={setInvite}
        />
        {captchaEnabled ? (
          <AuthCaptchaRow
            value={code}
            onChangeText={setCode}
            captchaUri={captchaUri}
            onRefresh={loadCaptcha}
          />
        ) : null}
        <View style={styles.submitWrap}>
          <PrimaryButton title="注 册" onPress={() => void onSubmit()} disabled={loading || !canSubmit} />
        </View>
        <Pressable onPress={() => router.replace('/sign-in')} style={styles.loginLink}>
          <Text style={styles.loginLinkText}>已有账号，返回登录</Text>
        </Pressable>
      </RefreshableScrollView>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  formContent: {
    flexGrow: 1,
    gap: 12,
  },
  submitWrap: {
    marginTop: 4,
  },
  loginLink: {
    alignSelf: 'center',
    marginTop: 8,
    paddingVertical: 6,
  },
  loginLinkText: {
    color: '#8BB8FF',
    fontSize: 14,
  },
});
