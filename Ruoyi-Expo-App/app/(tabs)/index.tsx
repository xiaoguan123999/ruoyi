import { useFocusEffect, useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, Text, useWindowDimensions, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { fetchAppNotices } from '@/api/app-notice';
import { fetchAppOverview } from '@/api/app-overview';
import type { AppNotice, AppOverviewItem } from '@/api/types';
import { NoticeMarquee } from '@/components/ui/NoticeMarquee';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

const PAGE_BG = '#050B1C';
const PANEL_BG = '#0B1730';

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
  const insets = useSafeAreaInsets();
  const { width } = useWindowDimensions();
  const bannerH = Math.round(width * (194 / 402));
  const iconSize = Math.min(70, Math.max(56, Math.round(width * 0.155)));
  const [notices, setNotices] = useState<AppNotice[]>([]);
  const [overview, setOverview] = useState<AppOverviewItem[]>([]);

  const load = useCallback(async () => {
    const [nextNotices, nextOverview] = await Promise.all([
      fetchAppNotices().catch(() => [] as AppNotice[]),
      fetchAppOverview().catch(() => [] as AppOverviewItem[]),
    ]);
    setNotices(nextNotices);
    setOverview(nextOverview);
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 16 }}
        onRefresh={load}
      >
        <View>
          <Image source={images.banner} style={{ width, height: bannerH }} contentFit="cover" />
          <View style={[styles.header, { top: insets.top + 4 }]}>
            <Image source={images.logo} style={styles.headerLogo} contentFit="contain" />
            <Text style={styles.headerTitle}>星帆智联</Text>
          </View>
        </View>

        <View style={styles.body}>
          <Pressable style={styles.notice} onPress={() => router.push('/notice')}>
            <View style={styles.noticeTag}>
              <Text style={styles.noticeTagText}>公告</Text>
            </View>
            <NoticeMarquee
              texts={notices.map((item) => item.title)}
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
    position: 'absolute',
    left: 16,
    right: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  headerLogo: { width: 26, height: 26, borderRadius: 6 },
  headerTitle: { color: colors.text, fontSize: 16, fontWeight: '700' },
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
