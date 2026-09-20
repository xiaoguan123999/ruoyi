import { createElement, useEffect, useRef, useState } from 'react';
import { Animated, Linking, Modal, Platform, Pressable, StyleSheet, useWindowDimensions, View } from 'react-native';
import { WebView, type WebViewNavigation } from 'react-native-webview';

import { fetchAppPayOrder, isPayOrderPaid, isPayOrderPending } from '@/api/app-pay';
import { Text } from '@/components/ui/AppText';
import { isPayReturnUrl } from '@/utils/pay-session';

const USE_NATIVE_DRIVER = Platform.OS !== 'web';

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function isHttpUrl(url: string) {
  return /^https?:/i.test(url);
}

function openBrowserPopup(payUrl: string) {
  if (typeof window === 'undefined') {
    return null;
  }
  const width = Math.min(440, Math.round(window.screen.availWidth * 0.92));
  const height = Math.min(720, Math.round(window.screen.availHeight * 0.9));
  const left = Math.max(0, Math.round((window.screen.availWidth - width) / 2 + (window.screenLeft || window.screenX || 0)));
  const top = Math.max(0, Math.round((window.screen.availHeight - height) / 2 + (window.screenTop || window.screenY || 0)));
  return window.open(
    payUrl,
    'pay_cashier',
    `popup=yes,width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`,
  );
}

export function PayCashierPopup({
  visible,
  payUrl,
  outTradeNo,
  onClose,
  onSettled,
}: {
  visible: boolean;
  payUrl?: string;
  outTradeNo?: string;
  onClose: () => void;
  onSettled: () => void;
}) {
  const { height, width } = useWindowDimensions();
  const [mounted, setMounted] = useState(visible);
  const [useOverlay, setUseOverlay] = useState(Platform.OS !== 'web');
  const popupRef = useRef<Window | null>(null);
  const closed = useRef(false);
  const onCloseRef = useRef(onClose);
  const onSettledRef = useRef(onSettled);
  const backdrop = useRef(new Animated.Value(0)).current;
  const scale = useRef(new Animated.Value(0.94)).current;
  onCloseRef.current = onClose;
  onSettledRef.current = onSettled;

  useEffect(() => {
    closed.current = false;
    if (!visible || !payUrl) {
      popupRef.current = null;
      return;
    }
    if (Platform.OS !== 'web') {
      setUseOverlay(true);
      return;
    }
    const popup = openBrowserPopup(payUrl);
    popupRef.current = popup;
    if (!popup) {
      setUseOverlay(true);
      return;
    }
    setUseOverlay(false);
    const timer = window.setInterval(() => {
      if (popup.closed) {
        window.clearInterval(timer);
        if (!closed.current) {
          onCloseRef.current();
        }
      }
    }, 400);
    return () => {
      window.clearInterval(timer);
    };
  }, [payUrl, visible]);

  useEffect(() => {
    if (!visible || !outTradeNo) {
      return;
    }
    let stop = false;
    void (async () => {
      // 约 3 分钟内确认到账；间隔略拉长，减轻查单压力
      for (let i = 0; i < 36; i += 1) {
        await sleep(i === 0 ? 2500 : 5000);
        if (stop || closed.current) {
          return;
        }
        try {
          const order = await fetchAppPayOrder(outTradeNo);
          if (isPayOrderPaid(order.status) || !isPayOrderPending(order.status)) {
            closed.current = true;
            popupRef.current?.close();
            onSettledRef.current();
            return;
          }
        } catch {
          return;
        }
      }
    })();
    return () => {
      stop = true;
    };
  }, [outTradeNo, visible]);

  useEffect(() => {
    if (visible && useOverlay) {
      setMounted(true);
      backdrop.setValue(0);
      scale.setValue(0.94);
      Animated.parallel([
        Animated.timing(backdrop, {
          toValue: 1,
          duration: 200,
          useNativeDriver: USE_NATIVE_DRIVER,
        }),
        Animated.spring(scale, {
          toValue: 1,
          useNativeDriver: USE_NATIVE_DRIVER,
          friction: 9,
          tension: 70,
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
        toValue: 0.96,
        duration: 160,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    ]).start(({ finished }) => {
      if (finished) {
        setMounted(false);
      }
    });
  }, [backdrop, mounted, scale, useOverlay, visible]);

  const dismiss = () => {
    closed.current = true;
    popupRef.current?.close();
    onClose();
  };

  if (!visible || !payUrl || !useOverlay) {
    return null;
  }
  if (!mounted) {
    return null;
  }

  const boxWidth = Math.min(440, Math.round(width * 0.92));
  const boxHeight = Math.min(720, Math.round(height * 0.86));

  return (
    <Modal transparent visible animationType="none" onRequestClose={dismiss} statusBarTranslucent>
      <View style={styles.host}>
        <Animated.View style={[styles.mask, { opacity: backdrop }]}>
          <Pressable style={StyleSheet.absoluteFill} onPress={dismiss} />
        </Animated.View>
        <Animated.View style={[styles.box, { width: boxWidth, height: boxHeight, transform: [{ scale }] }]}>
          <View style={styles.head}>
            <Text style={styles.title}>订单</Text>
            <Pressable onPress={dismiss} hitSlop={12} style={styles.close}>
              <Text style={styles.closeText}>×</Text>
            </Pressable>
          </View>
          <View style={styles.body}>
            {Platform.OS === 'web' ? (
              createElement('iframe', {
                src: payUrl,
                style: { width: '100%', height: '100%', border: 'none', background: '#fff' },
                allow: 'payment; clipboard-write',
                referrerPolicy: 'no-referrer-when-downgrade',
              })
            ) : (
              <WebView
                source={{ uri: payUrl }}
                style={styles.web}
                javaScriptEnabled
                domStorageEnabled
                originWhitelist={['*']}
                setSupportMultipleWindows={false}
                onNavigationStateChange={(nav: WebViewNavigation) => {
                  if (isPayReturnUrl(nav.url)) {
                    dismiss();
                  }
                }}
                onShouldStartLoadWithRequest={(req) => {
                  const url = req.url;
                  if (!url) {
                    return true;
                  }
                  if (isPayReturnUrl(url)) {
                    dismiss();
                    return false;
                  }
                  if (isHttpUrl(url)) {
                    return true;
                  }
                  void Linking.openURL(url).catch(() => {});
                  return false;
                }}
              />
            )}
          </View>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  host: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  mask: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
  },
  box: {
    borderRadius: 16,
    overflow: 'hidden',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  head: {
    height: 48,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#eee',
  },
  title: {
    flex: 1,
    color: '#111',
    fontSize: 16,
    fontWeight: '700',
  },
  close: {
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeText: {
    color: '#666',
    fontSize: 24,
    lineHeight: 24,
    fontWeight: '300',
  },
  body: {
    flex: 1,
    backgroundColor: '#fff',
  },
  web: {
    flex: 1,
    backgroundColor: '#fff',
  },
});
