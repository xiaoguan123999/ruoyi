import { useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AuthField } from '@/components/ui/AuthField';
import { AuthScreen } from '@/components/ui/AuthScreen';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { images } from '@/constants/images';
import { toast } from '@/utils/toast';

export default function SignUpScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [payPassword, setPayPassword] = useState('');
  const [invite, setInvite] = useState('');
  const [code, setCode] = useState('');
  const [captcha, setCaptcha] = useState(() => String(Math.floor(1000 + Math.random() * 9000)));

  const refreshCaptcha = () => setCaptcha(String(Math.floor(1000 + Math.random() * 9000)));

  const onSubmit = () => {
    if (!phone || !password || !confirm || !payPassword || !invite || !code) {
      toast('请填写完整信息');
      return;
    }
    if (password !== confirm) {
      toast('两次密码不一致');
      return;
    }
    if (code !== captcha) {
      toast('验证码不正确');
      refreshCaptcha();
      return;
    }
    toast('注册成功，请登录');
    router.replace('/sign-in');
  };

  return (
    <AuthScreen formStart={0.35}>
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
      <View style={styles.captchaRow}>
        <View style={{ flex: 1 }}>
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
      <View style={{ marginTop: 8 }}>
        <PrimaryButton title="注 册" onPress={onSubmit} />
      </View>
      <Pressable onPress={() => router.replace('/sign-in')} style={styles.loginLink}>
        <Text style={styles.loginLinkText}>已有账号，返回登录</Text>
      </Pressable>
    </AuthScreen>
  );
}

const styles = StyleSheet.create({
  captchaRow: { flexDirection: 'row', gap: 10, alignItems: 'center' },
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
  loginLink: {
    alignSelf: 'center',
    marginTop: 4,
    paddingVertical: 8,
  },
  loginLinkText: {
    color: '#8BB8FF',
    fontSize: 14,
  },
});
