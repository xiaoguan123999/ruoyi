import { useEffect, useRef, useState } from 'react';
import {
  Animated,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { formatKycRewardLabel } from '@/api/app-member';
import type { KycRewardCurrency } from '@/api/types';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';

const USE_NATIVE_DRIVER = Platform.OS !== 'web';

type Props = {
  visible: boolean;
  submitting?: boolean;
  rewardCny: number;
  rewardUsdt: number;
  onClose: () => void;
  onConfirm: (currency: KycRewardCurrency) => void;
};

export function KycRewardModal({
  visible,
  submitting,
  rewardCny,
  rewardUsdt,
  onClose,
  onConfirm,
}: Props) {
  const [mounted, setMounted] = useState(visible);
  const [selected, setSelected] = useState<KycRewardCurrency | null>(null);
  const backdrop = useRef(new Animated.Value(0)).current;
  const scale = useRef(new Animated.Value(0.92)).current;

  useEffect(() => {
    if (visible) {
      setMounted(true);
      setSelected(null);
      backdrop.setValue(0);
      scale.setValue(0.92);
      Animated.parallel([
        Animated.timing(backdrop, {
          toValue: 1,
          duration: 220,
          useNativeDriver: USE_NATIVE_DRIVER,
        }),
        Animated.spring(scale, {
          toValue: 1,
          useNativeDriver: USE_NATIVE_DRIVER,
          friction: 8,
          tension: 80,
        }),
      ]).start();
      return;
    }
    if (!mounted) {
      return;
    }
    Animated.parallel([
      Animated.timing(backdrop, {
        toValue: 0,
        duration: 160,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
      Animated.timing(scale, {
        toValue: 0.94,
        duration: 160,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    ]).start(({ finished }) => {
      if (finished) {
        setMounted(false);
      }
    });
  }, [visible, backdrop, scale, mounted]);

  if (!mounted) {
    return null;
  }

  return (
    <Modal transparent visible={mounted} animationType="none" onRequestClose={onClose}>
      <View style={styles.root}>
        <Animated.View style={[styles.backdrop, { opacity: backdrop }]} pointerEvents="none" />
        <Animated.View style={[styles.card, { transform: [{ scale }] }]}>
          <Pressable
            onPress={onClose}
            hitSlop={12}
            style={styles.closeBtn}
            accessibilityRole="button"
            accessibilityLabel="关闭"
          >
            <Text style={styles.closeText}>×</Text>
          </Pressable>
          <Text style={styles.title}>领取实名注册奖励</Text>
          <Text style={styles.hint}>实名认证成功，请选择奖励币种（每人仅可领取一次）</Text>

          <View style={styles.options}>
            <Option
              label={formatKycRewardLabel('CNY', rewardCny)}
              sub="人民币到账"
              selected={selected === 'CNY'}
              onPress={() => setSelected('CNY')}
            />
            <Option
              label={formatKycRewardLabel('USDT', rewardUsdt)}
              sub="USDT 到账"
              selected={selected === 'USDT'}
              onPress={() => setSelected('USDT')}
            />
          </View>

          <PrimaryButton
            title={submitting ? '领取中…' : '确认领取'}
            disabled={!selected || submitting}
            onPress={() => {
              if (selected) {
                onConfirm(selected);
              }
            }}
          />
        </Animated.View>
      </View>
    </Modal>
  );
}

function Option({
  label,
  sub,
  selected,
  onPress,
}: {
  label: string;
  sub: string;
  selected: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={[styles.option, selected && styles.optionSelected]}
    >
      <Text style={[styles.optionLabel, selected && styles.optionLabelSelected]}>{label}</Text>
      <Text style={styles.optionSub}>{sub}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 8, 20, 0.72)',
  },
  card: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(120, 160, 210, 0.35)',
    backgroundColor: 'rgba(12, 24, 44, 0.96)',
    paddingHorizontal: 18,
    paddingTop: 22,
    paddingBottom: 18,
  },
  closeBtn: {
    position: 'absolute',
    top: 8,
    right: 10,
    width: 32,
    height: 32,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 2,
  },
  closeText: {
    color: 'rgba(200, 215, 235, 0.85)',
    fontSize: 26,
    lineHeight: 28,
    fontWeight: '300',
  },
  title: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
    textAlign: 'center',
  },
  hint: {
    marginTop: 8,
    marginBottom: 16,
    color: 'rgba(180, 198, 220, 0.85)',
    fontSize: 13,
    lineHeight: 20,
    textAlign: 'center',
  },
  options: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 18,
  },
  option: {
    flex: 1,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(120, 160, 210, 0.28)',
    backgroundColor: 'rgba(20, 40, 70, 0.55)',
    paddingVertical: 16,
    paddingHorizontal: 10,
    alignItems: 'center',
  },
  optionSelected: {
    borderColor: '#2F7BFF',
    backgroundColor: 'rgba(47, 123, 255, 0.18)',
  },
  optionLabel: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '700',
  },
  optionLabelSelected: {
    color: '#7EB6FF',
  },
  optionSub: {
    marginTop: 6,
    color: colors.muted,
    fontSize: 12,
  },
});
