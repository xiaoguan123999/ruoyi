import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Animated,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { colors } from '@/theme/colors';
import {
  notifyToastDismissed,
  setDismissToastHandler,
  setToastHandler,
  type ToastPayload,
  type ToastType,
} from '@/utils/toast';
import { usePathname } from 'expo-router';

const USE_NATIVE_DRIVER = Platform.OS !== 'web';

const TYPE_META: Record<
  ToastType,
  { label: string; accent: string; ring: string; glow: string; symbol: string }
> = {
  success: {
    label: '操作成功',
    accent: colors.success,
    ring: 'rgba(61, 220, 132, 0.45)',
    glow: 'rgba(61, 220, 132, 0.12)',
    symbol: '✓',
  },
  warning: {
    label: '温馨提示',
    accent: colors.gold,
    ring: 'rgba(232, 195, 106, 0.5)',
    glow: 'rgba(232, 195, 106, 0.14)',
    symbol: '!',
  },
  error: {
    label: '操作失败',
    accent: colors.danger,
    ring: 'rgba(255, 90, 90, 0.45)',
    glow: 'rgba(255, 90, 90, 0.12)',
    symbol: '×',
  },
  info: {
    label: '系统提示',
    accent: colors.accent,
    ring: 'rgba(61, 139, 255, 0.45)',
    glow: 'rgba(61, 139, 255, 0.12)',
    symbol: 'i',
  },
};

function ToastIcon({ type, compact }: { type: ToastType; compact?: boolean }) {
  const meta = TYPE_META[type];
  if (compact) {
    return (
      <View style={[styles.compactIcon, { backgroundColor: meta.glow, borderColor: meta.ring }]}>
        <Text style={[styles.compactIconText, { color: meta.accent }]}>{meta.symbol}</Text>
      </View>
    );
  }
  return (
    <View style={[styles.iconOuter, { backgroundColor: meta.glow, borderColor: meta.ring }]}>
      <View style={[styles.iconInner, { borderColor: meta.accent }]}>
        <Text style={[styles.iconText, { color: meta.accent }]}>{meta.symbol}</Text>
      </View>
    </View>
  );
}

function LightToast({
  payload,
  topInset,
  onDismiss,
}: {
  payload: ToastPayload;
  topInset: number;
  onDismiss: () => void;
}) {
  const meta = TYPE_META[payload.type];
  const opacity = useRef(new Animated.Value(0)).current;
  const translateY = useRef(new Animated.Value(-16)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(opacity, { toValue: 1, duration: 200, useNativeDriver: USE_NATIVE_DRIVER }),
      Animated.spring(translateY, {
        toValue: 0,
        useNativeDriver: USE_NATIVE_DRIVER,
        friction: 9,
        tension: 120,
      }),
    ]).start();
  }, [opacity, translateY]);

  const dismiss = useCallback(() => {
    Animated.parallel([
      Animated.timing(opacity, { toValue: 0, duration: 180, useNativeDriver: USE_NATIVE_DRIVER }),
      Animated.timing(translateY, { toValue: -12, duration: 180, useNativeDriver: USE_NATIVE_DRIVER }),
    ]).start(({ finished }) => {
      if (finished) {
        onDismiss();
      }
    });
  }, [onDismiss, opacity, translateY]);

  useEffect(() => {
    if (!payload.autoClose || payload.duration <= 0) {
      return;
    }
    const timer = setTimeout(dismiss, payload.duration);
    return () => clearTimeout(timer);
  }, [dismiss, payload.autoClose, payload.duration]);

  return (
    <View style={[styles.lightHost, { paddingTop: topInset + 8 }]} pointerEvents="box-none">
      <Animated.View
        style={[
          styles.lightBar,
          {
            opacity,
            transform: [{ translateY }],
            borderLeftColor: meta.accent,
          },
        ]}
      >
        <ToastIcon type={payload.type} compact />
        <Text style={styles.lightMessage} numberOfLines={3}>
          {payload.message}
        </Text>
      </Animated.View>
    </View>
  );
}

function ModalToast({
  payload,
  onDismiss,
}: {
  payload: ToastPayload;
  onDismiss: () => void;
}) {
  const meta = TYPE_META[payload.type];
  const opacity = useRef(new Animated.Value(0)).current;
  const scale = useRef(new Animated.Value(0.94)).current;
  const translateY = useRef(new Animated.Value(12)).current;

  useEffect(() => {
    opacity.setValue(0);
    scale.setValue(0.94);
    translateY.setValue(12);
    Animated.parallel([
      Animated.spring(scale, {
        toValue: 1,
        useNativeDriver: USE_NATIVE_DRIVER,
        friction: 8,
        tension: 140,
      }),
      Animated.timing(opacity, { toValue: 1, duration: 200, useNativeDriver: USE_NATIVE_DRIVER }),
      Animated.timing(translateY, {
        toValue: 0,
        duration: 220,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    ]).start();
  }, [opacity, scale, translateY]);

  const dismiss = useCallback(() => {
    Animated.parallel([
      Animated.timing(opacity, { toValue: 0, duration: 180, useNativeDriver: USE_NATIVE_DRIVER }),
      Animated.timing(scale, { toValue: 0.94, duration: 180, useNativeDriver: USE_NATIVE_DRIVER }),
      Animated.timing(translateY, { toValue: 8, duration: 180, useNativeDriver: USE_NATIVE_DRIVER }),
    ]).start(({ finished }) => {
      if (finished) {
        onDismiss();
      }
    });
  }, [onDismiss, opacity, scale, translateY]);

  useEffect(() => {
    if (!payload.autoClose || payload.duration <= 0) {
      return;
    }
    const timer = setTimeout(dismiss, payload.duration);
    return () => clearTimeout(timer);
  }, [dismiss, payload.autoClose, payload.duration]);

  return (
    <View style={styles.modalHost} pointerEvents="box-none">
      <Pressable style={styles.backdrop} onPress={dismiss} accessibilityLabel="关闭提示" />
      <Animated.View
        style={[
          styles.panel,
          {
            opacity,
            transform: [{ scale }, { translateY }],
          },
        ]}
      >
        <View style={[styles.accentBar, { backgroundColor: meta.accent }]} />
        <Pressable
          onPress={dismiss}
          hitSlop={8}
          style={({ pressed }) => [styles.closeBtn, pressed && styles.closeBtnPressed]}
          accessibilityLabel="关闭"
        >
          <Text style={styles.closeText}>×</Text>
        </Pressable>
        <ToastIcon type={payload.type} />
        <Text style={styles.title}>{meta.label}</Text>
        <Text style={[styles.message, !payload.showButton && styles.messageNoBtn]}>
          {payload.message}
        </Text>
        {payload.showButton ? (
          <Pressable
            onPress={dismiss}
            style={({ pressed }) => [styles.btn, pressed && styles.btnPressed]}
          >
            <Text style={styles.btnText}>{payload.buttonText}</Text>
          </Pressable>
        ) : null}
      </Animated.View>
    </View>
  );
}

export function AppToast() {
  const insets = useSafeAreaInsets();
  const [payload, setPayload] = useState<ToastPayload | null>(null);
  const payloadKeyRef = useRef(0);

  const finishDismiss = useCallback(() => {
    setPayload(null);
    notifyToastDismissed();
  }, []);

  const dismissImmediate = useCallback(() => {
    setPayload(null);
  }, []);

  const show = useCallback((next: ToastPayload) => {
    payloadKeyRef.current += 1;
    setPayload(next);
  }, []);

  useEffect(() => {
    setToastHandler(show);
    setDismissToastHandler(dismissImmediate);
    return () => {
      setToastHandler(null);
      setDismissToastHandler(null);
    };
  }, [show, dismissImmediate]);

  const pathname = usePathname();
  const dismissImmediateRef = useRef(dismissImmediate);
  dismissImmediateRef.current = dismissImmediate;

  useEffect(() => {
    dismissImmediateRef.current();
  }, [pathname]);

  if (!payload) {
    return null;
  }

  if (payload.presentation === 'modal') {
    return (
      <ModalToast
        key={payloadKeyRef.current}
        payload={payload}
        onDismiss={finishDismiss}
      />
    );
  }

  return (
    <LightToast
      key={payloadKeyRef.current}
      payload={payload}
      topInset={insets.top}
      onDismiss={finishDismiss}
    />
  );
}

const styles = StyleSheet.create({
  lightHost: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 10000,
    elevation: 10000,
    alignItems: 'center',
    paddingHorizontal: 16,
  },
  lightBar: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    maxWidth: 420,
    paddingVertical: 12,
    paddingHorizontal: 14,
    borderRadius: 10,
    borderLeftWidth: 3,
    backgroundColor: 'rgba(13, 28, 58, 0.94)',
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.22)',
    gap: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.25,
    shadowRadius: 8,
    elevation: 6,
  },
  compactIcon: {
    width: 28,
    height: 28,
    borderRadius: 14,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  compactIconText: {
    fontSize: 14,
    fontWeight: '800',
    lineHeight: 16,
  },
  lightMessage: {
    flex: 1,
    color: 'rgba(220, 232, 248, 0.95)',
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '500',
  },
  modalHost: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 10000,
    elevation: 10000,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  backdrop: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(2, 8, 20, 0.55)',
  },
  panel: {
    width: '100%',
    maxWidth: 320,
    borderRadius: 14,
    paddingHorizontal: 24,
    paddingTop: 28,
    paddingBottom: 22,
    alignItems: 'center',
    backgroundColor: 'rgba(13, 28, 58, 0.82)',
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.28)',
    overflow: 'hidden',
    zIndex: 1,
  },
  accentBar: {
    position: 'absolute',
    top: 0,
    left: 24,
    right: 24,
    height: 2,
    borderRadius: 1,
    opacity: 0.85,
  },
  closeBtn: {
    position: 'absolute',
    top: 10,
    right: 10,
    width: 28,
    height: 28,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  closeBtnPressed: {
    opacity: 0.75,
  },
  closeText: {
    color: 'rgba(200, 218, 240, 0.85)',
    fontSize: 20,
    lineHeight: 22,
    fontWeight: '300',
    marginTop: -1,
  },
  iconOuter: {
    width: 64,
    height: 64,
    borderRadius: 32,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  iconInner: {
    width: 44,
    height: 44,
    borderRadius: 22,
    borderWidth: 1.5,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.06)',
  },
  iconText: {
    fontSize: 22,
    fontWeight: '800',
    lineHeight: 26,
  },
  title: {
    color: colors.text,
    fontSize: 19,
    fontWeight: '800',
    marginTop: 16,
    letterSpacing: 0.5,
  },
  message: {
    color: 'rgba(200, 218, 240, 0.92)',
    fontSize: 14,
    lineHeight: 22,
    textAlign: 'center',
    marginTop: 10,
    marginBottom: 22,
    paddingHorizontal: 4,
  },
  messageNoBtn: {
    marginBottom: 4,
  },
  btn: {
    alignSelf: 'stretch',
    height: 46,
    borderRadius: 8,
    backgroundColor: '#2F7BFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnPressed: {
    opacity: 0.88,
  },
  btnText: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    letterSpacing: 4,
  },
});
