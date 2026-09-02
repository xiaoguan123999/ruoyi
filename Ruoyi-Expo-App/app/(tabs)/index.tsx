import { useFocusEffect, useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, useWindowDimensions, View } from 'react-native';

import { fetchAppVideoCarousel } from '@/api/app-video';
import { fetchAppNotices, NOTICE_TYPE_NOTIFICATION } from '@/api/app-notice';
import { fetchAppOverview } from '@/api/app-overview';
import type { AppNotice, AppOverviewItem, AppVideoCarouselItem } from '@/api/types';
import { BannerVideoPlayer } from '@/components/ui/BannerVideoPlayer';
import { NoticeMarquee } from '@/components/ui/NoticeMarquee';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { useStableSafeTop } from '@/hooks/useStableSafeTop';
import { colors } from '@/theme/colors';
import { modalWarning } from '@/utils/toast';
import { prefetchVideos } from '@/utils/video-cache';

const PAGE_BG = '#050B1C';
const PANEL_BG = '#0B1730';

function HomeVideoCarousel({
  items,
  width,
  height,
}: {
  items: AppVideoCarouselItem[];
  width: number;
  height: number;
}) {
  const scrollRef = useRef<ScrollView | null>(null);
  const [activeIndex, setActiveIndex] = useState(0);
  const [playing, setPlaying] = useState<AppVideoCarouselItem | null>(null);

  const slides = items.filter((it) => !!it.coverUrl || !!it.videoUrl);
  const slideCount = Math.max(1, slides.length);

  useEffect(() => {
    if (slideCount <= 1 || playing) {
      return;
    }
    const t = setInterval(() => {
      const nextIndex = (activeIndex + 1) % slideCount;
      scrollRef.current?.scrollTo({ x: nextIndex * width, animated: true });
    }, 3500);
    return () => clearInterval(t);
  }, [activeIndex, slideCount, width, playing]);

  const onPlay = (item: AppVideoCarouselItem) => {
    if (!item.videoUrl) {
      modalWarning('暂无可播放视频');
      return;
    }
    setPlaying(item);
  };

  return (
    <View style={{ width, height }}>
      <ScrollView
        ref={(r) => {
          scrollRef.current = r;
        }}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        scrollEventThrottle={16}
        onScroll={(e) => {
          const x = e.nativeEvent.contentOffset.x;
          const idx = Math.round(x / width);
          if (idx !== activeIndex) {
            setActiveIndex(Math.max(0, Math.min(idx, slideCount - 1)));
          }
        }}
        onMomentumScrollEnd={(e) => {
          const x = e.nativeEvent.contentOffset.x;
          const idx = Math.round(x / width);
          setActiveIndex(Math.max(0, Math.min(idx, slideCount - 1)));
        }}
      >
        {slides.map((item) => (
          <Pressable
            key={item.id}
            style={{ width, height }}
            onPress={() => onPlay(item)}
          >
            <Image
              source={item.coverUrl ? { uri: item.coverUrl } : images.banner}
              style={{ width, height }}
              contentFit="cover"
            />
            {item.videoUrl ? (
              <View style={styles.playOverlay} pointerEvents="none">
                <View style={styles.playBtn}>
                  <Text style={styles.playIcon}>▶</Text>
                </View>
              </View>
            ) : null}
          </Pressable>
        ))}
      </ScrollView>

      {slideCount > 1 ? (
        <View style={styles.bannerDots}>
          {slides.map((item, index) => (
            <View
              key={item.id}
              style={[styles.bannerDot, index === activeIndex && styles.bannerDotActive]}
            />
          ))}
        </View>
      ) : null}

      <BannerVideoPlayer
        uri={playing?.videoUrl ?? ''}
        cacheId={playing?.id}
        title={playing?.title}
        visible={!!playing?.videoUrl}
        onClose={() => setPlaying(null)}
      />
    </View>
  );
}

const services = [
  { key: 'checkin', label: '每日签到', icon: images.iconCheckin, href: '/check-in' },
  { key: 'group', label: '官方群聊', icon: images.iconGroup, href: '/group-chat' },
  { key: 'team', label: '我的团队', icon: images.iconTeam, href: '/team' },
  { key: 'invite', label: '邀请好友', icon: images.iconInvite, href: '/invite' },
  { key: 'about', label: '关于我们', icon: images.iconAbout, href: '/about' },
  { key: 'service', label: '客服中心', icon: images.iconService, href: '/service' },
];

export default function HomeScreen() {
  const router = useRouter();
  const top = useStableSafeTop();
  const { width } = useWindowDimensions();
  const bannerH = Math.round(width * (194 / 402));
  const iconSize = Math.min(70, Math.max(56, Math.round(width * 0.155)));
  const [notices, setNotices] = useState<AppNotice[]>([]);
  const [overview, setOverview] = useState<AppOverviewItem[]>([]);
  const [videos, setVideos] = useState<AppVideoCarouselItem[]>([]);

  const load = useCallback(async () => {
    const [nextNotices, nextOverview] = await Promise.all([
      fetchAppNotices(NOTICE_TYPE_NOTIFICATION).catch(() => [] as AppNotice[]),
      fetchAppOverview().catch(() => [] as AppOverviewItem[]),
    ]);
    setNotices(nextNotices);
    setOverview(nextOverview);
  }, []);

  const loadVideos = useCallback(async () => {
    const list = await fetchAppVideoCarousel().catch(() => [] as AppVideoCarouselItem[]);
    setVideos(list);
    // 列表加载后后台预缓存全部视频（不阻塞 UI）
    void prefetchVideos(list);
  }, []);

  useFocusEffect(
    useCallback(() => {
      void (async () => {
        await load();
        await loadVideos();
      })();
    }, [load, loadVideos]),
  );

  const bannerEl = useMemo(() => {
    if (videos.length > 0) {
      return <HomeVideoCarousel items={videos} width={width} height={bannerH} />;
    }
    return <Image source={images.banner} style={{ width, height: bannerH }} contentFit="cover" />;
  }, [videos, width, bannerH]);

  return (
    <View style={styles.page}>
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 16 }}
        onRefresh={() => Promise.all([load(), loadVideos()]).then(() => undefined)}
      >
        <View>
          <View style={[styles.header, { paddingTop: top + 6 }]}>
            <Image source={images.logo} style={styles.headerLogo} contentFit="contain" />
            <Text style={styles.headerTitle}>星帆智联</Text>
          </View>
          {bannerEl}
        </View>

        <View style={styles.body}>
          <Pressable style={styles.notice} onPress={() => router.push('/notice')}>
            <View style={styles.noticeTag}>
              <Text style={styles.noticeTagText}>公告</Text>
            </View>
            <NoticeMarquee
              texts={notices.map((item) => {
                const title = item.title.trim();
                const content = (item.content || '').replace(/\s+/g, ' ').trim();
                return content ? `${title}　${content}` : title;
              })}
              textStyle={styles.noticeText}
            />
          </Pressable>

          <View style={styles.gridPanel}>
            {services.map((item) => (
              <Pressable
                key={item.key}
                style={styles.gridItem}
                onPress={() => router.push(item.href as never)}
              >
                <Image
                  source={item.icon}
                  style={{ width: iconSize, height: iconSize, marginBottom: 6 }}
                  contentFit="contain"
                />
                <Text style={styles.gridLabel}>{item.label}</Text>
              </Pressable>
            ))}
          </View>

          <Text style={styles.section}>运行概览</Text>
          <View style={styles.statsRow}>
            {overview.map((item) => (
              <View key={item.id} style={styles.statCard}>
                <Image
                  source={item.imageUrl ? { uri: item.imageUrl } : item.imageFallback}
                  style={StyleSheet.absoluteFill}
                  contentFit="cover"
                  contentPosition="bottom right"
                />
                <Text style={styles.statTitle}>{item.title}</Text>
                <Text style={styles.statValue}>{item.displayValue}</Text>
                <View style={styles.statusRow}>
                  <View style={[styles.dot, { backgroundColor: item.statusColor || '#4DA3FF' }]} />
                  <Text style={styles.statusText}>{item.statusText}</Text>
                </View>
              </View>
            ))}
          </View>
        </View>
      </RefreshableScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: PAGE_BG,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingBottom: 10,
    backgroundColor: PAGE_BG,
  },
  headerLogo: { width: 26, height: 26, borderRadius: 6 },
  headerTitle: { color: colors.text, fontSize: 16, fontWeight: '700' },
  playOverlay: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.18)',
  },
  playBtn: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: 'rgba(0, 0, 0, 0.45)',
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.85)',
    alignItems: 'center',
    justifyContent: 'center',
    paddingLeft: 3,
  },
  playIcon: {
    color: '#FFFFFF',
    fontSize: 22,
    fontWeight: '700',
  },
  bannerDots: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 10,
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 6,
  },
  bannerDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: 'rgba(255, 255, 255, 0.35)',
  },
  bannerDotActive: {
    width: 14,
    backgroundColor: '#FFFFFF',
  },
  body: {
    backgroundColor: PAGE_BG,
    paddingBottom: 8,
  },
  notice: {
    marginHorizontal: 16,
    marginTop: 12,
    backgroundColor: PANEL_BG,
    borderRadius: 22,
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    paddingHorizontal: 12,
    gap: 8,
    overflow: 'hidden',
    minWidth: 0,
  },
  noticeTag: {
    backgroundColor: '#2F7BFF',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  noticeTagText: { color: colors.text, fontSize: 11, fontWeight: '700' },
  noticeText: { color: colors.text, fontSize: 13 },
  gridPanel: {
    marginHorizontal: 16,
    marginTop: 14,
    paddingTop: 12,
    paddingBottom: 8,
    borderRadius: 16,
    backgroundColor: PANEL_BG,
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  gridItem: {
    width: '33.33%',
    alignItems: 'center',
    paddingVertical: 12,
  },
  gridLabel: { color: colors.text, fontSize: 12 },
  section: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    marginHorizontal: 16,
    marginTop: 18,
    marginBottom: 10,
  },
  statsRow: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    gap: 8,
  },
  statCard: {
    flex: 1,
    aspectRatio: 1,
    paddingTop: 10,
    paddingHorizontal: 8,
    borderRadius: 12,
    overflow: 'hidden',
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: 'rgba(90, 160, 230, 0.35)',
    backgroundColor: '#0A1630',
  },
  statTitle: {
    color: 'rgba(180, 200, 230, 0.85)',
    fontSize: 11,
    lineHeight: 14,
  },
  statValue: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '800',
    marginTop: 8,
    lineHeight: 20,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 6,
  },
  dot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  statusText: {
    color: 'rgba(200, 215, 240, 0.88)',
    fontSize: 10,
  },
});
