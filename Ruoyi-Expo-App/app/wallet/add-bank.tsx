import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalWarning } from '@/utils/toast';

export default function AddBankWalletScreen() {
  const [name, setName] = useState('');
  const [bankName, setBankName] = useState('');
  const [cardNo, setCardNo] = useState('');

  const onSubmit = () => {
    if (!name.trim() || !bankName.trim() || !cardNo.trim()) {
      modalWarning('请填写完整信息');
      return;
    }
    modalWarning('收款账户接口暂未对接');
  };

  return (
    <AppBackground>
      <PageHeader title="添加银行卡" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        onRefresh={async () => {}}
      >
        <View style={styles.tip}>
          <Text style={styles.tipIcon}>ⓘ</Text>
          <Text style={styles.tipText}>
            为了您的资金能够迅速到账，请确保填写的姓名与银行卡的开户姓名一致
          </Text>
        </View>

        <Field label="持卡人姓名" value={name} onChangeText={setName} placeholder="请输入持卡人姓名" />
        <Field label="所属银行" value={bankName} onChangeText={setBankName} placeholder="请输入银行名称" />
        <Field
          label="银行卡号"
          value={cardNo}
          onChangeText={setCardNo}
          placeholder="请输入银行卡号"
          keyboardType="number-pad"
        />

        <View style={styles.action}>
          <PrimaryButton title="确认添加" onPress={onSubmit} />
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
  keyboardType,
}: {
  label: string;
  value: string;
  onChangeText: (v: string) => void;
  placeholder: string;
  keyboardType?: 'default' | 'number-pad';
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
        keyboardType={keyboardType}
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
  tip: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(90, 160, 230, 0.45)',
    backgroundColor: 'rgba(10, 28, 58, 0.55)',
    paddingHorizontal: 12,
    paddingVertical: 12,
    marginBottom: 22,
  },
  tipIcon: {
    color: '#7EB6FF',
    fontSize: 14,
    lineHeight: 20,
  },
  tipText: {
    flex: 1,
    color: 'rgba(190, 210, 235, 0.88)',
    fontSize: 13,
    lineHeight: 20,
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
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(140, 180, 230, 0.28)',
    backgroundColor: 'rgba(10, 28, 58, 0.72)',
    color: colors.text,
    paddingHorizontal: 14,
    paddingVertical: 14,
    fontSize: 15,
  },
  action: {
    marginTop: 10,
  },
});
