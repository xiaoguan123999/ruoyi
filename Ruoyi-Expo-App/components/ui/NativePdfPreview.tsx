import { Image } from 'expo-image';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  PixelRatio,
  ScrollView,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import Pdf from 'react-native-pdf';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { getCachedPdfPath, prefetchPdf } from '@/utils/pdf-cache';
import {
  getCachedPdfPages,
  rasterizePdfPages,
  type CachedPdfPage,
} from '@/utils/pdf-page-cache';

type Props = {
  uri: string;
};

function toFileUri(path: string) {
  return path.startsWith('file:') ? path : `file://${path}`;
}

function pageRenderScale(viewWidth: number) {
  return Number(Math.min(2, Math.max(1.25, (viewWidth * PixelRatio.get()) / 595)).toFixed(2));
}

export function NativePdfPreview({ uri }: Props) {
  const { width: windowWidth } = useWindowDimensions();
  const contentWidth = Math.max(280, windowWidth);
  const [pages, setPages] = useState<CachedPdfPage[]>([]);
  const [sourceUri, setSourceUri] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const fallbackTriedRef = useRef(false);
  const showedPdfRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    setPages([]);
    setSourceUri('');
    fallbackTriedRef.current = false;
    showedPdfRef.current = false;

    const scale = pageRenderScale(contentWidth);

    const run = async () => {
      const cachedPages = await getCachedPdfPages(uri);
      if (cancelled) {
        return;
      }
      if (cachedPages?.length) {
        setPages(cachedPages);
        setLoading(false);
        return;
      }

      const cachedPdf = await getCachedPdfPath(uri);
      if (cancelled) {
        return;
      }
      if (cachedPdf) {
        const rasterized = await rasterizePdfPages(cachedPdf, uri, scale);
        if (cancelled) {
          return;
        }
        if (rasterized?.length) {
          setPages(rasterized);
          setLoading(false);
          return;
        }
        setSourceUri(toFileUri(cachedPdf));
        return;
      }

      setSourceUri(uri);
      void prefetchPdf(uri).then(async (path) => {
        if (!path || cancelled) {
          return;
        }
        const rasterized = await rasterizePdfPages(path, uri, scale);
        if (cancelled || showedPdfRef.current || !rasterized?.length) {
          return;
        }
        setPages(rasterized);
        setSourceUri('');
        setLoading(false);
      });
    };
    void run();

    return () => {
      cancelled = true;
    };
  }, [uri, contentWidth]);

  const openExternal = () => {
    void WebBrowser.openBrowserAsync(uri);
  };

  if (error) {
    return (
      <View style={styles.fallback}>
        <Text style={styles.fallbackText}>{error}</Text>
        <PrimaryButton title="打开 PDF" onPress={openExternal} />
      </View>
    );
  }

  if (pages.length) {
    return (
      <ScrollView
        style={styles.fill}
        contentContainerStyle={styles.pageList}
        showsVerticalScrollIndicator={false}
      >
        {pages.map((page, index) => {
          const displayHeight = (contentWidth * page.height) / page.width;
          return (
            <Image
              key={`${page.uri}-${index}`}
              source={{ uri: page.uri }}
              style={{ width: contentWidth, height: displayHeight }}
              contentFit="contain"
            />
          );
        })}
      </ScrollView>
    );
  }

  return (
    <View style={styles.fill}>
      {sourceUri ? (
        <Pdf
          source={{ uri: sourceUri, cache: false }}
          style={styles.fill}
          trustAllCerts={false}
          fitPolicy={0}
          spacing={8}
          onLoadComplete={() => {
            showedPdfRef.current = true;
            setLoading(false);
          }}
          onError={() => {
            void (async () => {
              if (fallbackTriedRef.current) {
                setLoading(false);
                setError('预览失败');
                return;
              }
              fallbackTriedRef.current = true;
              const cached = await prefetchPdf(uri);
              if (cached) {
                const rasterized = await rasterizePdfPages(
                  cached,
                  uri,
                  pageRenderScale(contentWidth),
                );
                if (rasterized?.length) {
                  setError('');
                  setPages(rasterized);
                  setSourceUri('');
                  setLoading(false);
                  return;
                }
                setError('');
                setLoading(true);
                setSourceUri(toFileUri(cached));
                return;
              }
              setLoading(false);
              setError('预览失败');
            })();
          }}
        />
      ) : null}
      {loading ? (
        <View style={styles.loadingOverlay} pointerEvents="none">
          <ActivityIndicator color={colors.accent} />
          <Text style={styles.progress}>加载中…</Text>
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
  pageList: {
    paddingBottom: 16,
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
    flex: 1,
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
