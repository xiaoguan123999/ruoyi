import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import * as Clipboard from 'expo-clipboard';

import { Text } from '@/components/ui/AppText';
import { GlassCard } from '@/components/ui/GlassCard';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import {
  chainLeftSeconds,
  displayPayAmountText,
  formatChainRemainHms,
} from '@/api/app-chain-deposit';
import type { AppChainDepositOrder } from '@/api/types';
import { colors } from '@/theme/colors';
import { toastSuccess, modalWarning } from '@/utils/toast';

async function copyText(value: string, label: string) {
  const text = value.trim();
  if (!text) {
    modalWarning(`暂无${label}`);
    return;
  }
  await Clipboard.setStringAsync(text);
  toastSuccess(`${label}已复制`);
}

/** 长地址按宽度自动换行，避免挤掉「复制」 */
function softWrapText(value: string) {
  return value.replace(/(.{1})/g, '$1\u200B');
}

function CopyBox({
  label,
  value,
  display,
  suffix,
  large,
  inline,
}: {
  label: string;
  value: string;
  display?: string;
  suffix?: string;
  large?: boolean;
  inline?: boolean;
}) {
  const shown = display ?? value;
  const body = large ? shown : softWrapText(shown);
  return (
    <View style={styles.block}>
      {inline ? null : <Text style={styles.blockLabel}>{label}</Text>}
      <Pressable
        style={[styles.box, large && styles.boxLarge, inline && styles.boxInline]}
        onPress={() => void copyText(value, label)}
      >
        {inline ? (
          <Text style={styles.inlineLabel} numberOfLines={1}>
            {label}
          </Text>
        ) : null}
        <Text
          style={[styles.boxText, large && styles.amountText]}
          numberOfLines={large || inline ? 1 : undefined}
        >
          {body}
          {suffix ? ` ${suffix}` : ''}
        </Text>
        <Text style={styles.copy} numberOfLines={1}>
          复制
        </Text>
      </Pressable>
    </View>
  );
}

export function ChainDepositPanel({
  order,
  settled,
  onExpired,
  onBack,
  onDone,
}: {
  order: AppChainDepositOrder;
  settled?: 'paid' | 'expired' | null;
  onExpired?: () => void;
  onBack?: () => void;
  onDone?: () => void;
}) {
  const [now, setNow] = useState(Date.now());
  const left = chainLeftSeconds(order, now);
  const timedOut = order.status === '2' || left === 0;
  const expired = settled === 'expired' || timedOut;

  useEffect(() => {
    if (expired || settled === 'paid') {
      return;
    }
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, [expired, settled]);

  useEffect(() => {
    if (timedOut && settled !== 'paid') {
      onExpired?.();
    }
  }, [onExpired, settled, timedOut]);

  const networkName = order.network || 'TRC20';
  const addressLabel = `付款地址（${networkName}）`;

  return (
    <View>
      <CopyBox
        label="付款金额"
        value={order.payAmountText}
        display={displayPayAmountText(order.payAmountText)}
        suffix="USDT"
        large
      />
      <Text style={styles.warn}>请完整支付（包括小数点），否则系统无法识别到账。</Text>
      <CopyBox label={addressLabel} value={order.address} />
      <View style={styles.qrWrap}>
        <QRCode value={order.address} size={196} backgroundColor="#FFFFFF" color="#0B1730" />
      </View>
      <CopyBox label="订单号" value={order.outTradeNo} inline />
      {settled === 'paid' ? (
        <GlassCard style={styles.resultCard}>
          <Text style={styles.resultOk}>充值成功</Text>
          <Text style={styles.resultHint}>已入账，可查看账户余额</Text>
          <PrimaryButton title="查看余额" compact onPress={onDone} />
        </GlassCard>
      ) : expired ? (
        <GlassCard style={styles.resultCard}>
          <Text style={styles.resultWarn}>订单已过期，请重新下单</Text>
          <Text style={styles.resultHint}>请勿继续转账</Text>
          <PrimaryButton title="重新下单" compact onPress={onBack} />
        </GlassCard>
      ) : left != null && left > 0 ? (
        <View style={[styles.countWrap, left > 180 ? styles.countWrapOk : styles.countWrapWarn]}>
          <Text style={[styles.count, left > 180 ? styles.countOk : styles.countWarn]}>
            {formatChainRemainHms(left)} 后订单将过期
          </Text>
        </View>
      ) : null}
      <GlassCard>
        <Text style={styles.cardTitle}>充值说明</Text>
        <Text style={styles.rule}>
          1.请在倒计时结束前支付完成，否则<Text style={styles.red}>订单失效</Text>。
        </Text>
        <Text style={styles.rule}>
          2.<Text style={styles.red}>订单超时 请勿转账</Text>，如由于超时转账导致资金损失，我司不承担损失。
        </Text>
        <Text style={styles.rule}>
          3.付款成功后，系统会在约<Text style={styles.red}>30秒</Text>内完成到账。
        </Text>
        <Text style={styles.rule}>
          4. 请<Text style={styles.red}>仔细核对转账地址</Text>，地址不一致不要转账。如由于剪切板内容被篡改导致转账地址错误，我司不承担损失。
        </Text>
      </GlassCard>
    </View>
  );
}

const styles = StyleSheet.create({
  block: { marginBottom: 8 },
  blockLabel: {
    textAlign: 'center',
    color: colors.text,
    fontSize: 15,
    fontWeight: '600',
    marginBottom: 10,
  },
  box: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(180, 205, 235, 0.28)',
    backgroundColor: 'rgba(8, 22, 48, 0.72)',
    paddingHorizontal: 14,
    paddingVertical: 14,
    gap: 12,
  },
  boxLarge: {
    alignItems: 'center',
  },
  boxInline: {
    alignItems: 'center',
    paddingVertical: 12,
  },
  inlineLabel: {
    flexShrink: 0,
    color: colors.muted,
    fontSize: 13,
    fontWeight: '600',
  },
  boxText: {
    flex: 1,
    flexShrink: 1,
    minWidth: 0,
    color: colors.text,
    fontSize: 14,
    lineHeight: 22,
  },
  amountText: { fontSize: 24, fontWeight: '700', letterSpacing: 0.4, textAlign: 'center' },
  copy: {
    flexShrink: 0,
    width: 36,
    color: colors.accent,
    fontSize: 13,
    fontWeight: '700',
    lineHeight: 22,
    textAlign: 'center',
  },
  warn: {
    marginTop: 8,
    marginBottom: 16,
    textAlign: 'center',
    color: colors.danger,
    fontSize: 13,
    lineHeight: 20,
  },
  qrWrap: {
    alignSelf: 'center',
    marginTop: 8,
    marginBottom: 16,
    padding: 12,
    borderRadius: 12,
    backgroundColor: '#fff',
  },
  countWrap: {
    marginBottom: 18,
    paddingVertical: 12,
    paddingHorizontal: 14,
    borderRadius: 12,
    backgroundColor: 'rgba(8, 14, 32, 0.88)',
    borderWidth: 1,
  },
  countWrapOk: { borderColor: 'rgba(61, 220, 132, 0.45)' },
  countWrapWarn: { borderColor: 'rgba(255, 110, 110, 0.45)' },
  count: {
    textAlign: 'center',
    fontSize: 18,
    fontWeight: '700',
    letterSpacing: 0.4,
  },
  countOk: { color: colors.success },
  countWarn: { color: '#FF8B8B' },
  resultCard: { marginBottom: 18 },
  resultOk: {
    color: colors.success,
    fontSize: 16,
    fontWeight: '700',
    textAlign: 'center',
  },
  resultWarn: {
    color: colors.danger,
    fontSize: 16,
    fontWeight: '700',
    textAlign: 'center',
  },
  resultHint: {
    color: colors.muted,
    fontSize: 13,
    textAlign: 'center',
    marginTop: 8,
    marginBottom: 14,
  },
  cardTitle: { color: colors.muted, fontSize: 13 },
  rule: { marginTop: 10, color: colors.text, fontSize: 13, lineHeight: 20 },
  red: { color: colors.danger, fontWeight: '700' },
});
