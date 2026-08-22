import { useFocusEffect, useLocalSearchParams } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { fetchAppNewsDetail } from '@/api/app-news';
import { ApiError } from '@/api/request';
import type { AppNewsDetail } from '@/api/types';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function NewsDetailScreen() {
  const { id } = useLocalSearchParams<{ id?: string }>();
  const newsId = Array.isArray(id) ? id[0] : id;
  const [loading, setLoading] = useState(true);
  const [news, setNews] = useState<AppNewsDetail | null>(null);

  const load = useCallback(async () => {
    if (!newsId) {
      setNews(null);
      setLoading(false);
      return;
    }
    try {
      setNews(await fetchAppNewsDetail(newsId));
    } catch (error) {
      setNews(null);
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取新闻详情失败');
      }
    } finally {
      setLoading(false);
    }
  }, [newsId]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <PageHeader title="" />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.content}
          onRefresh={load}
        >
          {!news ? (
            <Text style={styles.empty}>新闻不存在或已下架</Text>
          ) : (
            <GlassCard>
              {news.coverUrl ? (
                <Image source={{ uri: news.coverUrl }} style={styles.cover} contentFit="cover" />
              ) : null}
              <Text style={styles.title}>{news.title}</Text>
              <Text style={styles.date}>{news.publishDate}</Text>
              <Text style={styles.body}>{news.content || news.summary || '暂无内容'}</Text>
            </GlassCard>
          )}
        </RefreshableScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  cover: {
    width: '100%',
    height: 160,
    borderRadius: 10,
    marginBottom: 14,
  },
  title: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '800',
    lineHeight: 28,
  },
  date: {
    color: 'rgba(150, 175, 210, 0.75)',
    marginTop: 8,
    marginBottom: 16,
    fontSize: 13,
  },
  body: {
    color: colors.text,
    fontSize: 14,
    lineHeight: 24,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
});
