import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
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
      recharge: acc.recharge + row.rechargeCny,
      subscribe: acc.subscribe + row.subscribeCny,
    }),
    { register: 0, active: 0, recharge: 0, subscribe: 0 },
  );
}

function DualValue({ top, bottom }: { top: number; bottom: number }) {
  return (
    <View style={styles.dualValue}>
      <Text style={styles.dualLine}>{formatTeamAmount(top)}</Text>
      <Text style={styles.dualLine}>{formatTeamAmount(bottom)}</Text>
    </View>
  );
}

function SummaryMetric({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.metricCol}>
      <Text style={styles.metricLabel}>{label}</Text>
      <Text style={styles.metricValue}>{value}</Text>
    </View>
  );
}

function LevelTableRow({ label, row }: { label: string; row: TeamUiLevelRow }) {
  return (
    <View style={styles.levelRow}>
      <Text style={[styles.td, styles.colLevelNo]}>{label}</Text>
      <Text style={[styles.td, styles.colCount]}>{formatTeamAmount(row.register)}</Text>
      <Text style={[styles.td, styles.colCount]}>{formatTeamAmount(row.active)}</Text>
      <View style={styles.colDual}>
        <DualValue top={row.rechargeCny} bottom={row.rechargeUsd} />
      </View>
      <View style={styles.colDual}>
        <DualValue top={row.subscribeCny} bottom={row.subscribeUsd} />
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
      <Text style={[styles.td, styles.colName]} numberOfLines={1}>
        {member.name}
      </Text>
      <Text style={[styles.td, styles.colPhone]} numberOfLines={1}>
        {member.phone}
      </Text>
      <View style={[styles.colMoney, styles.moneyCell]}>
        <View style={styles.moneyStack}>
          <View style={styles.moneyLine}>
            <Text style={styles.moneyLabel}>USDT</Text>
            <Text style={styles.moneyValue}>{formatTeamAmount(member.usd)}</Text>
          </View>
          <View style={styles.moneyLine}>
            <Text style={styles.moneyLabel}>¥</Text>
            <Text style={styles.moneyValue}>{formatTeamAmount(member.cny)}</Text>
          </View>
        </View>
      </View>
    </View>
  );
}

export default function TeamScreen() {
  const [level, setLevel] = useState<TeamLevelNo>(1);
  const [refreshing, setRefreshing] = useState(false);
  const [team, setTeam] = useState(emptyTeamView());

  const loadTeam = useCallback(async (silent = false) => {
    if (!silent) {
      setRefreshing(true);
    }
    try {
      const data = await fetchAppTeam();
      setTeam(data);
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

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="我的团队" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        onRefresh={() => loadTeam()}
      >
        <View style={styles.topCard}>
          <SummaryMetric label="注册人数" value={formatTeamAmount(totals.register)} />
          <SummaryMetric label="激活人数" value={formatTeamAmount(totals.active)} />
          <SummaryMetric label="充值金额" value={formatTeamAmount(totals.recharge)} />
          <SummaryMetric label="认购金额" value={formatTeamAmount(totals.subscribe)} />
        </View>

        <View style={styles.tableCard}>
          <View style={styles.levelHeadRow}>
            <Text style={[styles.th, styles.colLevelNo]}>级别</Text>
            <Text style={[styles.th, styles.colCount]}>注册人数</Text>
            <Text style={[styles.th, styles.colCount]}>激活人数</Text>
            <Text style={[styles.th, styles.colDualHead]}>{'充值金额\n¥/USDT'}</Text>
            <Text style={[styles.th, styles.colDualHead]}>{'认购金额\n¥/USDT'}</Text>
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
                <Pressable key={n} onPress={() => setLevel(n)} style={styles.tab}>
                  <Text style={[styles.tabText, active && styles.tabTextActive]}>
                    {TEAM_TAB_LABELS[n - 1]}
                  </Text>
                  <View style={[styles.tabBar, active && styles.tabBarActive]} />
                </Pressable>
              );
            })}
          </View>

          <View style={styles.listHead}>
            <Text style={[styles.th, styles.colName]}>姓名</Text>
            <Text style={[styles.th, styles.colPhone]}>电话</Text>
            <Text style={[styles.th, styles.colMoney]}>累计充值</Text>
          </View>

          {members.length === 0 ? (
            <Text style={styles.emptyText}>暂无团队成员</Text>
          ) : (
            members.map((member, index) => {
              const key =
                'memberId' in member && member.memberId != null
                  ? String(member.memberId)
                  : `${member.phone}-${index}`;
              return <MemberRow key={key} member={member} />;
            })
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
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    backgroundColor: 'rgba(18, 36, 78, 0.92)',
    paddingHorizontal: 8,
    paddingVertical: 16,
  },
  metricCol: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  metricLabel: {
    color: 'rgba(180, 200, 230, 0.85)',
    fontSize: 12,
    textAlign: 'center',
  },
  metricValue: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    textAlign: 'center',
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
    width: '16%',
  },
  colDual: {
    width: '29%',
    alignItems: 'center',
    paddingTop: 1,
  },
  colDualHead: {
    width: '29%',
    textAlign: 'center',
  },
  dualValue: {
    alignItems: 'center',
    gap: 2,
  },
  dualLine: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
    lineHeight: 18,
    textAlign: 'center',
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
  },
  colPhone: {
    flex: 1.2,
  },
  colMoney: {
    flex: 1.2,
  },
  moneyCell: {
    alignItems: 'center',
  },
  moneyStack: {
    gap: 2,
  },
  moneyLine: {
    flexDirection: 'row',
    alignItems: 'baseline',
    gap: 4,
  },
  moneyLabel: {
    width: 40,
    color: colors.text,
    fontSize: 13,
    fontWeight: '600',
    lineHeight: 18,
    textAlign: 'right',
  },
  moneyValue: {
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
  refreshHint: {
    alignItems: 'center',
    paddingTop: 8,
  },
});
