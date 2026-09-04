import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
  type StyleProp,
  type TextStyle,
} from 'react-native';

import { emptyTeamView, fetchAppTeam, formatTeamAmount, TEAM_LEVEL_NOS } from '@/api/app-team';
import { ApiError } from '@/api/request';
import type { AppTeamLevelStats, AppTeamMemberItem } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import {
  TEAM_LEVEL_LABELS,
  TEAM_LEVELS,
  TEAM_TAB_LABELS,
  type TeamLevelNo,
  type TeamUiLevelRow,
} from '@/constants/team-ui';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

const MEMBER_PAGE_SIZE = 20;
const LOAD_MORE_DISTANCE = 160;

function toLevelRow(api: AppTeamLevelStats): TeamUiLevelRow {
  return {
    register: api.register,
    active: api.active,
    rechargeCny: api.rechargeCny,
    rechargeUsd: api.rechargeUsd,
    subscribeCny: api.subscribeCny,
    subscribeUsd: api.subscribeUsd,
  };
}

function sumStats(rows: TeamUiLevelRow[]) {
  return rows.reduce(
    (acc, row) => ({
      register: acc.register + row.register,
      active: acc.active + row.active,
      rechargeCny: acc.rechargeCny + row.rechargeCny,
      rechargeUsd: acc.rechargeUsd + row.rechargeUsd,
      subscribeCny: acc.subscribeCny + row.subscribeCny,
      subscribeUsd: acc.subscribeUsd + row.subscribeUsd,
    }),
    {
      register: 0,
      active: 0,
      rechargeCny: 0,
      rechargeUsd: 0,
      subscribeCny: 0,
      subscribeUsd: 0,
    },
  );
}

function FitText({
  children,
  style,
}: {
  children: string;
  style: StyleProp<TextStyle>;
}) {
  return (
    <Text
      style={style}
      numberOfLines={1}
      allowFontScaling={false}
      adjustsFontSizeToFit
      minimumFontScale={0.7}
    >
      {children}
    </Text>
  );
}

function UnitStack() {
  return (
    <View style={styles.unitStack}>
      <FitText style={styles.unitText}>¥</FitText>
      <FitText style={styles.unitText}>USDT</FitText>
    </View>
  );
}

function AmountNumbers({ usdt, cny }: { usdt: number; cny: number }) {
  return (
    <View style={styles.moneyStack}>
      <FitText style={styles.moneyValue}>{formatTeamAmount(cny)}</FitText>
      <FitText style={styles.moneyValue}>{formatTeamAmount(usdt)}</FitText>
    </View>
  );
}

function SummaryMetric({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.metricCol}>
      <FitText style={styles.th}>{label}</FitText>
      <FitText style={styles.td}>{value}</FitText>
    </View>
  );
}

function AmountMetric({ label, usdt, cny }: { label: string; usdt: number; cny: number }) {
  return (
    <View style={styles.amountMetricCol}>
      <FitText style={[styles.th, styles.amountMetricLabel]}>{label}</FitText>
      <AmountNumbers usdt={usdt} cny={cny} />
    </View>
  );
}

function LevelTableRow({ label, row }: { label: string; row: TeamUiLevelRow }) {
  return (
    <View style={styles.levelRow}>
      <FitText style={[styles.td, styles.colLevelNo]}>{label}</FitText>
      <FitText style={[styles.td, styles.colCount]}>{formatTeamAmount(row.register)}</FitText>
      <FitText style={[styles.td, styles.colCount]}>{formatTeamAmount(row.active)}</FitText>
      <View style={styles.colUnit}>
        <UnitStack />
      </View>
      <View style={styles.colDual}>
        <AmountNumbers usdt={row.rechargeUsd} cny={row.rechargeCny} />
      </View>
      <View style={styles.colDual}>
        <AmountNumbers usdt={row.subscribeUsd} cny={row.subscribeCny} />
      </View>
    </View>
  );
}

function MemberRow({
  member,
}: {
  member: Pick<AppTeamMemberItem, 'name' | 'phone' | 'usd' | 'cny'>;
}) {
  return (
    <View style={styles.listRow}>
      <FitText style={[styles.td, styles.colName]}>{member.name}</FitText>
      <FitText style={[styles.td, styles.colPhone]}>{member.phone}</FitText>
      <View style={styles.colUnit}>
        <UnitStack />
      </View>
      <View style={styles.colMoney}>
        <AmountNumbers usdt={member.usd} cny={member.cny} />
      </View>
    </View>
  );
}

export default function TeamScreen() {
  const [level, setLevel] = useState<TeamLevelNo>(1);
  const [visibleCount, setVisibleCount] = useState(MEMBER_PAGE_SIZE);
  const [refreshing, setRefreshing] = useState(false);
  const [team, setTeam] = useState(emptyTeamView());
  const loadingMoreRef = useRef(false);

  const loadTeam = useCallback(async (silent = false) => {
    if (!silent) {
      setRefreshing(true);
    }
    try {
      const data = await fetchAppTeam();
      setTeam(data);
      setVisibleCount(MEMBER_PAGE_SIZE);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取团队数据失败');
      }
    } finally {
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void loadTeam(true);
    }, [loadTeam]),
  );

  const levelRows = useMemo(
    () => TEAM_LEVEL_NOS.map((n) => toLevelRow(team.summary[`level${n}`])),
    [team.summary],
  );

  const totals = useMemo(() => sumStats(levelRows), [levelRows]);

  const members = useMemo(() => team.members[level] ?? [], [level, team.members]);
  const visibleMembers = members.slice(0, visibleCount);
  const hasMore = visibleCount < members.length;

  const selectLevel = (next: TeamLevelNo) => {
    setLevel(next);
    setVisibleCount(MEMBER_PAGE_SIZE);
  };

  const loadMore = useCallback(() => {
    setVisibleCount((current) => {
      if (current >= members.length) {
        return current;
      }
      return Math.min(current + MEMBER_PAGE_SIZE, members.length);
    });
  }, [members.length]);

  const onScroll = useCallback(
    (e: NativeSyntheticEvent<NativeScrollEvent>) => {
      if (!hasMore || loadingMoreRef.current) {
        return;
      }
      const { layoutMeasurement, contentOffset, contentSize } = e.nativeEvent;
      const distance = contentSize.height - contentOffset.y - layoutMeasurement.height;
      if (distance < LOAD_MORE_DISTANCE) {
        loadingMoreRef.current = true;
        loadMore();
        setTimeout(() => {
          loadingMoreRef.current = false;
        }, 280);
      }
    },
    [hasMore, loadMore],
  );

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="我的团队" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        onRefresh={() => loadTeam()}
        onScroll={onScroll}
        scrollEventThrottle={16}
      >
        <View style={styles.topCard}>
          <SummaryMetric label="注册人数" value={formatTeamAmount(totals.register)} />
          <SummaryMetric label="激活人数" value={formatTeamAmount(totals.active)} />
          <View style={styles.topUnitCol}>
            <View style={styles.amountUnitSpacer} />
            <UnitStack />
          </View>
          <AmountMetric
            label="充值金额"
            usdt={totals.rechargeUsd}
            cny={totals.rechargeCny}
          />
          <AmountMetric
            label="认购金额"
            usdt={totals.subscribeUsd}
            cny={totals.subscribeCny}
          />
        </View>

        <View style={styles.tableCard}>
          <View style={styles.levelHeadRow}>
            <FitText style={[styles.th, styles.colLevelNo]}>级别</FitText>
            <FitText style={[styles.th, styles.colCount]}>注册人数</FitText>
            <FitText style={[styles.th, styles.colCount]}>激活人数</FitText>
            <View style={styles.colUnit} />
            <FitText style={[styles.th, styles.colDualHead]}>充值金额</FitText>
            <FitText style={[styles.th, styles.colDualHead]}>认购金额</FitText>
          </View>
          {TEAM_LEVEL_LABELS.map((label, index) => (
            <LevelTableRow key={label} label={label} row={levelRows[index]} />
          ))}
        </View>

        <View style={[styles.tableCard, styles.listCard]}>
          <View style={styles.tabs}>
            {TEAM_LEVELS.map((n) => {
              const active = level === n;
              return (
                <Pressable key={n} onPress={() => selectLevel(n)} style={styles.tab}>
                  <Text style={[styles.tabText, active && styles.tabTextActive]}>
                    {TEAM_TAB_LABELS[n - 1]}
                  </Text>
                  <View style={[styles.tabBar, active && styles.tabBarActive]} />
                </Pressable>
              );
            })}
          </View>

          <View style={styles.listHead}>
            <FitText style={[styles.th, styles.colName]}>姓名</FitText>
            <FitText style={[styles.th, styles.colPhone]}>电话</FitText>
            <View style={styles.colUnit} />
            <FitText style={[styles.th, styles.colMoneyHead]}>累计充值</FitText>
          </View>

          {members.length === 0 ? (
            <Text style={styles.emptyText}>暂无团队成员</Text>
          ) : (
            <>
              {visibleMembers.map((member, index) => {
                const key =
                  'memberId' in member && member.memberId != null
                    ? String(member.memberId)
                    : `${member.phone}-${index}`;
                return <MemberRow key={key} member={member} />;
              })}
              {hasMore ? (
                <Pressable onPress={loadMore} style={styles.moreBtn}>
                  <Text style={styles.moreText}>
                    加载更多（{visibleMembers.length}/{members.length}）
                  </Text>
                </Pressable>
              ) : members.length > MEMBER_PAGE_SIZE ? (
                <Text style={styles.moreDone}>已全部加载 {members.length} 人</Text>
              ) : null}
            </>
          )}
        </View>

        {refreshing ? (
          <View style={styles.refreshHint}>
            <ActivityIndicator color={colors.accent} size="small" />
          </View>
        ) : null}
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 14,
  },
  topCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    backgroundColor: 'rgba(18, 36, 78, 0.92)',
    paddingHorizontal: 8,
    paddingVertical: 16,
  },
  topUnitCol: {
    width: 46,
    alignItems: 'flex-end',
    paddingRight: 4,
    gap: 8,
  },
  amountUnitSpacer: {
    height: 16,
  },
  amountMetricCol: {
    flex: 1,
    minWidth: 0,
    alignItems: 'flex-start',
    gap: 8,
  },
  amountMetricLabel: {
    textAlign: 'left',
  },
  metricCol: {
    flex: 1,
    minWidth: 0,
    alignItems: 'center',
    gap: 8,
  },
  tableCard: {
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    backgroundColor: 'rgba(18, 36, 78, 0.92)',
    paddingHorizontal: 10,
    paddingTop: 12,
    paddingBottom: 8,
  },
  listCard: {
    paddingTop: 10,
  },
  levelHeadRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingBottom: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.35)',
  },
  levelRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.22)',
  },
  th: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 11,
    textAlign: 'center',
    lineHeight: 16,
  },
  td: {
    color: colors.text,
    fontSize: 13,
    textAlign: 'center',
  },
  colLevelNo: {
    width: '10%',
  },
  colCount: {
    width: '15%',
  },
  colUnit: {
    width: 46,
    alignItems: 'flex-end',
    paddingRight: 4,
    paddingTop: 1,
  },
  colDual: {
    flex: 1,
    minWidth: 0,
    alignItems: 'flex-start',
    paddingTop: 1,
  },
  colDualHead: {
    flex: 1,
    minWidth: 0,
    textAlign: 'left',
  },
  tabs: {
    flexDirection: 'row',
    marginBottom: 10,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
  },
  tabText: {
    color: 'rgba(210, 225, 255, 0.85)',
    fontSize: 13,
  },
  tabTextActive: {
    color: colors.text,
    fontWeight: '600',
  },
  tabBar: {
    marginTop: 8,
    width: 24,
    height: 3,
    borderRadius: 2,
    backgroundColor: 'transparent',
  },
  tabBarActive: {
    backgroundColor: '#FF2A2A',
  },
  listHead: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.22)',
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.15)',
  },
  colName: {
    flex: 0.9,
    minWidth: 0,
  },
  colPhone: {
    flex: 1.2,
    minWidth: 0,
  },
  colMoney: {
    flex: 1.2,
    minWidth: 0,
    alignItems: 'flex-start',
  },
  colMoneyHead: {
    flex: 1.2,
    minWidth: 0,
    textAlign: 'left',
  },
  unitStack: {
    width: '100%',
    gap: 2,
    alignItems: 'flex-end',
  },
  unitText: {
    width: '100%',
    color: 'rgba(180, 200, 230, 0.9)',
    fontSize: 11,
    fontWeight: '600',
    lineHeight: 18,
    textAlign: 'right',
  },
  moneyStack: {
    width: '100%',
    gap: 2,
  },
  moneyValue: {
    width: '100%',
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
    lineHeight: 18,
    textAlign: 'left',
  },
  emptyText: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 14,
    textAlign: 'center',
    paddingVertical: 24,
  },
  moreBtn: {
    alignItems: 'center',
    paddingVertical: 14,
  },
  moreText: {
    color: colors.accent,
    fontSize: 13,
  },
  moreDone: {
    color: 'rgba(180, 200, 230, 0.7)',
    fontSize: 12,
    textAlign: 'center',
    paddingVertical: 12,
  },
  refreshHint: {
    alignItems: 'center',
    paddingTop: 8,
  },
});
