import { Image } from 'expo-image';
import { useFocusEffect, useRouter } from 'expo-router';
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
import {
  claimAppLevelReward,
  emptyLevelsView,
  fetchAppLevelRewardClaimable,
  fetchAppLevelsView,
} from '@/api/app-member';
import {
  emptyTeamView,
  fetchAppTeam,
  formatTeamAmount,
  sumTeamRecharge,
} from '@/api/app-team';
import { ApiError } from '@/api/request';
import type { AppLevel, AppLevelRewardClaimableItem, KycRewardCurrency } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { LevelRewardModal } from '@/components/ui/LevelRewardModal';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { useAuth } from '@/hooks/useAuth';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess } from '@/utils/toast';

type DisplayLevelRow = {
  levelId: number;
  levelName: string;
  teamDepth: string;
  minTeamRechargeCny: number;
  minTeamRechargeUsdt: number;
  rewardCny: number;
  rewardUsdt: number;
};

function toNumberOrZero(value?: number | null): number {
  return value !== undefined && value !== null && Number.isFinite(value) ? value : 0;
}

function mapLevelRows(apiLevels: AppLevel[]): DisplayLevelRow[] {
  return apiLevels.map((apiLevel) => ({
    levelId: apiLevel.levelId,
    levelName: apiLevel.levelName?.trim() || `等级${apiLevel.levelId}`,
    teamDepth: apiLevel.teamDepth?.trim() || '',
    minTeamRechargeCny: toNumberOrZero(apiLevel.minTeamRechargeCny),
    minTeamRechargeUsdt: toNumberOrZero(apiLevel.minTeamRechargeUsdt),
    rewardCny: toNumberOrZero(apiLevel.rewardCny),
    rewardUsdt: toNumberOrZero(apiLevel.rewardUsdt),
  }));
}

function formatAmountLine(value?: number | null): string {
  return formatTeamAmount(toNumberOrZero(value));
}

function TableCurrencyUnit() {
  return (
    <View style={styles.tableCurrencyUnit}>
      <Text style={[styles.tableAmountLine, styles.tableUnitText]} numberOfLines={1}>
        ¥
      </Text>
      <Text style={[styles.tableAmountLine, styles.tableUnitText]} numberOfLines={1}>
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
      <View style={styles.colMoneyGroup}>
        <TableCurrencyUnit />
        <View style={styles.colMoney}>
          <TableDualAmount cny={row.minTeamRechargeCny} usdt={row.minTeamRechargeUsdt} />
        </View>
        <View style={styles.colMoney}>
          <TableDualAmount cny={row.rewardCny} usdt={row.rewardUsdt} />
        </View>
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
  const [teamRecharge, setTeamRecharge] = useState({ cny: 0, usdt: 0 });
  const [claimItem, setClaimItem] = useState<AppLevelRewardClaimableItem | null>(null);
  const [claimSubmitting, setClaimSubmitting] = useState(false);

  const load = useCallback(async (silent = false) => {
    if (!silent) {
      setRefreshing(true);
    }
    let claimableItems: AppLevelRewardClaimableItem[] = [];
    try {
      claimableItems = await fetchAppLevelRewardClaimable();
    } catch (error) {
      if (error instanceof ApiError && error.code === 401) {
        setRefreshing(false);
        return;
      }
    }
    try {
      const [levelsData, team] = await Promise.all([
        fetchAppLevelsView(),
        fetchAppTeam().catch(() => emptyTeamView()),
      ]);
      setLevelsView({
        ...levelsData,
        claimable: claimableItems,
      });
      setTeamRecharge(sumTeamRecharge(team.summary));
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

  const claimable = levelsView.claimable;

  const submitClaim = useCallback(
    async (levelId: number, currency: KycRewardCurrency) => {
      setClaimSubmitting(true);
      try {
        const result = await claimAppLevelReward(levelId, currency);
        setClaimItem(null);
        modalSuccess(result.message || '领取成功，已到账');
        await load(true);
      } catch (error) {
        if (!(error instanceof ApiError) || error.code !== 401) {
          modalError(error instanceof ApiError ? error.message : '领取失败');
        }
      } finally {
        setClaimSubmitting(false);
      }
    },
    [load],
  );

  const onPressClaim = (item: AppLevelRewardClaimableItem) => {
    if (claimSubmitting) {
      return;
    }
    if (item.options.length === 1) {
      void submitClaim(item.levelId, item.options[0].currency);
      return;
    }
    setClaimItem(item);
  };

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
        refreshing={refreshing}
        onRefresh={() => load(false)}
      >
        <View style={styles.hero}>
          <Image source={images.levelTrophy} style={styles.trophy} contentFit="contain" />
          <View style={styles.heroTextWrap}>
            <Text style={styles.heroLineWhite}>等级越高</Text>
            <Text style={styles.heroLineGold}>奖励越丰厚!</Text>
          </View>
        </View>

        <View style={styles.statusCard}>
          <View style={styles.statusRow}>
            <View style={styles.statusCol}>
              <Text style={styles.statusLabel}>当前团队等级</Text>
              <Text style={styles.statusLevelValue}>{displayText(currentLevelName)}</Text>
            </View>
            <View style={styles.statusCol}>
              <Text style={styles.statusLabel}>团队充值金额</Text>
              <View style={styles.statusMoneyPair}>
                <View style={styles.statusUnitHang}>
                  <TableCurrencyUnit />
                </View>
                <TableDualAmount cny={teamRecharge.cny} usdt={teamRecharge.usdt} />
              </View>
            </View>
          </View>
          {claimable.map((item) => (
            <Pressable
              key={item.levelId}
              style={styles.claimEntry}
              disabled={claimSubmitting}
              onPress={() => onPressClaim(item)}
            >
              <Text style={styles.claimEntryText}>
                {claimSubmitting ? '领取中…' : '等级奖励待领取，点击领取'}
              </Text>
            </Pressable>
          ))}
        </View>

        {levelsView.hint?.trim() ? (
          <Text style={styles.note}>{levelsView.hint.trim()}</Text>
        ) : null}

        <View style={styles.tableCard}>
          <View style={styles.tableHead}>
            <Text style={[styles.headText, styles.colLevel]}>会员等级</Text>
            <Text style={[styles.headText, styles.colDepth]}>团队要求</Text>
            <View style={styles.colMoneyGroup}>
              <View style={styles.tableCurrencyUnit} />
              <Text style={[styles.headText, styles.colMoneyHead]}>充值金额</Text>
              <Text style={[styles.headText, styles.colMoneyHead]}>团队奖励</Text>
            </View>
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
      <LevelRewardModal
        visible={claimItem != null}
        submitting={claimSubmitting}
        item={claimItem}
        onClose={() => {
          if (!claimSubmitting) {
            setClaimItem(null);
          }
        }}
        onConfirm={(currency) => {
          if (claimItem) {
            void submitClaim(claimItem.levelId, currency);
          }
        }}
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
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.35)',
    backgroundColor: 'rgba(12, 24, 52, 0.82)',
    paddingHorizontal: 14,
    paddingVertical: 16,
  },
  statusRow: {
    flexDirection: 'row',
    alignItems: 'stretch',
    gap: 16,
  },
  statusCol: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'flex-start',
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
  statusMoneyPair: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
  },
  statusUnitHang: {
    marginLeft: -48,
  },
  claimEntry: {
    marginTop: 14,
    paddingTop: 12,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: 'rgba(232, 195, 106, 0.35)',
  },
  claimEntryText: {
    color: colors.gold,
    fontSize: 13,
    fontWeight: '600',
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
    width: '22%',
  },
  colDepth: {
    width: '18%',
  },
  colMoneyGroup: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    paddingTop: 1,
  },
  colMoney: {
    flex: 1,
    alignItems: 'flex-start',
  },
  colMoneyHead: {
    flex: 1,
    textAlign: 'left',
  },
  tableCurrencyUnit: {
    width: 40,
    alignItems: 'flex-end',
    gap: 2,
  },
  tableDualAmount: {
    alignItems: 'flex-start',
    gap: 2,
  },
  tableAmountLine: {
    color: colors.text,
    fontSize: 13,
    fontWeight: '700',
    lineHeight: 18,
    textAlign: 'left',
  },
  tableUnitText: {
    width: '100%',
    textAlign: 'right',
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
