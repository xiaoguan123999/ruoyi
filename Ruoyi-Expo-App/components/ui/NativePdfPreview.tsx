import * as WebBrowser from 'expo-web-browser';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Platform, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { resolvePdfFetchUrl } from '@/utils/pdf-url';

const FETCH_TIMEOUT_MS = 20_000;
const RENDER_TIMEOUT_MS = 25_000;

const VIEWER_HTML = `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=3"/>
<style>
  html,body{margin:0;background:#050B1C}
  canvas{display:block;width:100%;margin:0 0 8px}
  .msg{color:#8AA4C6;text-align:center;padding:40px 16px;font-family:sans-serif;font-size:14px}
</style>
</head>
<body>
<div id="msg" class="msg">加载中…</div>
<div id="root"></div>
<script>
  function loadScript(src) {
    return new Promise(function (resolve, reject) {
      var s = document.createElement('script');
      s.src = src;
      s.onload = resolve;
      s.onerror = reject;
      document.head.appendChild(s);
    });
  }
  function b64ToBytes(b64) {
    var raw = atob(b64);
    var bytes = new Uint8Array(raw.length);
    for (var i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);
    return bytes;
  }
  function post(type) {
    if (window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(type);
    }
  }
  window.renderPdf = async function (b64) {
    var msg = document.getElementById('msg');
    var root = document.getElementById('root');
    try {
      var mains = [
        'https://unpkg.com/pdfjs-dist@4.10.38/legacy/build/pdf.min.js',
        'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.10.38/legacy/build/pdf.min.js'
      ];
      var workers = [
        'https://unpkg.com/pdfjs-dist@4.10.38/legacy/build/pdf.worker.min.js',
        'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.10.38/legacy/build/pdf.worker.min.js'
      ];
      var loaded = false;
      for (var i = 0; i < mains.length; i++) {
        try {
          await loadScript(mains[i]);
          loaded = true;
          break;
        } catch (e) {}
      }
      if (!loaded || typeof pdfjsLib === 'undefined') {
        throw new Error('pdfjs');
      }
      pdfjsLib.GlobalWorkerOptions.workerSrc = workers[0];
      var pdf = await pdfjsLib.getDocument({ data: b64ToBytes(b64) }).promise;
      msg.remove();
      for (var p = 1; p <= pdf.numPages; p++) {
        var page = await pdf.getPage(p);
        var unscaled = page.getViewport({ scale: 1 });
        var scale = window.innerWidth / unscaled.width;
        var viewport = page.getViewport({ scale: scale });
        var canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        await page.render({ canvasContext: canvas.getContext('2d'), viewport: viewport }).promise;
        root.appendChild(canvas);
      }
      post('ok');
    } catch (e) {
      msg.textContent = '预览失败';
      post('error');
    }
  };
  post('ready');
</script>
</body>
</html>`;

function arrayBufferToBase64(data: ArrayBuffer) {
  const bytes = new Uint8Array(data);
  const chunkSize = 8192;
  let binary = '';
  for (let i = 0; i < bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunkSize));
  }
  return btoa(binary);
}

async function loadPdfBase64(uri: string) {
  const url = resolvePdfFetchUrl(uri);
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), FETCH_TIMEOUT_MS);
  try {
    const response = await fetch(url, { signal: controller.signal });
    if (!response.ok) {
      throw new Error(`PDF 下载失败 ${response.status}`);
    }
    const data = await response.arrayBuffer();
    if (data.byteLength === 0) {
      throw new Error('PDF 加载失败');
    }
    return arrayBufferToBase64(data);
  } catch (error) {
    if (error instanceof Error && error.name === 'AbortError') {
      throw new Error('PDF 下载超时，请稍后重试');
    }
    throw error;
  } finally {
    clearTimeout(timer);
  }
}

type Props = {
  uri: string;
};

export function NativePdfPreview({ uri }: Props) {
  const webRef = useRef<WebView>(null);
  const [pdfB64, setPdfB64] = useState('');
  const [ready, setReady] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const run = async () => {
      setLoading(true);
      setError('');
      setPdfB64('');
      setReady(false);
      try {
        const next = await loadPdfBase64(uri);
        if (!cancelled) {
          setPdfB64(next);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'PDF 加载失败');
          setLoading(false);
        }
      }
    };
    void run();
    return () => {
      cancelled = true;
    };
  }, [uri]);

  useEffect(() => {
    if (!ready || !pdfB64) {
      return;
    }
    const payload = JSON.stringify(pdfB64);
    webRef.current?.injectJavaScript(`window.renderPdf(${payload}); true;`);
    const timer = setTimeout(() => {
      setError((prev) => prev || '预览超时，请使用系统阅读器打开');
      setLoading(false);
    }, RENDER_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [ready, pdfB64]);

  const onMessage = (type: string) => {
    if (type === 'ready') {
      setReady(true);
      return;
    }
    if (type === 'ok') {
      setLoading(false);
      setError('');
      return;
    }
    if (type === 'error') {
      setLoading(false);
      setError('预览失败');
    }
  };

  if (error && !pdfB64) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  return (
    <View style={styles.fill}>
      {pdfB64 ? (
        <WebView
          ref={webRef}
          source={{ html: VIEWER_HTML, baseUrl: 'https://localhost/' }}
          style={styles.fill}
          originWhitelist={['*']}
          mixedContentMode="always"
          javaScriptEnabled
          domStorageEnabled
          nestedScrollEnabled
          scalesPageToFit={Platform.OS === 'android'}
          setSupportMultipleWindows={false}
          onMessage={(event) => onMessage(event.nativeEvent.data)}
          onError={() => {
            setLoading(false);
            setError('预览失败');
          }}
        />
      ) : null}
      {loading ? (
        <View style={styles.loadingOverlay} pointerEvents="none">
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.progress}>加载中…</Text>
        </View>
      ) : null}
      {error && pdfB64 ? (
        <View style={styles.fallback}>
          <Text style={styles.fallbackText}>{error}</Text>
          <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    backgroundColor: '#050B1C',
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    backgroundColor: '#050B1C',
  },
  progress: {
    color: colors.muted,
    fontSize: 13,
  },
  fallback: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    paddingHorizontal: 28,
    gap: 16,
    backgroundColor: '#050B1C',
  },
  fallbackText: {
    color: colors.muted,
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 22,
  },
});
