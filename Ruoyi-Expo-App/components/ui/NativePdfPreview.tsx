import * as WebBrowser from 'expo-web-browser';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import Pdf from 'react-native-pdf';

import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { getCachedPdfPath, prefetchPdf } from '@/utils/pdf-cache';

type Props = {
  uri: string;
};

function toFileUri(path: string) {
  return path.startsWith('file:') ? path : `file://${path}`;
}

export function NativePdfPreview({ uri }: Props) {
  const [sourceUri, setSourceUri] = useState('');
  const [fromCache, setFromCache] = useState(false);
  const [caching, setCaching] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const fallbackTriedRef = useRef(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    setSourceUri('');
    setFromCache(false);
    setCaching(false);
    fallbackTriedRef.current = false;

    const run = async () => {
      const cached = await getCachedPdfPath(uri);
      if (cancelled) {
        return;
      }
      if (cached) {
        setFromCache(true);
        setSourceUri(toFileUri(cached));
        return;
      }
      setCaching(true);
      setSourceUri(uri);
      void prefetchPdf(uri).then((path) => {
        if (!cancelled && path) {
          setCaching(false);
        }
      });
    };
    void run();

    return () => {
      cancelled = true;
    };
  }, [uri]);

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

  return (
    <View style={styles.fill}>
      {sourceUri ? (
        <Pdf
          source={{ uri: sourceUri, cache: false }}
          style={styles.fill}
          trustAllCerts={false}
          fitPolicy={0}
          spacing={8}
          onLoadComplete={() => setLoading(false)}
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
                setFromCache(true);
                setCaching(false);
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
      {fromCache || caching ? (
        <View style={styles.cacheTag}>
          <Text style={styles.cacheTagText}>{fromCache ? '已缓存' : '缓存中'}</Text>
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
  cacheTag: {
    position: 'absolute',
    top: 12,
    right: 12,
    backgroundColor: 'rgba(0,0,0,0.55)',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  cacheTagText: {
    color: '#FFFFFF',
    fontSize: 12,
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
