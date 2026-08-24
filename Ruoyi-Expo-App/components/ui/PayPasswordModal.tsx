import { useEffect, useRef, useState } from 'react';
import {
  Animated,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';

const USE_NATIVE_DRIVER = Platform.OS !== 'web';
const SHEET_OFFSET = 480;

export type PayPasswordMode = 'verify' | 'set';

type Props = {
  visible: boolean;
  mode?: PayPasswordMode;
  title?: string;
  submitting?: boolean;
  onCancel: () => void;
  onConfirm: (payPassword: string) => void;
};

export function PayPasswordModal({
  visible,
  mode = 'verify',
  title,
  submitting,
  onCancel,
  onConfirm,
}: Props) {
  const insets = useSafeAreaInsets();
  const [payPassword, setPayPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [mounted, setMounted] = useState(visible);
  const backdrop = useRef(new Animated.Value(0)).current;
  const sheetY = useRef(new Animated.Value(SHEET_OFFSET)).current;

  const isSet = mode === 'set';
  const resolvedTitle = title || (isSet ? '设置支付密码' : '请输入支付密码');
  const hint = isSet
    ? '检测到您尚未设置支付密码，请先设置后再认购'
    : '为保障资金安全，认购前需验证支付密码';
  const confirmLabel = isSet ? '确认设置' : '确认认购';
  const canSubmit =
    payPassword.trim().length >= 4 &&
    (!isSet || payPassword.trim() === confirmPassword.trim());

  useEffect(() => {
    if (visible) {
      setMounted(true);
      backdrop.setValue(0);
      sheetY.setValue(SHEET_OFFSET);
      Animated.parallel([
        Animated.timing(backdrop, {
          toValue: 1,
          duration: 260,
          useNativeDriver: USE_NATIVE_DRIVER,
        }),
        Animated.spring(sheetY, {
          toValue: 0,
          useNativeDriver: USE_NATIVE_DRIVER,
          friction: 9,
          tension: 68,
          velocity: 1,
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
        duration: 200,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
      Animated.timing(sheetY, {
        toValue: SHEET_OFFSET,
        duration: 220,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    ]).start(({ finished }) => {
      if (finished) {
        setMounted(false);
        setPayPassword('');
        setConfirmPassword('');
      }
    });
  }, [visible, mounted, backdrop, sheetY]);

  if (!mounted) {
    return null;
  }

  return (
    <Modal transparent visible animationType="none" onRequestClose={onCancel} statusBarTranslucent>
      <KeyboardAvoidingView
        style={styles.host}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <Animated.View style={[styles.mask, { opacity: backdrop }]}>
          <Pressable style={StyleSheet.absoluteFill} onPress={submitting ? undefined : onCancel} />
        </Animated.View>

        <Animated.View
          style={[
            styles.sheet,
            {
              paddingBottom: Math.max(insets.bottom, 0) + 20,
              transform: [{ translateY: sheetY }],
            },
          ]}
        >
          <View style={styles.handle} />

          <View style={styles.head}>
            <Text style={styles.title}>{resolvedTitle}</Text>
            <Pressable
              onPress={submitting ? undefined : onCancel}
              hitSlop={12}
              style={styles.closeBtn}
            >
              <Text style={styles.closeText}>×</Text>
            </Pressable>
          </View>

          <Text style={styles.hint}>{hint}</Text>

          <TextInput
            value={payPassword}
            onChangeText={setPayPassword}
            placeholder={isSet ? '请设置支付密码' : '请输入支付密码'}
            placeholderTextColor={colors.placeholder}
            secureTextEntry
            style={styles.input}
            autoFocus
            editable={!submitting}
            returnKeyType={isSet ? 'next' : 'done'}
            onSubmitEditing={() => {
              if (!isSet && canSubmit) {
                onConfirm(payPassword.trim());
              }
            }}
          />

          {isSet ? (
            <TextInput
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              placeholder="请再次输入支付密码"
              placeholderTextColor={colors.placeholder}
              secureTextEntry
              style={[styles.input, styles.inputGap]}
              editable={!submitting}
              returnKeyType="done"
              onSubmitEditing={() => {
                if (canSubmit) {
                  onConfirm(payPassword.trim());
                }
              }}
            />
          ) : null}

          <View style={styles.actions}>
            <Pressable
              onPress={onCancel}
              disabled={submitting}
              style={[styles.cancelBtn, submitting && styles.disabled]}
            >
              <Text style={styles.cancelText}>取消</Text>
            </Pressable>
            <View style={styles.confirmWrap}>
              <PrimaryButton
                title={confirmLabel}
                compact
                disabled={submitting || !canSubmit}
                onPress={() => onConfirm(payPassword.trim())}
              />
            </View>
          </View>
        </Animated.View>
      </KeyboardAvoidingView>
    </Modal>
  );
}

const styles = StyleSheet.create({
  host: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  mask: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
  },
  sheet: {
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    borderWidth: 1,
    borderBottomWidth: 0,
    borderColor: 'rgba(110, 185, 255, 0.35)',
    backgroundColor: 'rgba(12, 28, 58, 0.98)',
    paddingHorizontal: 18,
    paddingTop: 10,
  },
  handle: {
    alignSelf: 'center',
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: 'rgba(180, 205, 235, 0.4)',
    marginBottom: 12,
  },
  head: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  title: {
    flex: 1,
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
  },
  closeBtn: {
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeText: {
    color: colors.text,
    fontSize: 24,
    lineHeight: 24,
    fontWeight: '300',
  },
  hint: {
    color: 'rgba(190, 210, 235, 0.8)',
    fontSize: 13,
    lineHeight: 20,
    marginBottom: 14,
  },
  input: {
    height: 48,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(120, 160, 210, 0.28)',
    backgroundColor: 'rgba(8, 22, 48, 0.72)',
    color: colors.text,
    paddingHorizontal: 14,
    fontSize: 16,
    letterSpacing: 2,
  },
  inputGap: {
    marginTop: 12,
  },
  actions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginTop: 18,
  },
  cancelBtn: {
    height: 46,
    minWidth: 88,
    paddingHorizontal: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(140, 180, 230, 0.35)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelText: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
  },
  confirmWrap: {
    flex: 1,
  },
  disabled: {
    opacity: 0.5,
  },
});
