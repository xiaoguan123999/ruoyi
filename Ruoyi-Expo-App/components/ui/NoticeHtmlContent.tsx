import { createElement, useCallback, useMemo, useState } from 'react';
import { Platform, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { looksLikeHtml, noticeHtmlCss, sanitizeNoticeHtml, wrapNoticeHtmlDocument } from '@/utils/notice-html';

type Props = {
  html: string;
  textStyle?: object;
};

const HEIGHT_JS = `
(function () {
  function send() {
    var h = Math.max(
      document.body ? document.body.scrollHeight : 0,
      document.documentElement ? document.documentElement.scrollHeight : 0
    );
    if (window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(String(h));
    }
  }
  send();
  setTimeout(send, 80);
  setTimeout(send, 320);
})();
true;
`;

export function NoticeHtmlContent({ html, textStyle }: Props) {
  const source = sanitizeNoticeHtml(html);
  const [height, setHeight] = useState(80);
  const documentHtml = useMemo(() => wrapNoticeHtmlDocument(source), [source]);

  const onMessage = useCallback((event: { nativeEvent: { data: string } }) => {
    const next = Number.parseInt(event.nativeEvent.data, 10);
    if (Number.isFinite(next) && next > 0) {
      setHeight((current) => (Math.abs(current - next) > 2 ? next : current));
    }
  }, []);

  if (!source) {
    return <Text style={textStyle}>暂无内容</Text>;
  }

  if (!looksLikeHtml(source)) {
    return <Text style={textStyle}>{source}</Text>;
  }

  if (Platform.OS === 'web') {
    return (
      <View style={styles.webBox}>
        {createElement('div', {
          className: 'notice-html-body',
          style: {
            color: 'rgba(220, 232, 255, 0.92)',
            fontSize: 14,
            lineHeight: 1.75,
            wordBreak: 'break-word',
          },
          dangerouslySetInnerHTML: { __html: source },
        })}
        {createElement('style', {
          dangerouslySetInnerHTML: {
            __html: `.notice-html-body *{color:inherit !important;} ${noticeHtmlCss('.notice-html-body')}`,
          },
        })}
      </View>
    );
  }

  return (
    <WebView
      originWhitelist={['*']}
      source={{ html: documentHtml }}
      style={[styles.webView, { height }]}
      scrollEnabled={false}
      showsVerticalScrollIndicator={false}
      showsHorizontalScrollIndicator={false}
      javaScriptEnabled
      automaticallyAdjustContentInsets={false}
      injectedJavaScript={HEIGHT_JS}
      onMessage={onMessage}
    />
  );
}

const styles = StyleSheet.create({
  webBox: {
    width: '100%',
  },
  webView: {
    width: '100%',
    backgroundColor: 'transparent',
  },
});
