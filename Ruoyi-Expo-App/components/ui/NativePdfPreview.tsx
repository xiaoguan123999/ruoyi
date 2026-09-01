import { Asset } from 'expo-asset';
import * as FileSystem from 'expo-file-system/legacy';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Platform, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { getCachedPdfPath, prefetchPdf, readPdfAsBase64 } from '@/utils/pdf-cache';
import { resolvePdfFetchUrl } from '@/utils/pdf-url';

import pdfMainAsset from '../../assets/pdf/pdf.min.bin';
import pdfWorkerAsset from '../../assets/pdf/pdf.worker.min.bin';

const RENDER_TIMEOUT_MS = 60_000;
const PDFJS_CACHE_VER = '4.10.38-3';
const INJECT_CHUNK = 80_000;

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
<script src="pdf.min.js"></script>
<script src="worker-data.js"></script>
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
  try {
    if (typeof pdfjsLib === 'undefined' || !window.__PDF_WORKER) {
      throw new Error('pdfjs');
    }
    pdfjsLib.GlobalWorkerOptions.workerSrc = URL.createObjectURL(
      new Blob([window.__PDF_WORKER], { type: 'text/javascript' })
    );
  } catch (e) {
    post('error');
    return;
  }

  var rendering = false;
  window.renderFromBase64 = function (b64) {
    if (rendering) {
      return;
    }
    rendering = true;
    var msg = document.getElementById('msg');
    var root = document.getElementById('root');
    (async function () {
      try {
        var pdf = await pdfjsLib.getDocument({
          data: b64ToBytes(b64),
          disableStream: true,
          disableAutoFetch: true,
          isEvalSupported: false
        }).promise;
        if (msg) msg.style.display = 'none';
        root.innerHTML = '';
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
        rendering = false;
        if (msg) {
          msg.style.display = 'block';
          msg.textContent = '预览失败';
        }
        post('error');
      }
    })();
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

async function ensureViewer() {
  const dir = viewerDir();
  if (!dir) {
    throw new Error('无法创建预览目录');
  }
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  const verPath = `${dir}ver.txt`;
  const htmlPath = `${dir}index.html`;
  let cachedVer = '';
  try {
    cachedVer = await FileSystem.readAsStringAsync(verPath);
  } catch {
    cachedVer = '';
  }
  if (cachedVer === PDFJS_CACHE_VER) {
    const htmlInfo = await FileSystem.getInfoAsync(htmlPath);
    if (htmlInfo.exists) {
      return { dir, htmlPath };
    }
  }
  await FileSystem.deleteAsync(dir, { idempotent: true });
  await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  const mainPath = `${dir}pdf.min.js`;
  const workerPath = `${dir}pdf.worker.min.js`;
  await Promise.all([copyAssetTo(pdfMainAsset, mainPath), copyAssetTo(pdfWorkerAsset, workerPath)]);
  const workerJs = await FileSystem.readAsStringAsync(workerPath);
  if (!workerJs) {
    throw new Error('PDF 组件加载失败');
  }
  await FileSystem.writeAsStringAsync(`${dir}worker-data.js`, `window.__PDF_WORKER = ${JSON.stringify(workerJs)};`);
  await FileSystem.writeAsStringAsync(htmlPath, VIEWER_HTML);
  await FileSystem.writeAsStringAsync(verPath, PDFJS_CACHE_VER);
  return { dir, htmlPath };
}

function injectScript(web: WebView, script: string) {
  web.injectJavaScript(`${script}; true;`);
}

async function injectBase64(web: WebView, b64: string) {
  injectScript(web, 'window.__pdfB64=""');
  for (let i = 0; i < b64.length; i += INJECT_CHUNK) {
    injectScript(web, `window.__pdfB64+=${JSON.stringify(b64.slice(i, i + INJECT_CHUNK))}`);
    await new Promise((resolve) => setTimeout(resolve, 8));
  }
  injectScript(web, 'window.renderFromBase64(window.__pdfB64)');
}

type Props = {
  uri: string;
};

export function NativePdfPreview({ uri }: Props) {
  const webRef = useRef<WebView>(null);
  const startedRef = useRef(false);
  const [pageUri, setPageUri] = useState('');
  const [readAccessUrl, setReadAccessUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUrl = resolvePdfFetchUrl(uri);

  useEffect(() => {
    let cancelled = false;
    startedRef.current = false;
    const run = async () => {
      setLoading(true);
      setError('');
      setPageUri('');
      void prefetchPdf(fetchUrl);
      try {
        const { dir, htmlPath } = await ensureViewer();
        if (!cancelled) {
          setReadAccessUrl(dir);
          setPageUri(htmlPath);
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
  }, [fetchUrl]);

  useEffect(() => {
    if (!pageUri || !loading) {
      return;
    }
    const timer = setTimeout(() => {
      setError((prev) => prev || '预览超时，请使用系统阅读器打开');
      setLoading(false);
    }, RENDER_TIMEOUT_MS);
    return () => clearTimeout(timer);
  }, [pageUri, loading]);

  const onMessage = (type: string) => {
    const web = webRef.current;
    if (type === 'ready') {
      if (!web || startedRef.current) {
        return;
      }
      startedRef.current = true;
      void (async () => {
        const cached = (await getCachedPdfPath(fetchUrl)) || (await prefetchPdf(fetchUrl));
        if (!cached || !webRef.current) {
          setLoading(false);
          setError('PDF 下载失败');
          return;
        }
        await injectBase64(webRef.current, await readPdfAsBase64(cached));
      })();
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

  if (error && !pageUri) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  return (
    <View style={styles.fill}>
      {pageUri ? (
        <WebView
          ref={webRef}
          source={{ uri: pageUri }}
          style={styles.fill}
          originWhitelist={['*', 'file://*']}
          allowFileAccess
          allowFileAccessFromFileURLs
          allowUniversalAccessFromFileURLs
          allowingReadAccessToURL={readAccessUrl}
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
      {error && pageUri ? (
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
