import { ScrollView, StyleSheet, Text, View } from 'react-native';

import { SubscribeButton } from '@/components/ui/SubscribeButton';
import type { ProductItem } from '@/constants/mock';
import { colors } from '@/theme/colors';

type Props = {
  item: ProductItem;
  onSubscribeCny?: () => void;
  onSubscribeUsdt?: () => void;
};

type RowProps = {
  label: string;
  value: string;
  highlight?: boolean;
  last?: boolean;
};

function InfoRow({ label, value, highlight, last }: RowProps) {
  return (
    <View style={[styles.row, last && styles.rowLast]}>
      <Text style={styles.label}>{label}</Text>
      <Text style={[styles.value, highlight && styles.valueHighlight]}>{value}</Text>
    </View>
  );
}

export function ProductSubscribePanel({ item, onSubscribeCny, onSubscribeUsdt }: Props) {
  const productName = `${item.name} ${item.enName}`;
  const rows = [
    { label: '产品名称', value: productName },
    { label: '产品类型', value: item.tag },
    {
      label: '参与金额',
      value: `${item.amount} USDT / ${item.amountCny} RMB`,
      highlight: true,
    },
    {
      label: '日收益',
      value: `${item.daily} USDT / ${item.dailyCny} RMB`,
      highlight: true,
    },
    { label: '收益发放方式', value: item.payoutMethod ?? '每日回报（次日发放）' },
    { label: '产品期限', value: `${item.termDays} 天`, highlight: true },
    { label: '支持货币', value: item.currencies ?? 'USDT / RMB' },
    { label: '风险等级', value: item.riskLevel ?? 'R2 中低风险' },
  ] as const;

  return (
    <View style={styles.panel}>
      <Text style={styles.sectionTitle}>产品信息</Text>

      <View style={styles.table}>
        {rows.map((row, index) => (
          <InfoRow
            key={row.label}
            label={row.label}
            value={row.value}
            highlight={'highlight' in row ? row.highlight : false}
            last={index === rows.length - 1}
          />
        ))}
      </View>

      <View style={styles.actions}>
        <SubscribeButton title="使用RMB认购" variant="cny" onPress={onSubscribeCny} />
        <SubscribeButton title="使用USDT认购" variant="usdt" onPress={onSubscribeUsdt} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  panel: {
    borderRadius: 14,
    paddingHorizontal: 16,
    paddingTop: 18,
    paddingBottom: 20,
    backgroundColor: 'rgba(10, 24, 52, 0.78)',
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.28)',
  },
  sectionTitle: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
    marginBottom: 14,
  },
  table: {
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.22)',
    overflow: 'hidden',
    backgroundColor: 'rgba(6, 18, 42, 0.55)',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: 12,
    paddingHorizontal: 14,
    paddingVertical: 13,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(110, 185, 255, 0.14)',
  },
  rowLast: {
    borderBottomWidth: 0,
  },
  label: {
    color: 'rgba(200, 218, 240, 0.78)',
    fontSize: 13,
    lineHeight: 20,
    flexShrink: 0,
  },
  value: {
    color: colors.text,
    fontSize: 13,
    lineHeight: 20,
    textAlign: 'right',
    flex: 1,
  },
  valueHighlight: {
    color: '#F0B45A',
    fontWeight: '700',
  },
  actions: {
    marginTop: 18,
    gap: 12,
  },
});
