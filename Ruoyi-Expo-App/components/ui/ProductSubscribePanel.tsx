import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { SubscribeButton } from '@/components/ui/SubscribeButton';
import type { ProductItem } from '@/types/product';
import { colors } from '@/theme/colors';

const MIN_QUANTITY = 1;

type Props = {
  item: ProductItem;
  submitting?: boolean;
  onSubscribeCny?: (quantity: number) => void;
  onSubscribeUsdt?: (quantity: number) => void;
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

function formatMoney(amount: number, unit: string): string {
  if (!(amount > 0)) {
    return '--';
  }
  const text = Number.isInteger(amount) ? String(amount) : amount.toFixed(2).replace(/\.?0+$/, '');
  return `${text} ${unit}`;
}

export function ProductSubscribePanel({
  item,
  submitting,
  onSubscribeCny,
  onSubscribeUsdt,
}: Props) {
  const [quantity, setQuantity] = useState(MIN_QUANTITY);
  const [quantityText, setQuantityText] = useState(String(MIN_QUANTITY));
  const productName = [item.name, item.enName].filter((v) => v && v !== '--').join(' ') || '--';
  const supportCny = item.amountCny > 0;
  const supportUsdt = item.amount > 0;
  const amountUsdt = supportUsdt ? `${item.amount} USDT` : '--';
  const amountCny = supportCny ? `${item.amountCny} RMB` : '--';
  const dailyUsdt = item.daily > 0 ? `${item.daily} USDT` : '--';
  const dailyCny = item.dailyCny > 0 ? `${item.dailyCny} RMB` : '--';
  const term = item.termDays > 0 ? `${item.termDays} 天` : '--';

  const applyQuantity = (next: number) => {
    const safe = Number.isFinite(next) ? Math.max(MIN_QUANTITY, Math.floor(next)) : MIN_QUANTITY;
    setQuantity(safe);
    setQuantityText(String(safe));
    return safe;
  };

  const payable = useMemo(() => {
    const parts: string[] = [];
    if (supportUsdt) {
      parts.push(formatMoney(item.amount * quantity, 'USDT'));
    }
    if (supportCny) {
      parts.push(formatMoney(item.amountCny * quantity, 'RMB'));
    }
    return parts.length ? parts.join(' / ') : '--';
  }, [item.amount, item.amountCny, quantity, supportCny, supportUsdt]);

  const rows = [
    { label: '产品名称', value: productName },
    { label: '产品类型', value: item.tag || '--' },
    {
      label: '单份金额',
      value: `${amountUsdt} / ${amountCny}`,
      highlight: true,
    },
    {
      label: '单份日收益',
      value: `${dailyUsdt} / ${dailyCny}`,
      highlight: true,
    },
    { label: '收益发放方式', value: item.payoutMethod || '--' },
    { label: '产品期限', value: term, highlight: true },
    { label: '支持货币', value: item.currencies || '--' },
    { label: '风险等级', value: item.riskLevel || '--' },
  ] as const;

  const changeQuantity = (delta: number) => {
    applyQuantity(quantity + delta);
  };

  const onQuantityChange = (text: string) => {
    const digits = text.replace(/[^\d]/g, '');
    setQuantityText(digits);
    if (!digits) {
      return;
    }
    const next = Number(digits);
    if (Number.isFinite(next) && next >= MIN_QUANTITY) {
      setQuantity(Math.floor(next));
    }
  };

  const onQuantityBlur = () => {
    applyQuantity(Number(quantityText));
  };

  const resolvedQuantity = () => applyQuantity(Number(quantityText) || quantity);

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

      <View style={styles.quantityCard}>
        <Text style={styles.quantityLabel}>认购份数</Text>
        <View style={styles.stepper}>
          <Pressable
            style={[styles.stepBtn, quantity <= MIN_QUANTITY && styles.stepBtnDisabled]}
            disabled={quantity <= MIN_QUANTITY || submitting}
            onPress={() => changeQuantity(-1)}
          >
            <Text style={styles.stepBtnText}>−</Text>
          </Pressable>
          <TextInput
            value={quantityText}
            onChangeText={onQuantityChange}
            onBlur={onQuantityBlur}
            editable={!submitting}
            keyboardType="number-pad"
            inputMode="numeric"
            selectTextOnFocus
            style={styles.quantityInput}
            placeholder="1"
            placeholderTextColor="rgba(180, 200, 230, 0.45)"
          />
          <Pressable
            style={styles.stepBtn}
            disabled={submitting}
            onPress={() => changeQuantity(1)}
          >
            <Text style={styles.stepBtnText}>+</Text>
          </Pressable>
        </View>
      </View>

      <View style={styles.payableCard}>
        <View style={styles.payableRow}>
          <Text style={styles.payableLabel}>应付金额</Text>
          <Text style={styles.payableValue}>{payable}</Text>
        </View>
      </View>

      <View style={styles.actions}>
        {supportCny ? (
          <SubscribeButton
            title="使用RMB认购"
            variant="cny"
            disabled={submitting}
            onPress={() => onSubscribeCny?.(resolvedQuantity())}
          />
        ) : null}
        {supportUsdt ? (
          <SubscribeButton
            title="使用USDT认购"
            variant="usdt"
            disabled={submitting}
            onPress={() => onSubscribeUsdt?.(resolvedQuantity())}
          />
        ) : null}
        {!supportCny && !supportUsdt ? (
          <Text style={styles.unsupported}>该产品暂未开放认购</Text>
        ) : null}
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
  quantityCard: {
    marginTop: 14,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.22)',
    backgroundColor: 'rgba(6, 18, 42, 0.55)',
    paddingHorizontal: 14,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  quantityLabel: {
    color: 'rgba(200, 218, 240, 0.78)',
    fontSize: 14,
    fontWeight: '600',
  },
  stepper: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  stepBtn: {
    width: 34,
    height: 34,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(47, 123, 255, 0.28)',
    borderWidth: 1,
    borderColor: 'rgba(120, 180, 255, 0.35)',
  },
  stepBtnDisabled: {
    opacity: 0.35,
  },
  stepBtnText: {
    color: colors.text,
    fontSize: 22,
    fontWeight: '500',
    lineHeight: 24,
    marginTop: -1,
  },
  quantityValue: {
    minWidth: 28,
    textAlign: 'center',
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
  },
  quantityInput: {
    minWidth: 56,
    maxWidth: 88,
    paddingHorizontal: 6,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(120, 180, 255, 0.28)',
    backgroundColor: 'rgba(8, 20, 44, 0.75)',
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
    textAlign: 'center',
  },
  payableCard: {
    marginTop: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: 'rgba(240, 180, 90, 0.28)',
    backgroundColor: 'rgba(40, 28, 12, 0.45)',
    paddingHorizontal: 14,
    paddingVertical: 10,
    gap: 8,
  },
  payableRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  payableLabel: {
    color: 'rgba(220, 200, 160, 0.85)',
    fontSize: 13,
  },
  payableValue: {
    color: '#F0B45A',
    fontSize: 15,
    fontWeight: '800',
    textAlign: 'right',
    flex: 1,
  },
  actions: {
    marginTop: 18,
    gap: 12,
  },
  unsupported: {
    color: colors.muted,
    textAlign: 'center',
    fontSize: 14,
    paddingVertical: 8,
  },
});
