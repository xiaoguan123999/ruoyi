import { Asset } from 'expo-asset';
import * as FileSystem from 'expo-file-system/legacy';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Platform, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { prefetchPdf, readPdfAsBase64 } from '@/utils/pdf-cache';
import { resolvePdfFetchUrl } from '@/utils/pdf-url';

import pdfMainAsset from '../../assets/pdf/pdf.min.bin';
import pdfWorkerAsset from '../../assets/pdf/pdf.worker.min.bin';

const RENDER_TIMEOUT_MS = 60_000;
const INJECT_CHUNK = 80_000;

let pdfJsAssets: { pdfJs: string; workerJs: string } | null = null;

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
(function () {
  function post(type) {
    if (window.ReactNativeWebView) {
      window.ReactNativeWebView.postMessage(type);
    }
  }
  function b64ToBytes(b64) {
    var raw = atob(b64);
    var bytes = new Uint8Array(raw.length);
    for (var i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);
    return bytes;
  }
  window.__startPdf = async function () {
    var msg = document.getElementById('msg');
    var root = document.getElementById('root');
    try {
      var mainUrl = URL.createObjectURL(new Blob([window.__PDF_JS], { type: 'text/javascript' }));
      var workerUrl = URL.createObjectURL(new Blob([window.__PDF_WORKER], { type: 'text/javascript' }));
      var pdfjs = await import(mainUrl);
      pdfjs.GlobalWorkerOptions.workerSrc = workerUrl;
      var pdf = await pdfjs.getDocument({
        data: b64ToBytes(window.__PDF_B64),
        disableStream: true,
        disableAutoFetch: true,
        isEvalSupported: false
      }).promise;
      if (msg) msg.remove();
      for (var p = 1; p <= pdf.numPages; p++) {
        var page = await pdf.getPage(p);
        var unscaled = page.getViewport({ scale: 1 });
        var scale = Math.max(1, window.innerWidth / unscaled.width);
        var viewport = page.getViewport({ scale: scale });
        var canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        await page.render({ canvasContext: canvas.getContext('2d'), viewport: viewport }).promise;
        root.appendChild(canvas);
        if (p === 1) {
          post('page');
        }
      }
      post('ok');
    } catch (e) {
      if (msg) msg.textContent = '预览失败';
      post('error');
    }
  };
  post('ready');
})();
</script>
</body>
</html>`;

function viewerDir() {
  return `${FileSystem.cacheDirectory ?? ''}pdf-viewer/`;
}

async function copyAssetTo(mod: number, dest: string) {
  const asset = Asset.fromModule(mod);
  await asset.downloadAsync();
  const from = asset.localUri;
  if (!from) {
    throw new Error('PDF 组件加载失败');
  }
  const info = await FileSystem.getInfoAsync(dest);
  if (info.exists && 'size' in info && typeof info.size === 'number' && info.size > 0) {
    return;
  }
  await FileSystem.copyAsync({ from, to: dest });
}

async function loadPdfJsAssets() {
  if (pdfJsAssets) {
    return pdfJsAssets;
  }
  const dir = viewerDir();
  if (!dir) {
    throw new Error('无法创建预览目录');
  }
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  const mainPath = `${dir}pdf.min.js`;
  const workerPath = `${dir}pdf.worker.min.js`;
  await Promise.all([copyAssetTo(pdfMainAsset, mainPath), copyAssetTo(pdfWorkerAsset, workerPath)]);
  const [pdfJs, workerJs] = await Promise.all([
    FileSystem.readAsStringAsync(mainPath),
    FileSystem.readAsStringAsync(workerPath),
  ]);
  if (!pdfJs || !workerJs) {
    throw new Error('PDF 组件加载失败');
  }
  pdfJsAssets = { pdfJs, workerJs };
  return pdfJsAssets;
}

function injectScript(web: WebView, script: string) {
  web.injectJavaScript(`${script}; true;`);
}

async function injectString(web: WebView, name: string, value: string) {
  injectScript(web, `window.${name}=""`);
  for (let i = 0; i < value.length; i += INJECT_CHUNK) {
    injectScript(web, `window.${name}+=${JSON.stringify(value.slice(i, i + INJECT_CHUNK))}`);
    await new Promise((resolve) => setTimeout(resolve, 8));
  }
}

type Props = {
  uri: string;
};

export function NativePdfPreview({ uri }: Props) {
  const webRef = useRef<WebView>(null);
  const startedRef = useRef(false);
  const [ready, setReady] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUrl = resolvePdfFetchUrl(uri);

  useEffect(() => {
    startedRef.current = false;
    setReady(false);
    setLoading(true);
    setError('');
    void prefetchPdf(fetchUrl);
  }, [fetchUrl]);

  useEffect(() => {
    if (!ready || startedRef.current) {
      return;
    }
    const web = webRef.current;
    if (!web) {
      return;
    }
    startedRef.current = true;
    let cancelled = false;
    const run = async () => {
      try {
        const [{ pdfJs, workerJs }, pdfPath] = await Promise.all([loadPdfJsAssets(), prefetchPdf(fetchUrl)]);
        if (cancelled) {
          return;
        }
        if (!pdfPath) {
          setError('PDF 下载失败');
          setLoading(false);
          return;
        }
        const pdfB64 = await readPdfAsBase64(pdfPath);
        await injectString(web, '__PDF_JS', pdfJs);
        await injectString(web, '__PDF_WORKER', workerJs);
        await injectString(web, '__PDF_B64', pdfB64);
        injectScript(web, 'window.__startPdf()');
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
  }, [ready, fetchUrl]);

  useEffect(() => {
    if (!loading) {
      return;
    }
    const timer = setTimeout(() => {
      setError((prev) => prev || '预览超时，请使用系统阅读器打开');
      setLoading(false);
    }, RENDER_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [loading, fetchUrl]);

  const onMessage = (type: string) => {
    if (type === 'ready') {
      setReady(true);
      return;
    }
    if (type === 'page' || type === 'ok') {
      setLoading(false);
      setError('');
      return;
    }
    if (type === 'error') {
      setLoading(false);
      setError('预览失败');
    }
  };

  if (error && !ready) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  return (
    <View style={styles.fill}>
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
      {loading ? (
        <View style={styles.loadingOverlay} pointerEvents="none">
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.progress}>加载中…</Text>
        </View>
      ) : null}
      {error ? (
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
