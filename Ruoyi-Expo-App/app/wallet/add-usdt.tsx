import { Image } from 'expo-image';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { createAppPayAccount, fetchAppPayAccounts } from '@/api/app-pay-account';
import { ApiError } from '@/api/request';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

const PROTOCOLS = ['TRC20', 'ERC20'] as const;

export default function AddUsdtWalletScreen() {
  const router = useRouter();
  const [protocol, setProtocol] = useState<(typeof PROTOCOLS)[number]>('TRC20');
  const [address, setAddress] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async () => {
    if (!address.trim()) {
      modalWarning('请输入虚拟币地址');
      return;
    }
    setSubmitting(true);
    try {
      const existing = await fetchAppPayAccounts('USDT');
      if (existing.length >= 1) {
        modalWarning('最多添加1个虚拟账户');
        return;
      }
      const msg = await createAppPayAccount({
        accountType: 'USDT',
        accountNo: address.trim(),
        network: protocol,
      });
      modalSuccess(msg);
      router.replace('/wallet?tab=usdt');
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
      <PageHeader title="添加虚拟币账户" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        onRefresh={async () => {}}
      >
        <Text style={styles.label}>虚拟币种类</Text>
        <View style={styles.row}>
          <SelectChip selected>
            <Image source={images.payUsdt} style={styles.usdtIcon} contentFit="contain" />
            <Text style={[styles.chipText, styles.chipTextActive]}>USDT</Text>
          </SelectChip>
        </View>

        <Text style={[styles.label, styles.labelGap]}>虚拟币协议</Text>
        <View style={styles.row}>
          {PROTOCOLS.map((item) => (
            <SelectChip key={item} selected={protocol === item} onPress={() => setProtocol(item)}>
              <Text style={[styles.chipText, protocol === item && styles.chipTextActive]}>{item}</Text>
            </SelectChip>
          ))}
        </View>

        <Text style={[styles.label, styles.labelGap]}>银行卡号</Text>
        <TextInput
          value={address}
          onChangeText={setAddress}
          placeholder="请输入虚拟币地址"
          placeholderTextColor={colors.placeholder}
          style={styles.input}
          autoCapitalize="none"
          autoCorrect={false}
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

function SelectChip({
  selected,
  onPress,
  children,
}: {
  selected?: boolean;
  onPress?: () => void;
  children: React.ReactNode;
}) {
  return (
    <Pressable onPress={onPress} style={[styles.chip, selected && styles.chipSelected]}>
      {children}
      {selected ? (
        <>
          <View style={styles.check} />
          <Text style={styles.checkMark}>✓</Text>
        </>
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
    paddingTop: 8,
  },
  label: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
    marginBottom: 12,
  },
  labelGap: {
    marginTop: 22,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
  },
  chip: {
    minWidth: 118,
    height: 52,
    paddingHorizontal: 18,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(180, 200, 230, 0.32)',
    backgroundColor: 'rgba(8, 18, 40, 0.55)',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    overflow: 'hidden',
  },
  chipSelected: {
    borderColor: '#3D8BFF',
    backgroundColor: 'rgba(20, 48, 100, 0.45)',
  },
  chipText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },
  chipTextActive: {
    color: '#5BA3FF',
  },
  usdtIcon: {
    width: 24,
    height: 24,
  },
  check: {
    position: 'absolute',
    right: 0,
    bottom: 0,
    width: 0,
    height: 0,
    borderStyle: 'solid',
    borderLeftWidth: 18,
    borderBottomWidth: 18,
    borderLeftColor: 'transparent',
    borderBottomColor: '#3D8BFF',
  },
  checkMark: {
    position: 'absolute',
    right: 1,
    bottom: 0,
    color: '#fff',
    fontSize: 9,
    fontWeight: '700',
    lineHeight: 12,
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
    marginTop: 32,
  },
});
