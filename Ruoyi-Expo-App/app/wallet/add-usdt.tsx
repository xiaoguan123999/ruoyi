import { Image } from 'expo-image';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalWarning } from '@/utils/toast';

const PROTOCOLS = ['TRC20', 'ERC20'] as const;

export default function AddUsdtWalletScreen() {
  const [protocol, setProtocol] = useState<(typeof PROTOCOLS)[number]>('TRC20');
  const [address, setAddress] = useState('');

  const onSubmit = () => {
    if (!address.trim()) {
      modalWarning('请输入虚拟币地址');
      return;
    }
    modalWarning('收款账户接口暂未对接');
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

        <Text style={[styles.label, styles.labelGap]}>虚拟币地址</Text>
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
          <PrimaryButton title="确认添加" onPress={onSubmit} />
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
    <Pressable
      onPress={onPress}
      style={[styles.chip, selected && styles.chipSelected]}
    >
      {children}
      {selected ? (
        <View style={styles.check}>
          <Text style={styles.checkMark}>✓</Text>
        </View>
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
    minWidth: 108,
    height: 48,
    paddingHorizontal: 16,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(180, 200, 230, 0.35)',
    backgroundColor: 'rgba(8, 20, 44, 0.45)',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    overflow: 'hidden',
  },
  chipSelected: {
    borderColor: '#4A9EFF',
    backgroundColor: 'rgba(30, 70, 140, 0.35)',
  },
  chipText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },
  chipTextActive: {
    color: '#7EB6FF',
  },
  usdtIcon: {
    width: 22,
    height: 22,
  },
  check: {
    position: 'absolute',
    right: 0,
    bottom: 0,
    width: 18,
    height: 14,
    borderTopLeftRadius: 6,
    backgroundColor: '#3D8BFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkMark: {
    color: '#fff',
    fontSize: 10,
    fontWeight: '700',
    lineHeight: 12,
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
    marginTop: 28,
  },
});
