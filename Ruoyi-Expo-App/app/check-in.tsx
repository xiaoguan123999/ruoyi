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
import { appCheckin, fetchAppCheckinInfo, fetchAppCheckinList } from '@/api/app-trade';
import type { AppCheckinInfo } from '@/api/types';
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

function formatAmountText(amount: number): string {
  if (!Number.isFinite(amount)) {
    return '0';
  }
  if (Number.isInteger(amount)) {
    return String(amount);
  }
  return amount.toFixed(2).replace(/\.?0+$/, '');
}

const RULE_DISCLAIMER = '规则如有调整将会提前通知，最终解释权归星帆智联所有';

function appendDisclaimer(lines: string[]): string[] {
  if (lines.length === 0) {
    return lines;
  }
  if (lines.some((line) => line.includes('最终解释权'))) {
    return lines;
  }
  return [...lines, RULE_DISCLAIMER];
}

function buildRuleLines(info: AppCheckinInfo | null): string[] {
  if (!info) {
    return [];
  }

  const fromApi = info.ruleText?.trim();
  if (fromApi) {
    return appendDisclaimer(
      fromApi
        .split(/\n+/)
        .map((line) => line.trim())
        .filter(Boolean),
    );
  }

  const lines: string[] = [];
  const amountText = formatAmountText(info.rule.amount || info.amount);
  lines.push(`1、每天签到可以获得${amountText}元`);

  const prizes = info.rule.prizes.filter((item) => item.enabled);
  if (prizes.length > 0) {
    const prizeParts = prizes.map(
      (prize) => `连续签到满${prize.days}天可以有机会获得${prize.name}一台`,
    );
    lines.push(`2、${prizeParts.join('；')}`);
  }

  if (info.rule.oncePerDay !== false) {
    const index = prizes.length > 0 ? 3 : 2;
    lines.push(`${index}、每个账户每日仅可签到一次`);
  }

  return appendDisclaimer(lines);
}

export default function CheckInScreen() {
  const { width } = useWindowDimensions();
  const now = useMemo(() => new Date(), []);
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const today = now.getDate();
  const todayKey = `${year}-${String(month).padStart(2, '0')}-${String(today).padStart(2, '0')}`;

  const [signedDays, setSignedDays] = useState<Set<string>>(new Set());
  const [info, setInfo] = useState<AppCheckinInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const cells = useMemo(() => buildCalendarCells(year, month), [year, month]);
  const pad = 16;
  const cardPad = 14;
  const gap = 6;
  const cellSize = Math.floor((width - pad * 2 - cardPad * 2 - gap * 6) / 7);
  const checkedToday = info?.checkedToday ?? signedDays.has(todayKey);
  const streak = info?.streakDays ?? 0;
  const ruleLines = useMemo(() => buildRuleLines(info), [info]);

  const load = useCallback(async () => {
    try {
      const [nextInfo, list] = await Promise.all([fetchAppCheckinInfo(), fetchAppCheckinList()]);
      setInfo(nextInfo);
      setSignedDays(new Set(list.map((item) => item.checkinDate)));
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取签到信息失败');
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
          {ruleLines.length > 0 ? (
            ruleLines.map((line, index) => (
              <Text
                key={`${index}-${line.slice(0, 12)}`}
                style={[styles.rule, line.includes('最终解释权') ? styles.ruleDisclaimer : null]}
              >
                {line}
              </Text>
            ))
          ) : (
            <Text style={styles.rule}>暂无规则说明</Text>
          )}
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
  ruleDisclaimer: {
    marginTop: 10,
    color: 'rgba(180, 198, 220, 0.72)',
    fontSize: 13,
  },
});
