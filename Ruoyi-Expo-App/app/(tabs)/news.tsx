import { useFocusEffect, useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, useWindowDimensions, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { fetchAppNews } from '@/api/app-news';
import { ApiError } from '@/api/request';
import type { AppNewsItem } from '@/api/types';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function NewsScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { width } = useWindowDimensions();
  const headerH = Math.round(width * (170 / 402));
  const thumb = Math.min(84, Math.max(72, Math.round(width * 0.19)));
  const [loading, setLoading] = useState(true);
  const [list, setList] = useState<AppNewsItem[]>([]);

  const load = useCallback(async () => {
    try {
      setList(await fetchAppNews());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取新闻失败');
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
    <View style={styles.page}>
      <View style={styles.headerWrap}>
        <Image
          source={images.newsBg}
          style={{ width, height: headerH }}
          contentFit="cover"
          contentPosition="top"
        />
        <Text style={[styles.title, { top: insets.top + 10 }]}>新闻资讯</Text>
      </View>

      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.list}
          onRefresh={load}
        >
          {list.length === 0 ? (
            <Text style={styles.empty}>暂无新闻</Text>
          ) : (
            list.map((item) => (
              <Pressable key={item.id} onPress={() => router.push(`/news/${item.id}`)}>
                <View style={styles.card}>
                  {item.coverUrl ? (
                    <Image
                      source={{ uri: item.coverUrl }}
                      style={[styles.cover, { width: thumb, height: thumb }]}
                      contentFit="cover"
                    />
                  ) : null}
                  <View style={[styles.body, { minHeight: item.coverUrl ? thumb : undefined }]}>
                    <Text style={styles.headline} numberOfLines={2}>
                      {item.title}
                    </Text>
                    <Text style={styles.summary} numberOfLines={2}>
                      {item.summary || '--'}
                    </Text>
                    <Text style={styles.date}>{item.publishDate}</Text>
                  </View>
                  <Text style={styles.chevron}>›</Text>
                </View>
              </Pressable>
            ))
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
  headerWrap: {
    position: 'relative',
  },
  title: {
    position: 'absolute',
    left: 0,
    right: 0,
    textAlign: 'center',
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  list: {
    paddingHorizontal: 16,
    paddingTop: 14,
    paddingBottom: 24,
    gap: 12,
  },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 12,
    borderRadius: 14,
    backgroundColor: '#0A1528',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
  },
  cover: {
    borderRadius: 8,
  },
  body: {
    flex: 1,
    justifyContent: 'space-between',
    paddingVertical: 2,
  },
  headline: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '700',
    lineHeight: 20,
  },
  summary: {
    color: 'rgba(150, 175, 210, 0.75)',
    fontSize: 12,
    marginTop: 4,
    lineHeight: 17,
  },
  date: {
    color: 'rgba(150, 175, 210, 0.75)',
    fontSize: 11,
    marginTop: 8,
  },
  chevron: {
    color: 'rgba(220, 230, 255, 0.55)',
    fontSize: 22,
    paddingLeft: 4,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
});
