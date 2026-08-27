import { useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import * as WebBrowser from 'expo-web-browser';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';

type Props = {
  /** PDF 绝对或相对地址 */
  uri: string;
};

type PageImage = {
  page: number;
  uri: string;
  width: number;
  height: number;
};

type PdfJsModule = {
  GlobalWorkerOptions: { workerSrc: string };
  getDocument: (src: { url: string }) => { promise: Promise<PdfDocument> };
};

type PdfDocument = {
  numPages: number;
  getPage: (pageNumber: number) => Promise<PdfPage>;
};

type PdfPage = {
  getViewport: (params: { scale: number }) => { width: number; height: number };
  render: (params: {
    canvasContext: CanvasRenderingContext2D;
    viewport: { width: number; height: number };
  }) => { promise: Promise<void> };
};

declare global {
  interface Window {
    __pdfjsLib?: PdfJsModule;
  }
}

/** 内存缓存：同会话内切走再回来 / 宽度未变时不再重复解码 */
const pageCache = new Map<string, PageImage[]>();

function cacheKey(uri: string, contentWidth: number) {
  return `${uri}|${contentWidth}`;
}

/** 不经 Metro 打包，用浏览器原生 ESM 加载 public 下的 pdf.js，避免 import.meta 报错 */
function loadPdfJs(): Promise<PdfJsModule> {
  if (typeof window === 'undefined') {
    return Promise.reject(new Error('仅支持 Web'));
  }
  if (window.__pdfjsLib) {
    return Promise.resolve(window.__pdfjsLib);
  }

  return new Promise((resolve, reject) => {
    const onReady = () => {
      if (window.__pdfjsLib) {
        resolve(window.__pdfjsLib);
        return;
      }
      reject(new Error('pdf.js 加载失败'));
    };

    window.addEventListener('pdfjs-ready', onReady, { once: true });

    const script = document.createElement('script');
    script.type = 'module';
    script.textContent = `
      import * as pdfjs from '/mock/pdf.min.mjs';
      window.__pdfjsLib = pdfjs;
      window.dispatchEvent(new Event('pdfjs-ready'));
    `;
    script.onerror = () => {
      window.removeEventListener('pdfjs-ready', onReady);
      reject(new Error('pdf.js 脚本加载失败'));
    };
    document.head.appendChild(script);
  });
}

/**
 * 用 pdf.js 把每一页画成图片再列表展示。
 * 手机端无法可靠 iframe 嵌 PDF，需要解码后展示；本地文件同样要逐页画图。
 */
export function PdfPagesViewer({ uri }: Props) {
  const { width: windowWidth } = useWindowDimensions();
  // 取整，避免手机浏览器地址栏显隐导致宽度微变、整份 PDF 重渲
  const contentWidth = Math.max(280, Math.round((windowWidth - 24) / 8) * 8);
  const [pages, setPages] = useState<PageImage[]>(() => pageCache.get(cacheKey(uri, contentWidth)) ?? []);
  const [loading, setLoading] = useState(() => !pageCache.has(cacheKey(uri, contentWidth)));
  const [error, setError] = useState('');
  const cancelledRef = useRef(false);

  useEffect(() => {
    cancelledRef.current = false;
    const key = cacheKey(uri, contentWidth);
    const cached = pageCache.get(key);
    if (cached?.length) {
      setPages(cached);
      setLoading(false);
      setError('');
      return;
    }

    const run = async () => {
      setLoading(true);
      setError('');
      setPages([]);

      try {
        if (Platform.OS !== 'web' || typeof document === 'undefined') {
          setError('native');
          setLoading(false);
          return;
        }

        const pdfjs = await loadPdfJs();
        pdfjs.GlobalWorkerOptions.workerSrc = '/mock/pdf.worker.min.mjs';

        const pdf = await pdfjs.getDocument({ url: uri }).promise;
        if (cancelledRef.current) {
          return;
        }

        const nextPages: PageImage[] = [];
        for (let pageNum = 1; pageNum <= pdf.numPages; pageNum += 1) {
          if (cancelledRef.current) {
            return;
          }
          const page = await pdf.getPage(pageNum);
          const unscaled = page.getViewport({ scale: 1 });
          const scale = contentWidth / unscaled.width;
          const viewport = page.getViewport({ scale: Math.min(1.6, Math.max(1, scale)) });

          const canvas = document.createElement('canvas');
          canvas.width = Math.ceil(viewport.width);
          canvas.height = Math.ceil(viewport.height);
          const context = canvas.getContext('2d');
          if (!context) {
            throw new Error('无法创建画布');
          }

          await page.render({
            canvasContext: context,
            viewport,
          }).promise;

          nextPages.push({
            page: pageNum,
            uri: canvas.toDataURL('image/jpeg', 0.72),
            width: canvas.width,
            height: canvas.height,
          });
          // 静默追加，不再刷「渲染中 x/y」
          setPages([...nextPages]);
        }

        pageCache.set(key, nextPages);
      } catch (err) {
        if (!cancelledRef.current) {
          setError(err instanceof Error ? err.message : 'PDF 加载失败');
        }
      } finally {
        if (!cancelledRef.current) {
          setLoading(false);
        }
      }
    };

    void run();
    return () => {
      cancelledRef.current = true;
    };
  }, [uri, contentWidth]);

  if (error === 'native' || (error && Platform.OS !== 'web')) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>当前环境请使用系统阅读器打开</Text>
        <PrimaryButton title="打开 PDF" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  if (error && pages.length === 0) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="尝试用浏览器打开" onPress={() => void WebBrowser.openBrowserAsync(uri)} />
      </View>
    );
  }

  return (
    <View style={styles.root}>
      {loading && pages.length === 0 ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.progress}>加载中…</Text>
        </View>
      ) : (
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={styles.scrollContent}
          showsVerticalScrollIndicator={false}
        >
          {pages.map((page) => {
            const displayHeight = (contentWidth * page.height) / page.width;
            return (
              <Image
                key={page.page}
                source={{ uri: page.uri }}
                style={{ width: contentWidth, height: displayHeight, alignSelf: 'center' }}
                resizeMode="contain"
              />
            );
          })}
          {loading ? (
            <View style={styles.tailLoading}>
              <ActivityIndicator color={colors.accent} size="small" />
            </View>
          ) : null}
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  scroll: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 12,
    paddingBottom: 24,
    gap: 10,
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
  tailLoading: {
    alignItems: 'center',
    paddingVertical: 12,
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
