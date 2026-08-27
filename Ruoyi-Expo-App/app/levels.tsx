import { useFocusEffect, useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { displayText } from '@/api/app-auth';
import { emptyLevelsView, fetchAppLevelsView } from '@/api/app-member';
import { formatTeamAmount } from '@/api/app-team';
import { ApiError } from '@/api/request';
import type { AppLevel } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { useAuth } from '@/hooks/useAuth';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

type DisplayLevelRow = {
  levelId: number;
  levelName: string;
  teamDepth: string;
  minRechargeCny: number;
  minRechargeUsdt: number;
  minTeamPerfCny: number;
  minTeamPerfUsdt: number;
  teamRewardCny: number;
  teamRewardUsdt: number;
};

function toNumberOrZero(value?: number | null): number {
  return value !== undefined && value !== null && Number.isFinite(value) ? value : 0;
}

function mapLevelRows(apiLevels: AppLevel[]): DisplayLevelRow[] {
  return apiLevels.map((apiLevel) => ({
    levelId: apiLevel.levelId,
    levelName: apiLevel.levelName?.trim() || `等级${apiLevel.levelId}`,
    teamDepth: apiLevel.teamDepth?.trim() || '',
    minRechargeCny: toNumberOrZero(apiLevel.minRechargeCny),
    minRechargeUsdt: toNumberOrZero(apiLevel.minRechargeUsdt),
    minTeamPerfCny: toNumberOrZero(apiLevel.minTeamPerfCny),
    minTeamPerfUsdt: toNumberOrZero(apiLevel.minTeamPerfUsdt),
    teamRewardCny: toNumberOrZero(apiLevel.teamRewardCny),
    teamRewardUsdt: toNumberOrZero(apiLevel.teamRewardUsdt),
  }));
}

function formatAmountLine(value?: number | null): string {
  return formatTeamAmount(toNumberOrZero(value));
}

function TableCurrencyUnit() {
  return (
    <View style={styles.tableCurrencyUnit}>
      <Text style={styles.tableUnitLine} numberOfLines={1}>
        ¥
      </Text>
      <Text style={styles.tableUnitLine} numberOfLines={1}>
        USDT
      </Text>
    </View>
  );
}

function TableDualAmount({ cny, usdt }: { cny?: number; usdt?: number }) {
  return (
    <View style={styles.tableDualAmount}>
      <Text style={styles.tableAmountLine} numberOfLines={1}>
        {formatAmountLine(cny)}
      </Text>
      <Text style={styles.tableAmountLine} numberOfLines={1}>
        {formatAmountLine(usdt)}
      </Text>
    </View>
  );
}

function RulesModal({
  visible,
  ruleText,
  onClose,
}: {
  visible: boolean;
  ruleText: string;
  onClose: () => void;
}) {
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <Pressable style={styles.modalMask} onPress={onClose}>
        <Pressable style={styles.modalCard} onPress={() => {}}>
          <View style={styles.modalHead}>
            <Text style={styles.modalTitle}>规则说明</Text>
            <Pressable onPress={onClose} hitSlop={12} style={styles.modalClose}>
              <Text style={styles.modalCloseText}>×</Text>
            </Pressable>
          </View>
          <Text style={styles.modalRuleText}>{ruleText.trim() || '暂无规则说明'}</Text>
        </Pressable>
      </Pressable>
    </Modal>
  );
}

function LevelTableRow({
  row,
  current,
}: {
  row: DisplayLevelRow;
  current: boolean;
}) {
  return (
    <View style={[styles.tableRow, current && styles.tableRowCurrent]}>
      <Text style={[styles.cellLevel, styles.colLevel]} numberOfLines={1}>
        {row.levelName}
      </Text>
      <Text style={[styles.cellText, styles.colDepth]} numberOfLines={1}>
        {row.teamDepth || '—'}
      </Text>
      <View style={styles.colUnit}>
        <TableCurrencyUnit />
      </View>
      <View style={styles.colRecharge}>
        <TableDualAmount cny={row.minRechargeCny} usdt={row.minRechargeUsdt} />
      </View>
      <View style={styles.colReward}>
        <TableDualAmount cny={row.teamRewardCny} usdt={row.teamRewardUsdt} />
      </View>
    </View>
  );
}

export default function LevelsScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { user } = useAuth();
  const [refreshing, setRefreshing] = useState(false);
  const [rulesVisible, setRulesVisible] = useState(false);
  const [levelsView, setLevelsView] = useState(emptyLevelsView());

  const load = useCallback(async (silent = false) => {
    if (!silent) {
      setRefreshing(true);
    }
    try {
      const levelsData = await fetchAppLevelsView();
      setLevelsView(levelsData);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取会员等级失败');
      }
    } finally {
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load(true);
    }, [load]),
  );

  const displayRows = useMemo(() => mapLevelRows(levelsView.levels), [levelsView.levels]);

  const currentLevelId = levelsView.current.levelId ?? user?.levelId;
  const currentLevelName = useMemo(() => {
    const fromApi = levelsView.current.levelName?.trim();
    if (fromApi) {
      return fromApi;
    }
    const fromProfile = user?.levelName?.trim();
    if (fromProfile) {
      return fromProfile;
    }
    if (currentLevelId !== undefined) {
      const matched = displayRows.find((row) => row.levelId === currentLevelId);
      if (matched?.levelName) {
        return matched.levelName;
      }
    }
    return '';
  }, [levelsView.current.levelName, user?.levelName, currentLevelId, displayRows]);

  const currentTeamReward = useMemo(() => {
    const matched =
      currentLevelId !== undefined
        ? displayRows.find((row) => row.levelId === currentLevelId)
        : displayRows.find((row) => row.levelName === currentLevelName);
    return {
      cny: matched?.teamRewardCny ?? 0,
      usdt: matched?.teamRewardUsdt ?? 0,
    };
  }, [currentLevelId, currentLevelName, displayRows]);

  return (
    <AppBackground source={images.levelBg} dim={false} contentPosition="top">
      <View style={[styles.header, { paddingTop: insets.top + 6 }]}>
        <View style={styles.headerLeft}>
          <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
            <Text style={styles.back}>‹</Text>
          </Pressable>
        </View>
        <View style={styles.headerTitleWrap} pointerEvents="none">
          <Text style={styles.headerTitle}>会员等级</Text>
        </View>
        <Pressable
          style={styles.headerRight}
          hitSlop={8}
          onPress={() => setRulesVisible(true)}
        >
          <View style={styles.rulesBtn}>
            <View style={styles.rulesIcon}>
              <Text style={styles.rulesIconText}>?</Text>
            </View>
            <Text style={styles.rulesText}>规则说明</Text>
          </View>
        </Pressable>
      </View>

      <RefreshableScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
        onRefresh={() => load()}
      >
        <View style={styles.hero}>
          <Image source={images.levelTrophy} style={styles.trophy} contentFit="contain" />
          <View style={styles.heroTextWrap}>
            <Text style={styles.heroLineWhite}>等级越高</Text>
            <Text style={styles.heroLineGold}>奖励越丰厚!</Text>
          </View>
        </View>

        <View style={styles.statusCard}>
          <View style={styles.statusCol}>
            <Text style={styles.statusLabel}>当前团队等级</Text>
            <Text style={styles.statusLevelValue}>{displayText(currentLevelName)}</Text>
          </View>
          <View style={styles.statusCol}>
            <Text style={styles.statusLabel}>团队奖励</Text>
            <Text style={styles.statusMoneyLine}>¥ {formatAmountLine(currentTeamReward.cny)}</Text>
            <Text style={styles.statusMoneyLine}>USDT {formatAmountLine(currentTeamReward.usdt)}</Text>
          </View>
        </View>

        {levelsView.hint?.trim() ? (
          <Text style={styles.note}>{levelsView.hint.trim()}</Text>
        ) : null}

        <View style={styles.tableCard}>
          <View style={styles.tableHead}>
            <Text style={[styles.headText, styles.colLevel]}>会员等级</Text>
            <Text style={[styles.headText, styles.colDepth]}>团队要求</Text>
            <View style={styles.colUnit} />
            <Text style={[styles.headText, styles.colRechargeHead]}>充值金额</Text>
            <Text style={[styles.headText, styles.colRewardHead]}>团队奖励</Text>
          </View>

          {displayRows.length === 0 ? (
            <Text style={styles.emptyText}>暂无等级数据</Text>
          ) : (
            displayRows.map((row) => (
              <LevelTableRow
                key={row.levelId}
                row={row}
                current={currentLevelId !== undefined && currentLevelId === row.levelId}
              />
            ))
          )}
        </View>

        {refreshing ? (
          <View style={styles.refreshHint}>
            <ActivityIndicator color={colors.accent} size="small" />
          </View>
        ) : null}
      </RefreshableScrollView>

      <RulesModal
        visible={rulesVisible}
        ruleText={levelsView.ruleText || ''}
        onClose={() => setRulesVisible(false)}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  header: {
    position: 'relative',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
    paddingBottom: 10,
    minHeight: 44,
  },
  headerLeft: {
    zIndex: 1,
    minWidth: 44,
    alignItems: 'flex-start',
    justifyContent: 'center',
  },
  headerRight: {
    zIndex: 1,
    minWidth: 44,
    alignItems: 'flex-end',
    justifyContent: 'center',
  },
  backBtn: {
    width: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  back: {
    color: colors.text,
    fontSize: 32,
    lineHeight: 34,
    fontWeight: '300',
  },
  headerTitleWrap: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitle: {
    textAlign: 'center',
    color: colors.text,
    fontSize: 18,
    fontWeight: '600',
  },
  rulesBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingRight: 2,
  },
  rulesText: {
    color: colors.text,
    fontSize: 12,
    fontWeight: '500',
  },
  rulesIcon: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.85)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  rulesIconText: {
    color: colors.text,
    fontSize: 11,
    lineHeight: 12,
    fontWeight: '700',
    marginTop: -1,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  hero: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
    marginBottom: 16,
    paddingHorizontal: 2,
  },
  trophy: {
    width: 132,
    height: 132,
  },
  heroTextWrap: {
    flex: 1,
    paddingLeft: 6,
    justifyContent: 'center',
  },
  heroLineWhite: {
    color: colors.text,
    fontSize: 21,
    fontWeight: '700',
    lineHeight: 30,
  },
  heroLineGold: {
    color: colors.gold,
    fontSize: 21,
    fontWeight: '800',
    lineHeight: 30,
    textShadowColor: 'rgba(232, 195, 106, 0.4)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 8,
  },
  statusCard: {
    flexDirection: 'row',
    alignItems: 'stretch',
    gap: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.35)',
    backgroundColor: 'rgba(12, 24, 52, 0.82)',
    paddingHorizontal: 14,
    paddingVertical: 16,
  },
  statusCol: {
    flex: 1,
    justifyContent: 'center',
  },
  statusLabel: {
    color: 'rgba(180, 198, 228, 0.85)',
    fontSize: 13,
    fontWeight: '500',
    marginBottom: 10,
  },
  statusLevelValue: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '700',
  },
  statusMoneyLine: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
    lineHeight: 22,
  },
  note: {
    color: 'rgba(180, 198, 228, 0.78)',
    fontSize: 12,
    lineHeight: 18,
    marginTop: 12,
    marginBottom: 14,
  },
  tableCard: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.35)',
    backgroundColor: 'rgba(12, 24, 52, 0.82)',
    paddingHorizontal: 10,
    paddingTop: 12,
    paddingBottom: 6,
  },
  tableHead: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingBottom: 10,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.35)',
  },
  headText: {
    color: 'rgba(220, 232, 255, 0.95)',
    fontSize: 12,
    fontWeight: '700',
  },
  tableRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingVertical: 11,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.22)',
  },
  tableRowCurrent: {
    backgroundColor: 'rgba(232, 195, 106, 0.06)',
  },
  cellLevel: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '700',
  },
  cellText: {
    color: colors.text,
    fontSize: 12,
  },
  colLevel: {
    width: '20%',
  },
  colDepth: {
    width: '16%',
  },
  colUnit: {
    width: '14%',
    alignItems: 'flex-end',
    paddingRight: 6,
    paddingTop: 1,
  },
  colRecharge: {
    width: '25%',
    alignItems: 'flex-end',
    paddingRight: 8,
    paddingTop: 1,
  },
  colReward: {
    width: '25%',
    alignItems: 'flex-end',
    paddingTop: 1,
  },
  colRechargeHead: {
    width: '25%',
    textAlign: 'right',
    paddingRight: 8,
  },
  colRewardHead: {
    width: '25%',
    textAlign: 'right',
  },
  tableCurrencyUnit: {
    alignItems: 'flex-end',
    gap: 2,
  },
  tableUnitLine: {
    color: 'rgba(200, 215, 245, 0.75)',
    fontSize: 12,
    fontWeight: '600',
    lineHeight: 18,
  },
  tableDualAmount: {
    alignItems: 'flex-end',
    gap: 2,
  },
  tableAmountLine: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '700',
    lineHeight: 18,
  },
  refreshHint: {
    alignItems: 'center',
    paddingTop: 16,
  },
  modalMask: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  modalCard: {
    borderRadius: 14,
    backgroundColor: '#0E172A',
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    paddingHorizontal: 18,
    paddingTop: 16,
    paddingBottom: 20,
  },
  modalHead: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 14,
  },
  modalTitle: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
  },
  modalClose: {
    position: 'absolute',
    right: 0,
    top: -2,
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalCloseText: {
    color: colors.text,
    fontSize: 24,
    lineHeight: 24,
    fontWeight: '300',
  },
  modalRuleText: {
    color: colors.text,
    fontSize: 13,
    lineHeight: 22,
  },
  emptyText: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 14,
    textAlign: 'center',
    paddingVertical: 24,
  },
});
