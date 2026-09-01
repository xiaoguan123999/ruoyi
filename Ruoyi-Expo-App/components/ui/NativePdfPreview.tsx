import * as FileSystem from 'expo-file-system/legacy';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Platform, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { resolvePdfFetchUrl } from '@/utils/pdf-url';

const PDFJS_DIR = `${FileSystem.cacheDirectory ?? ''}pdfjs-4.10.38/`;
const PDF_DOC_DIR = `${FileSystem.cacheDirectory ?? ''}pdf-preview/`;

const PDFJS_MAIN = [
  'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.10.38/legacy/build/pdf.min.js',
  'https://unpkg.com/pdfjs-dist@4.10.38/legacy/build/pdf.min.js',
];
const PDFJS_WORKER = [
  'https://cdn.jsdelivr.net/npm/pdfjs-dist@4.10.38/legacy/build/pdf.worker.min.js',
  'https://unpkg.com/pdfjs-dist@4.10.38/legacy/build/pdf.worker.min.js',
];

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
<script>
  (async function () {
    var msg = document.getElementById('msg');
    var root = document.getElementById('root');
    try {
      var workerRes = await fetch('pdf.worker.min.js');
      var workerBlob = await workerRes.blob();
      pdfjsLib.GlobalWorkerOptions.workerSrc = URL.createObjectURL(workerBlob);
      var pdf = await pdfjsLib.getDocument({ url: 'doc.pdf' }).promise;
      msg.remove();
      for (var i = 1; i <= pdf.numPages; i++) {
        var page = await pdf.getPage(i);
        var unscaled = page.getViewport({ scale: 1 });
        var scale = window.innerWidth / unscaled.width;
        var viewport = page.getViewport({ scale: scale });
        var canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        await page.render({ canvasContext: canvas.getContext('2d'), viewport: viewport }).promise;
        root.appendChild(canvas);
      }
    } catch (e) {
      msg.textContent = '预览失败';
    }
  })();
</script>
</body>
</html>`;

async function downloadFirst(urls: string[], dest: string) {
  let last: unknown;
  for (const url of urls) {
    try {
      const result = await FileSystem.downloadAsync(url, dest);
      if (result.status === 200) {
        return;
      }
      last = new Error(`下载失败 ${result.status}`);
    } catch (error) {
      last = error;
    }
  }
  throw last instanceof Error ? last : new Error('脚本下载失败');
}

async function ensureFile(path: string, urls: string[]) {
  const info = await FileSystem.getInfoAsync(path);
  if (info.exists && 'size' in info && (info.size ?? 0) > 0) {
    return;
  }
  await downloadFirst(urls, path);
}

function arrayBufferToBase64(data: ArrayBuffer) {
  const bytes = new Uint8Array(data);
  const chunkSize = 8192;
  let binary = '';
  for (let i = 0; i < bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunkSize));
  }
  return btoa(binary);
}

async function writePdf(uri: string, dest: string) {
  const url = resolvePdfFetchUrl(uri);
  try {
    const result = await FileSystem.downloadAsync(url, dest);
    if (result.status === 200) {
      return;
    }
  } catch {
    // 带鉴权或非直链时改用 fetch
  }
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`PDF 下载失败 ${response.status}`);
  }
  const data = await response.arrayBuffer();
  if (data.byteLength === 0) {
    throw new Error('PDF 加载失败');
  }
  await FileSystem.writeAsStringAsync(dest, arrayBufferToBase64(data), {
    encoding: FileSystem.EncodingType.Base64,
  });
}

function toFileUri(path: string) {
  return path.startsWith('file://') ? path : `file://${path}`;
}

type Props = {
  uri: string;
};

export function NativePdfPreview({ uri }: Props) {
  const [htmlUri, setHtmlUri] = useState('');
  const [readAccessUrl, setReadAccessUrl] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const run = async () => {
      setLoading(true);
      setError('');
      setHtmlUri('');
      try {
        if (!FileSystem.cacheDirectory) {
          throw new Error('无法写入缓存');
        }
        await FileSystem.makeDirectoryAsync(PDFJS_DIR, { intermediates: true });
        await FileSystem.makeDirectoryAsync(PDF_DOC_DIR, { intermediates: true });
        await Promise.all([
          ensureFile(`${PDFJS_DIR}pdf.min.js`, PDFJS_MAIN),
          ensureFile(`${PDFJS_DIR}pdf.worker.min.js`, PDFJS_WORKER),
        ]);
        await FileSystem.deleteAsync(`${PDF_DOC_DIR}pdf.min.js`, { idempotent: true });
        await FileSystem.deleteAsync(`${PDF_DOC_DIR}pdf.worker.min.js`, { idempotent: true });
        await FileSystem.copyAsync({ from: `${PDFJS_DIR}pdf.min.js`, to: `${PDF_DOC_DIR}pdf.min.js` });
        await FileSystem.copyAsync({
          from: `${PDFJS_DIR}pdf.worker.min.js`,
          to: `${PDF_DOC_DIR}pdf.worker.min.js`,
        });
        await writePdf(uri, `${PDF_DOC_DIR}doc.pdf`);
        await FileSystem.writeAsStringAsync(`${PDF_DOC_DIR}index.html`, VIEWER_HTML);
        if (!cancelled) {
          setReadAccessUrl(toFileUri(PDF_DOC_DIR));
          setHtmlUri(toFileUri(`${PDF_DOC_DIR}index.html`));
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'PDF 加载失败');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };
    void run();
    return () => {
      cancelled = true;
    };
  }, [uri]);

  if (error) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  if (loading || !htmlUri) {
    return (
      <View style={styles.loadingWrap}>
        <ActivityIndicator color={colors.accent} />
        <Text style={styles.progress}>加载中…</Text>
      </View>
    );
  }

  return (
    <WebView
      source={{ uri: htmlUri }}
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
      onError={() => setError('预览失败')}
    />
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    backgroundColor: '#050B1C',
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  progress: {
    color: colors.muted,
    fontSize: 13,
  },
  fallback: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 28,
    gap: 16,
  },
  fallbackText: {
    color: colors.muted,
    fontSize: 14,
    textAlign: 'center',
    lineHeight: 22,
  },
});
