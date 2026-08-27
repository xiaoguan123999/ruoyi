import { useFocusEffect } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { fetchAppAbout } from '@/api/app-about';
import { ApiError } from '@/api/request';
import type { AppAbout } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PdfPagesViewer } from '@/components/ui/PdfPagesViewer';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function AboutScreen() {
  const [loading, setLoading] = useState(true);
  const [about, setAbout] = useState<AppAbout | null>(null);

  const load = useCallback(async () => {
    try {
      setAbout(await fetchAppAbout());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取关于我们失败');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  const isPdf = about?.mode === 'PDF' && !!about.pdfUrl;

  return (
    <AppBackground>
      <PageHeader title="关于我们" />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : isPdf ? (
        <PdfPagesViewer uri={about.pdfUrl!} />
      ) : (
        <RefreshableScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          onRefresh={load}
        >
          {!about || (!about.title && !about.subtitle && !about.content && !about.imageUrl) ? (
            <Text style={styles.empty}>暂无内容</Text>
          ) : (
            <GlassCard style={styles.card}>
              {about.imageUrl ? (
                <Image source={{ uri: about.imageUrl }} style={styles.cover} contentFit="cover" />
              ) : null}
              {about.title ? <Text style={styles.title}>{about.title}</Text> : null}
              {about.subtitle ? <Text style={styles.subtitle}>{about.subtitle}</Text> : null}
              {about.content ? <Text style={styles.body}>{about.content}</Text> : null}
            </GlassCard>
          )}
        </RefreshableScrollView>
      )}
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 12,
  },
  card: {
    paddingVertical: 18,
  },
  cover: {
    width: '100%',
    height: 140,
    borderRadius: 10,
    marginBottom: 14,
  },
  title: {
    color: colors.text,
    fontSize: 22,
    fontWeight: '800',
  },
  subtitle: {
    color: colors.muted,
    marginTop: 10,
    fontSize: 15,
    lineHeight: 22,
  },
  body: {
    color: colors.muted,
    marginTop: 10,
    lineHeight: 22,
    fontSize: 14,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
});
