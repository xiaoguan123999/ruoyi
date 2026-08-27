import { useMemo } from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { formatBalance } from '@/api/app-auth';
import { formatMoneyLabel } from '@/api/app-trade';
import type { AppFundRecord, AppWalletLogItem } from '@/api/types';
import { GlassCard } from '@/components/ui/GlassCard';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';

/** credit：累计入账（充值）；debit：累计出账（提现） */
export type FundSummaryMode = 'credit' | 'debit';

type FundListItem = Pick<AppWalletLogItem, 'id' | 'title' | 'amount' | 'currency' | 'createTime'> | AppFundRecord;

function sumByCurrency(records: FundListItem[], mode: FundSummaryMode) {
  return records.reduce(
    (acc, item) => {
      // 申请单：只累计「成功」；流水：成功文案或正负金额
      const successByTitle = /成功|到账/.test(item.title);
      const pendingOrFail = /申请|审核|处理|失败|拒绝|退回|冻结/.test(item.title);
      let value = 0;
      if (successByTitle) {
        value = Math.abs(item.amount);
      } else if (!pendingOrFail) {
        if (mode === 'credit' && item.amount > 0) {
          value = item.amount;
        } else if (mode === 'debit' && item.amount < 0) {
          value = Math.abs(item.amount);
        }
      }
      if (value <= 0) {
        return acc;
      }
      const currency = (item.currency || 'CNY').toUpperCase();
      if (currency === 'USDT' || currency === 'USD' || currency === 'U') {
        acc.usdt += value;
      } else {
        acc.cny += value;
      }
      return acc;
    },
    { cny: 0, usdt: 0 },
  );
}

function formatRecordDate(value: string): string {
  const raw = value.replace(/：/g, ':').trim();
  if (!raw) {
    return '';
  }
  return raw.slice(0, 10);
}

function resolveTone(title: string, amount: number) {
  if (/失败|拒绝|退回/.test(title)) {
    return styles.fail;
  }
  if (/待审|审核|申请|处理|冻结/.test(title)) {
    return styles.pending;
  }
  if (/成功|到账|入账|已通过/.test(title)) {
    return styles.success;
  }
  return amount < 0 ? styles.pending : styles.success;
}

type Props = {
  loading: boolean;
  records: FundListItem[];
  summaryLabel?: string;
  summaryMode?: FundSummaryMode;
  showSummary?: boolean;
  emptyText?: string;
  onRefresh: () => void | Promise<void>;
};

export function FundRecordsPanel({
  loading,
  records,
  summaryLabel = '累计',
  summaryMode = 'credit',
  showSummary = true,
  emptyText = '暂无记录',
  onRefresh,
}: Props) {
  const totals = useMemo(() => sumByCurrency(records, summaryMode), [records, summaryMode]);
  const rows = useMemo(
    () =>
      records.map((item) => ({
        id: item.id,
        title: item.title,
        date: formatRecordDate(item.createTime),
        amount: formatMoneyLabel(item.amount, item.currency),
        tone: resolveTone(item.title, item.amount),
      })),
    [records],
  );

  if (loading) {
    return (
      <View style={styles.loadingWrap}>
        <ActivityIndicator color={colors.accent} />
      </View>
    );
  }

  return (
    <RefreshableScrollView
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
      onRefresh={onRefresh}
    >
      {showSummary ? (
        <GlassCard style={styles.card}>
          <View style={styles.summaryRow}>
            <View style={styles.summaryLeft}>
              <View style={styles.summaryMark} />
              <Text style={styles.summaryLabel}>{summaryLabel}</Text>
            </View>
            <View style={styles.summaryAmounts}>
              <Text style={styles.summaryAmount}>¥ {formatBalance(totals.cny)}</Text>
              <Text style={styles.summaryAmount}>USDT {formatBalance(totals.usdt)}</Text>
            </View>
          </View>
        </GlassCard>
      ) : null}

      {rows.length === 0 ? (
        <Text style={styles.empty}>{emptyText}</Text>
      ) : (
        rows.map((item) => (
          <GlassCard key={item.id} style={styles.card}>
            <View style={styles.detailRow}>
              <View style={styles.detailLeft}>
                <Text style={[styles.title, item.tone]}>{item.title}</Text>
                <Text style={styles.time}>{item.date}</Text>
              </View>
              <Text style={styles.amount}>{item.amount}</Text>
            </View>
          </GlassCard>
        ))
      )}
    </RefreshableScrollView>
  );
}

const styles = StyleSheet.create({
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 24,
    gap: 12,
  },
  card: {
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
    borderColor: 'rgba(98, 150, 220, 0.24)',
    borderRadius: 12,
    paddingVertical: 16,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  summaryLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 1,
  },
  summaryMark: {
    width: 3,
    height: 18,
    borderRadius: 2,
    backgroundColor: '#FF2A2A',
    marginRight: 14,
  },
  summaryLabel: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
    lineHeight: 22,
  },
  summaryAmounts: {
    alignItems: 'flex-end',
    gap: 4,
  },
  summaryAmount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  detailLeft: {
    flex: 1,
    paddingRight: 12,
  },
  title: {
    fontSize: 15,
    fontWeight: '600',
  },
  success: {
    color: '#6FCF97',
  },
  fail: {
    color: '#FF6B6B',
  },
  pending: {
    color: '#F0C36A',
  },
  time: {
    marginTop: 8,
    color: colors.text,
    fontSize: 13,
  },
  amount: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
  },
});
