import { useRouter } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { createAppPayAccount, fetchAppPayAccounts } from '@/api/app-pay-account';
import { ApiError } from '@/api/request';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

export default function AddAlipayWalletScreen() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [account, setAccount] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async () => {
    if (!name.trim() || !account.trim()) {
      modalWarning('请填写完整信息');
      return;
    }
    setSubmitting(true);
    try {
      const existing = await fetchAppPayAccounts('ALIPAY');
      if (existing.length >= 1) {
        modalWarning('最多添加1个支付宝账户');
        return;
      }
      const msg = await createAppPayAccount({
        accountType: 'ALIPAY',
        accountName: name.trim(),
        accountNo: account.trim(),
      });
      modalSuccess(msg);
      router.replace('/wallet?tab=alipay');
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '添加失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground>
      <PageHeader title="添加支付宝账户" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        onRefresh={async () => {}}
      >
        <Field
          label="姓名"
          value={name}
          onChangeText={setName}
          placeholder="请输入支付宝账户真实姓名"
        />
        <Field
          label="支付宝账户"
          value={account}
          onChangeText={setAccount}
          placeholder="请输入支付宝账户"
        />

        <View style={styles.action}>
          <PrimaryButton
            title="确认添加"
            compact
            onPress={() => void onSubmit()}
            disabled={submitting}
          />
        </View>
      </RefreshableScrollView>
    </AppBackground>
  );
}

function Field({
  label,
  value,
  onChangeText,
  placeholder,
}: {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
  placeholder: string;
}) {
  return (
    <View style={styles.field}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={colors.placeholder}
        style={styles.input}
        autoCapitalize="none"
        autoCorrect={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
    paddingTop: 8,
  },
  field: {
    marginBottom: 18,
  },
  label: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
    marginBottom: 10,
  },
  input: {
    height: 48,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(120, 160, 210, 0.22)',
    backgroundColor: 'rgba(8, 22, 48, 0.72)',
    color: colors.text,
    paddingHorizontal: 14,
    fontSize: 15,
  },
  action: {
    marginTop: 18,
  },
});
