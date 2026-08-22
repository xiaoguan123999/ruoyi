import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { fetchAppNotices } from '@/api/app-notice';
import { ApiError } from '@/api/request';
import type { AppNotice } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function NoticeListScreen() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [notices, setNotices] = useState<AppNotice[]>([]);

  const load = useCallback(async () => {
    try {
      setNotices(await fetchAppNotices());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取公告失败');
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

  return (
    <AppBackground>
      <PageHeader title="公告" />
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
          {notices.length === 0 ? (
            <Text style={styles.empty}>暂无公告</Text>
          ) : (
            notices.map((item) => (
              <Pressable key={item.id} onPress={() => router.push(`/notice/${item.id}`)}>
                <GlassCard style={styles.card}>
                  <Text style={styles.title} numberOfLines={2}>
                    {item.title}
                  </Text>
                  <Text style={styles.date}>{item.createTime}</Text>
                </GlassCard>
              </Pressable>
            ))
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
    gap: 12,
  },
  card: {
    paddingVertical: 16,
  },
  title: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    lineHeight: 24,
  },
  date: {
    color: colors.muted,
    marginTop: 10,
    fontSize: 13,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
});
