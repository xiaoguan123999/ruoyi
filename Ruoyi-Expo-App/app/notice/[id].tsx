import { useFocusEffect, useLocalSearchParams } from 'expo-router';
import { useCallback, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { fetchAppNoticeDetail } from '@/api/app-notice';
import { ApiError } from '@/api/request';
import type { AppNoticeDetail } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { NoticeHtmlContent } from '@/components/ui/NoticeHtmlContent';

export default function NoticeDetailScreen() {
  const { id } = useLocalSearchParams<{ id?: string }>();
  const noticeId = Array.isArray(id) ? id[0] : id;
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState<AppNoticeDetail | null>(null);

  const load = useCallback(async () => {
    if (!noticeId) {
      setNotice(null);
      setLoading(false);
      return;
    }
    try {
      setNotice(await fetchAppNoticeDetail(noticeId));
    } catch (error) {
      setNotice(null);
      if (!(error instanceof ApiError) || error.code !== 401) {
          modalError(error instanceof ApiError ? error.message : '获取公告详情失败');
        }
    } finally {
      setLoading(false);
    }
  }, [noticeId]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <AppBackground>
      <PageHeader title="公告详情" />
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
          {!notice ? (
            <Text style={styles.empty}>公告不存在或已下架</Text>
          ) : (
            <GlassCard>
              <Text style={styles.title}>{notice.title}</Text>
              <Text style={styles.date}>{notice.createTime}</Text>
              <NoticeHtmlContent html={notice.contentHtml || notice.content} textStyle={styles.body} />
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
    paddingBottom: 32,
  },
  title: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
    lineHeight: 26,
  },
  date: {
    color: colors.muted,
    marginTop: 10,
    marginBottom: 14,
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
