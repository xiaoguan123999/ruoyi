import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import { Image } from 'expo-image';

import { ApiError } from '@/api/request';
import { appCheckin, fetchAppCheckinList } from '@/api/app-trade';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

const WEEK_LABELS = ['日', '一', '二', '三', '四', '五', '六'];

function buildCalendarCells(year: number, month: number) {
  const firstWeekday = new Date(year, month - 1, 1).getDay();
  const daysInMonth = new Date(year, month, 0).getDate();
  const cells: (number | null)[] = Array.from({ length: firstWeekday }, () => null);
  for (let d = 1; d <= daysInMonth; d += 1) {
    cells.push(d);
  }
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }
  return cells;
}

function calcStreak(dates: Set<string>, today: Date): number {
  let streak = 0;
  const cursor = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  while (true) {
    const key = `${cursor.getFullYear()}-${String(cursor.getMonth() + 1).padStart(2, '0')}-${String(cursor.getDate()).padStart(2, '0')}`;
    if (!dates.has(key)) {
      break;
    }
    streak += 1;
    cursor.setDate(cursor.getDate() - 1);
  }
  return streak;
}

export default function CheckInScreen() {
  const { width } = useWindowDimensions();
  const now = useMemo(() => new Date(), []);
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const today = now.getDate();
  const todayKey = `${year}-${String(month).padStart(2, '0')}-${String(today).padStart(2, '0')}`;

  const [signedDays, setSignedDays] = useState<Set<string>>(new Set());
  const [streak, setStreak] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const cells = useMemo(() => buildCalendarCells(year, month), [year, month]);
  const pad = 16;
  const cardPad = 14;
  const gap = 6;
  const cellSize = Math.floor((width - pad * 2 - cardPad * 2 - gap * 6) / 7);
  const checkedToday = signedDays.has(todayKey);

  const load = useCallback(async () => {
    try {
      const list = await fetchAppCheckinList();
      const dates = new Set(list.map((item) => item.checkinDate));
      setSignedDays(dates);
      setStreak(calcStreak(dates, new Date()));
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取签到记录失败');
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

  const onCheckIn = async () => {
    if (submitting) {
      return;
    }
    if (checkedToday) {
      modalWarning('今日已签到');
      return;
    }
    setSubmitting(true);
    try {
      const result = await appCheckin();
      modalSuccess(result.message);
      await load();
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '签到失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground source={images.pageBg}>
      <PageHeader title="每日签到" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scroll}
        onRefresh={load}
      >
        <View style={styles.streakCard}>
          <Image source={images.checkinBg} style={StyleSheet.absoluteFill} contentFit="cover" />
          <Text style={styles.streakText}>连续签到 {streak} 天</Text>
        </View>

        <GlassCard style={styles.calendarCard}>
          <Text style={styles.month}>
            {year}年{month}月
          </Text>

          {loading ? (
            <View style={styles.loadingWrap}>
              <ActivityIndicator color={colors.accent} />
            </View>
          ) : (
            <>
              <View style={styles.weekRow}>
                {WEEK_LABELS.map((label) => (
                  <Text key={label} style={[styles.weekItem, { width: cellSize }]}>
                    {label}
                  </Text>
                ))}
              </View>

              <View style={styles.grid}>
                {cells.map((day, index) => {
                  if (day == null) {
                    return <View key={`e-${index}`} style={{ width: cellSize, height: cellSize }} />;
                  }
                  const key = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                  const signed = signedDays.has(key);
                  return (
                    <View
                      key={day}
                      style={[
                        styles.dayCell,
                        { width: cellSize, height: cellSize },
                        signed && styles.daySigned,
                      ]}
                    >
                      <Text style={[styles.dayText, signed && styles.dayTextSigned]}>{day}</Text>
                    </View>
                  );
                })}
              </View>
            </>
          )}

          <View style={styles.btnWrap}>
            <PrimaryButton
              title={checkedToday ? '今日已签到' : '立即签到'}
              onPress={() => void onCheckIn()}
              compact
              disabled={submitting || checkedToday || loading}
            />
          </View>
        </GlassCard>

        <GlassCard style={styles.ruleCard}>
          <Text style={styles.ruleTitle}>签到规则</Text>
          <Text style={styles.rule}>1、每天签到可以获得2元</Text>
          <Text style={styles.rule}>2、</Text>
        </GlassCard>
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 12,
  },
  streakCard: {
    height: 88,
    borderRadius: 14,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
    justifyContent: 'center',
    paddingHorizontal: 18,
    backgroundColor: '#0A1528',
  },
  streakText: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
  },
  calendarCard: {
    paddingVertical: 16,
  },
  month: {
    color: colors.text,
    textAlign: 'center',
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 14,
  },
  loadingWrap: {
    minHeight: 160,
    alignItems: 'center',
    justifyContent: 'center',
  },
  weekRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  weekItem: {
    textAlign: 'center',
    color: colors.text,
    fontSize: 13,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    rowGap: 8,
  },
  dayCell: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  daySigned: {
    backgroundColor: '#5A2E24',
    borderRadius: 6,
    borderWidth: 1,
    borderColor: 'rgba(220, 170, 150, 0.55)',
  },
  dayText: {
    color: colors.text,
    fontSize: 14,
  },
  dayTextSigned: {
    color: '#FFFFFF',
    fontWeight: '600',
  },
  btnWrap: {
    marginTop: 18,
  },
  ruleCard: {
    marginBottom: 8,
  },
  ruleTitle: {
    color: colors.text,
    fontWeight: '700',
    marginBottom: 8,
    fontSize: 15,
  },
  rule: {
    color: 'rgba(200, 215, 235, 0.85)',
    lineHeight: 24,
    fontSize: 14,
  },
});
