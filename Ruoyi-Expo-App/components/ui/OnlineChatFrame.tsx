import { createElement, useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  StyleSheet,
  View,
  type LayoutChangeEvent,
} from 'react-native';
import { WebView } from 'react-native-webview';

import { Text } from '@/components/ui/AppText';

type Props = {
  url: string;
};

const MAX_RECONNECT = 5;
const RETRY_DELAY_MS = 1200;

const FIT_CHAT_VIEWPORT = `
(function () {
  function apply(h) {
    var px = Math.round(h || window.innerHeight);
    if (!px) return;
    var css = px + 'px';
    var root = document.documentElement;
    var body = document.body;
    root.style.setProperty('height', css, 'important');
    root.style.setProperty('min-height', css, 'important');
    root.style.setProperty('max-height', css, 'important');
    root.style.overflow = 'hidden';
    if (body) {
      body.style.setProperty('height', css, 'important');
      body.style.setProperty('min-height', css, 'important');
      body.style.setProperty('max-height', css, 'important');
      body.style.overflow = 'hidden';
    }
    var tag = document.getElementById('xfzl-chat-vh');
    if (!tag) {
      tag = document.createElement('style');
      tag.id = 'xfzl-chat-vh';
      document.head.appendChild(tag);
    }
    tag.textContent = 'html,body{height:' + css + '!important;max-height:' + css + '!important;}';
  }
  window.__xfzlFitChat = apply;
  apply();
})();
true;
`;

export function OnlineChatFrame({ url }: Props) {
  const webRef = useRef<WebView>(null);
  const heightRef = useRef(0);
  const retryRef = useRef(0);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [attempt, setAttempt] = useState(0);
  const [retryCount, setRetryCount] = useState(0);
  const [giveUp, setGiveUp] = useState(false);

  const clearTimer = () => {
    if (timerRef.current != null) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  };

  useEffect(() => {
    retryRef.current = 0;
    setRetryCount(0);
    setGiveUp(false);
    setAttempt(0);
    return clearTimer;
  }, [url]);

  const applyFit = useCallback((height: number) => {
    if (height <= 0) {
      return;
    }
    webRef.current?.injectJavaScript(
      `window.__xfzlFitChat && window.__xfzlFitChat(${height}); true;`,
    );
  }, []);

  const fitToLayout = useCallback(
    (event: LayoutChangeEvent) => {
      const height = Math.round(event.nativeEvent.layout.height);
      heightRef.current = height;
      applyFit(height);
    },
    [applyFit],
  );

  const reconnect = useCallback(() => {
    clearTimer();
    retryRef.current = 0;
    setRetryCount(0);
    setGiveUp(false);
    setAttempt((value) => value + 1);
  }, []);

  const handleFail = useCallback(() => {
    if (timerRef.current != null) {
      return;
    }
    if (retryRef.current >= MAX_RECONNECT) {
      setGiveUp(true);
      return;
    }
    const next = retryRef.current + 1;
    retryRef.current = next;
    setRetryCount(next);
    setGiveUp(false);
    timerRef.current = setTimeout(() => {
      timerRef.current = null;
      setAttempt((value) => value + 1);
    }, RETRY_DELAY_MS);
  }, []);

  const status = giveUp ? (
    <View style={styles.status}>
      <Text style={styles.title}>客服暂时无法连接</Text>
      <Text style={styles.desc}>请检查网络后重试</Text>
      <Pressable onPress={reconnect} style={styles.button}>
        <Text style={styles.buttonText}>重新连接</Text>
      </Pressable>
    </View>
  ) : (
    <View style={styles.status}>
      <ActivityIndicator color="#3A78F0" />
      <Text style={styles.desc}>
        {retryCount > 0 ? `正在重新连接（${retryCount}/${MAX_RECONNECT}）` : '正在连接客服'}
      </Text>
    </View>
  );

  if (Platform.OS === 'web') {
    return (
      <View style={styles.fill}>
        {createElement('iframe', {
          key: `${url}-${attempt}`,
          src: url,
          style: {
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            width: '100%',
            height: '100%',
            border: 'none',
          },
          allow: 'microphone; camera; clipboard-write; clipboard-read; autoplay',
          referrerPolicy: 'no-referrer-when-downgrade',
          onError: handleFail,
        })}
      </View>
    );
  }

  return (
    <WebView
      key={`${url}-${attempt}`}
      ref={webRef}
      source={{ uri: url }}
      style={styles.fill}
      javaScriptEnabled
      domStorageEnabled
      startInLoadingState
      allowsInlineMediaPlayback
      setSupportMultipleWindows={false}
      nestedScrollEnabled
      hideKeyboardAccessoryView
      injectedJavaScript={FIT_CHAT_VIEWPORT}
      onLayout={fitToLayout}
      onLoad={() => {
        clearTimer();
        retryRef.current = 0;
        setRetryCount(0);
        setGiveUp(false);
      }}
      onLoadEnd={() => applyFit(heightRef.current)}
      renderLoading={() => status}
      renderError={() => status}
      onError={handleFail}
      onHttpError={(event) => {
        if (event.nativeEvent.statusCode >= 500) {
          handleFail();
        }
      }}
    />
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    position: 'relative',
    backgroundColor: '#F3F5F8',
  },
  status: {
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 28,
    backgroundColor: '#F3F5F8',
    gap: 12,
  },
  title: {
    color: '#1F2A37',
    fontSize: 16,
    fontWeight: '600',
  },
  desc: {
    color: '#5A6A80',
    fontSize: 14,
    textAlign: 'center',
  },
  button: {
    marginTop: 8,
    minWidth: 120,
    height: 40,
    paddingHorizontal: 18,
    borderRadius: 20,
    backgroundColor: '#3A78F0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '600',
  },
});
