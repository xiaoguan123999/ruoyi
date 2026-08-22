import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { emptyTeamView, fetchAppTeam, formatTeamAmount } from '@/api/app-team';
import { ApiError } from '@/api/request';
import type { AppTeamMemberItem } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function TeamScreen() {
  const [level, setLevel] = useState<1 | 2 | 3>(1);
  const [loading, setLoading] = useState(true);
  const [team, setTeam] = useState(emptyTeamView());

  const loadTeam = useCallback(async () => {
    try {
      const data = await fetchAppTeam();
      setTeam(data);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取团队数据失败');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void loadTeam();
    }, [loadTeam]),
  );

  const members = team.members[level];
  const summary = team.summary;

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="我的团队" />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.content}
          onRefresh={loadTeam}
        >
          <View style={styles.card}>
            <View style={styles.summaryHead}>
              <View style={styles.labelSpacer} />
              <View style={styles.unitCol} />
              <Text style={styles.levelHead}>一级</Text>
              <Text style={styles.levelHead}>二级</Text>
              <Text style={styles.levelHead}>三级</Text>
            </View>

            <SummaryRow
              label="注册人数"
              values={[summary.level1.register, summary.level2.register, summary.level3.register]}
            />
            <SummaryRow
              label="激活人数"
              values={[summary.level1.active, summary.level2.active, summary.level3.active]}
            />
            <MoneySummaryRow
              label="认购金额"
              values={[
                { usd: summary.level1.subscribeUsd, cny: summary.level1.subscribeCny },
                { usd: summary.level2.subscribeUsd, cny: summary.level2.subscribeCny },
                { usd: summary.level3.subscribeUsd, cny: summary.level3.subscribeCny },
              ]}
            />
            <MoneySummaryRow
              label="充值金额"
              values={[
                { usd: summary.level1.rechargeUsd, cny: summary.level1.rechargeCny },
                { usd: summary.level2.rechargeUsd, cny: summary.level2.rechargeCny },
                { usd: summary.level3.rechargeUsd, cny: summary.level3.rechargeCny },
              ]}
            />
          </View>

          <View style={[styles.card, styles.listCard]}>
            <View style={styles.tabs}>
              {([1, 2, 3] as const).map((n) => {
                const active = level === n;
                return (
                  <Pressable key={n} onPress={() => setLevel(n)} style={styles.tab}>
                    <Text style={styles.tabText}>{['', '一级', '二级', '三级'][n]}</Text>
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
              members.map((member, index) => (
                <MemberRow
                  key={member.memberId ?? `${member.phone}-${index}`}
                  member={member}
                />
              ))
            )}
          </View>
        </RefreshableScrollView>
      )}
    </AppBackground>
  );
}

function MemberRow({ member }: { member: AppTeamMemberItem }) {
  return (
    <View style={styles.listRow}>
      <Text style={[styles.td, styles.colName]} numberOfLines={1}>
        {member.name}
      </Text>
      <Text style={[styles.td, styles.colPhone]} numberOfLines={1}>
        {member.phone}
      </Text>
      <View style={[styles.colMoney, styles.moneyCell]}>
        <Text style={styles.moneyLine}>¥ {formatTeamAmount(member.cny)}</Text>
        <Text style={styles.moneyLine}>USDT {formatTeamAmount(member.usd)}</Text>
      </View>
    </View>
  );
}

function SummaryRow({ label, values }: { label: string; values: number[] }) {
  return (
    <View style={styles.summaryRow}>
      <View style={styles.labelBox}>
        <Text style={styles.label}>{label}</Text>
      </View>
      <View style={styles.unitCol} />
      {values.map((value, index) => (
        <View key={`${label}-${index}`} style={styles.valueCell}>
          <Text style={styles.valueText}>{value}</Text>
        </View>
      ))}
    </View>
  );
}

/** 左侧只显示一次 ¥ / USDT，一级二三级只显示数值 */
function MoneySummaryRow({
  label,
  values,
}: {
  label: string;
  values: Array<{ usd: number; cny: number }>;
}) {
  return (
    <View style={[styles.summaryRow, styles.summaryRowMoney]}>
      <View style={[styles.labelBox, styles.labelBoxMoney]}>
        <Text style={styles.label}>{label}</Text>
      </View>
      <View style={styles.moneyBody}>
        <View style={styles.moneyValueRow}>
          <Text style={styles.moneyUnit}>¥</Text>
          {values.map((value, index) => (
            <Text key={`${label}-cny-${index}`} style={styles.moneyAmount}>
              {formatTeamAmount(value.cny ?? 0)}
            </Text>
          ))}
        </View>
        <View style={styles.moneyValueRow}>
          <Text style={styles.moneyUnit}>USDT</Text>
          {values.map((value, index) => (
            <Text key={`${label}-usd-${index}`} style={styles.moneyAmount}>
              {formatTeamAmount(value.usd ?? 0)}
            </Text>
          ))}
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingBottom: 80,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 14,
  },
  card: {
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    backgroundColor: 'rgba(18, 36, 78, 0.92)',
    paddingHorizontal: 12,
    paddingTop: 14,
    paddingBottom: 12,
  },
  listCard: {
    paddingTop: 10,
  },
  summaryHead: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  labelSpacer: {
    width: 78,
  },
  unitCol: {
    width: 40,
    marginRight: 2,
  },
  levelHead: {
    flex: 1,
    color: colors.text,
    textAlign: 'left',
    fontSize: 14,
    fontWeight: '600',
    paddingLeft: 2,
  },
  summaryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 10,
  },
  summaryRowMoney: {
    alignItems: 'center',
  },
  labelBox: {
    width: 78,
    height: 28,
    borderWidth: 1,
    borderColor: 'rgba(120, 185, 255, 0.7)',
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  labelBoxMoney: {
    height: 40,
  },
  label: {
    color: colors.text,
    fontSize: 12,
  },
  valueCell: {
    flex: 1,
    alignItems: 'flex-start',
    justifyContent: 'center',
    minHeight: 28,
    paddingLeft: 2,
  },
  valueText: {
    color: colors.text,
    fontSize: 14,
  },
  moneyBody: {
    flex: 1,
  },
  moneyValueRow: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 18,
  },
  moneyUnit: {
    width: 40,
    marginRight: 2,
    color: colors.text,
    fontSize: 12,
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 18,
  },
  moneyAmount: {
    flex: 1,
    color: colors.text,
    fontSize: 13,
    lineHeight: 18,
    textAlign: 'left',
    paddingLeft: 2,
  },
  moneyLine: {
    color: colors.text,
    fontSize: 13,
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
    color: colors.text,
    fontSize: 15,
  },
  tabBar: {
    marginTop: 8,
    width: 28,
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
  },
  listRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
  },
  th: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 13,
    textAlign: 'center',
  },
  td: {
    color: colors.text,
    fontSize: 14,
    textAlign: 'center',
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
  emptyText: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 14,
    textAlign: 'center',
    paddingVertical: 24,
  },
});
