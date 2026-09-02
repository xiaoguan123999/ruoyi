import { createElement, useCallback, useRef } from 'react';
import { Platform, StyleSheet, View, type LayoutChangeEvent } from 'react-native';
import { WebView } from 'react-native-webview';

type Props = {
  url: string;
};

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

  if (Platform.OS === 'web') {
    return (
      <View style={styles.fill}>
        {createElement('iframe', {
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
        })}
      </View>
    );
  }

  return (
    <WebView
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
      onLoadEnd={() => applyFit(heightRef.current)}
    />
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    position: 'relative',
  },
});
