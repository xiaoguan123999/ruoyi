import { Link, useRouter } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AuthField } from '@/components/ui/AuthField';
import { AuthScreen } from '@/components/ui/AuthScreen';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { images } from '@/constants/images';
import { mockSignIn } from '@/utils/mock-auth';
import { toast } from '@/utils/toast';

export default function SignInScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [code, setCode] = useState('');
  const [captcha, setCaptcha] = useState(() => String(Math.floor(1000 + Math.random() * 9000)));
  const [loading, setLoading] = useState(false);

  const refreshCaptcha = () => setCaptcha(String(Math.floor(1000 + Math.random() * 9000)));

  const canSubmit = useMemo(
    () => phone.trim().length >= 6 && password.length >= 4 && code.length >= 4,
    [phone, password, code],
  );

  const onSubmit = async () => {
    if (!canSubmit) {
      toast('请填写完整信息');
      return;
    }
    if (code !== captcha) {
      toast('验证码不正确');
      refreshCaptcha();
      return;
    }
    setLoading(true);
    try {
      await mockSignIn(phone);
      toast('登录成功');
      router.replace('/(tabs)');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthScreen formStart={0.40}>
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
      <View style={styles.captchaRow}>
        <View style={styles.captchaInput}>
          <AuthField
            icon={images.iconCaptcha}
            placeholder="请输入验证码"
            value={code}
            onChangeText={setCode}
            keyboardType="number-pad"
          />
        </View>
        <Pressable onPress={refreshCaptcha} style={styles.captchaBox}>
          <Text style={styles.captchaText}>{captcha}</Text>
        </Pressable>
      </View>
      <View style={styles.btnWrap}>
        <PrimaryButton title="登 录" onPress={() => void onSubmit()} disabled={loading} />
      </View>
      <View style={styles.links}>
        <Link href="/sign-up" asChild>
          <Pressable>
            <Text style={styles.link}>立即注册</Text>
          </Pressable>
        </Link>
        <Pressable onPress={() => toast('客服功能开发中')}>
          <Text style={styles.link}>联系客服</Text>
        </Pressable>
      </View>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  captchaRow: { flexDirection: 'row', gap: 10, alignItems: 'center' },
  captchaInput: { flex: 1 },
  captchaBox: {
    width: 88,
    height: 46,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(140, 190, 255, 0.45)',
    backgroundColor: 'rgba(8, 28, 68, 0.55)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  captchaText: { color: '#FFFFFF', fontSize: 22, fontWeight: '700', letterSpacing: 2 },
  btnWrap: { marginTop: 8 },
  links: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 14,
  },
  link: { color: '#8BB8FF', fontSize: 14 },
});
