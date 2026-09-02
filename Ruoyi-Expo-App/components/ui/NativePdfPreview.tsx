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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const recoveringRef = useRef(false);
  const failedLocalUriRef = useRef('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    setSourceUri('');
    recoveringRef.current = false;
    failedLocalUriRef.current = '';

    const run = async () => {
      const cached = await getCachedPdfPath(uri);
      if (cancelled) {
        return;
      }
      if (cached) {
        setSourceUri(toFileUri(cached));
        return;
      }
      setSourceUri(uri);
      void prefetchPdf(uri);
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
          minScale={1}
          maxScale={4}
          enableDoubleTapZoom
          onLoadComplete={() => setLoading(false)}
          onError={() => {
            if (recoveringRef.current) {
              return;
            }
            const current = sourceUri;
            if (failedLocalUriRef.current && failedLocalUriRef.current === current) {
              setLoading(false);
              setError('预览失败');
              return;
            }
            recoveringRef.current = true;
            setLoading(true);
            void (async () => {
              const cached = (await getCachedPdfPath(uri)) || (await prefetchPdf(uri));
              const localUri = cached ? toFileUri(cached) : '';
              if (localUri && localUri !== current) {
                recoveringRef.current = false;
                setError('');
                setSourceUri(localUri);
                return;
              }
              if (localUri) {
                failedLocalUriRef.current = localUri;
              }
              recoveringRef.current = false;
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
