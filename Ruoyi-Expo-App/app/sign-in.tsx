import { Link, useRouter } from 'expo-router';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { appLogin, fetchAppCaptcha } from '@/api/app-auth';
import { ApiError } from '@/api/request';
import { AuthCaptchaRow } from '@/components/ui/AuthCaptchaRow';
import { AuthField } from '@/components/ui/AuthField';
import { AuthScreen } from '@/components/ui/AuthScreen';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { modalError, modalWarning, toastThenNavigate } from '@/utils/toast';

export default function SignInScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [code, setCode] = useState('');
  const [uuid, setUuid] = useState('');
  const [captchaText, setCaptchaText] = useState('');
  const [captchaEnabled, setCaptchaEnabled] = useState(true);
  const [loading, setLoading] = useState(false);

  const loadCaptcha = useCallback(async () => {
    try {
      const res = await fetchAppCaptcha();
      setCaptchaEnabled(res.enabled);
      setUuid(res.uuid);
      setCaptchaText(res.text);
      setCode('');
    } catch (error) {
      modalError(error instanceof ApiError ? error.message : '验证码加载失败');
    }
  }, []);

  useEffect(() => {
    void loadCaptcha();
  }, [loadCaptcha]);

  const canSubmit = useMemo(() => {
    const base = phone.trim().length >= 6 && password.length >= 4;
    if (!captchaEnabled) {
      return base;
    }
    return base && code.trim().length >= 1 && uuid.length > 0;
  }, [phone, password, code, uuid, captchaEnabled]);

  const onSubmit = async () => {
    if (!canSubmit) {
      modalWarning('请填写手机号、密码和验证码');
      return;
    }
    setLoading(true);
    try {
      await appLogin({
        phone: phone.trim(),
        password,
        code: code.trim(),
        uuid,
      });
      toastThenNavigate('登录成功', () => router.replace('/(tabs)'), { type: 'success' });
    } catch (error) {
      modalError(error instanceof ApiError ? error.message : '登录失败');
      void loadCaptcha();
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthScreen formStart={0.34} rows={5}>
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
          placeholder="请输入密码"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />
        {captchaEnabled ? (
          <AuthCaptchaRow
            value={code}
            onChangeText={setCode}
            captchaText={captchaText}
            onRefresh={loadCaptcha}
          />
        ) : null}
        <View style={styles.btnWrap}>
          <PrimaryButton title="登 录" onPress={() => void onSubmit()} disabled={loading} />
        </View>
        <View style={styles.links}>
          <Link href="/sign-up" asChild>
            <Pressable>
              <Text style={styles.link}>立即注册</Text>
            </Pressable>
          </Link>
          <Pressable onPress={() => router.push('/service')}>
            <Text style={styles.link}>联系客服</Text>
          </Pressable>
        </View>
      </RefreshableScrollView>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  formContent: {
    flexGrow: 1,
    gap: 12,
  },
  btnWrap: { marginTop: 8 },
  links: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 14,
  },
  link: { color: '#8BB8FF', fontSize: 14 },
});
