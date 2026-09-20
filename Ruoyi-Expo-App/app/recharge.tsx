import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useEffect, useMemo, useRef, useState, type ComponentRef } from 'react';
import { Image, type ImageSource } from 'expo-image';
import { Animated, Modal, Platform, Pressable, ScrollView, StyleSheet, useWindowDimensions, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Text, TextInput } from '@/components/ui/AppText';
import {
  createAppPayDeposit,
  fetchAppPayChannels,
  fetchAppPayOrder,
  isPayOrderPaid,
  isPayOrderPending,
  listPayChannelsByScene,
} from '@/api/app-pay';
import { ApiError } from '@/api/request';
import { fetchAppWallet, parseAmountInput } from '@/api/app-trade';
import type { AppPayChannel, AppWallet } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { DualBalance } from '@/components/ui/DualBalance';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { PayCashierPopup } from '@/components/pay/PayCashierPopup';
import { forgetPayOrder, payReturnUrl, readPayOrder, rememberPayOrder } from '@/utils/pay-session';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

const methods = [
  { key: 'wechat', label: '微信', icon: images.payWechat, currency: 'CNY' as const, scene: 'wechat' },
  { key: 'alipay', label: '支付宝', icon: images.payAlipay, currency: 'CNY' as const, scene: 'alipay' },
  { key: 'usdt', label: 'USDT（客服）', icon: images.payUsdt, currency: 'USDT' as const, toService: true },
  { key: 'bank', label: '银行卡（客服）', icon: images.payCard, currency: 'CNY' as const, toService: true },
];

function formatLimit(value?: number) {
  if (value == null) {
    return '';
  }
  return String(value);
}

function channelLimitText(channel: AppPayChannel) {
  const min = formatLimit(channel.minAmount);
  const max = formatLimit(channel.maxAmount);
  if (min && max) {
    return `限额 ${min} ~ ${max}`;
  }
  if (min) {
    return `最低 ${min}`;
  }
  if (max) {
    return `最高 ${max}`;
  }
  return '';
}

function channelDisplayName(channel: AppPayChannel) {
  return channel.name?.trim() || '';
}

const USE_NATIVE_DRIVER = Platform.OS !== 'web';

function ChannelSheet({
  visible,
  title,
  hint,
  icon,
  channels,
  selectedCode,
  onClose,
  onSelect,
}: {
  visible: boolean;
  title: string;
  hint?: string;
  icon?: ImageSource;
  channels: AppPayChannel[];
  selectedCode?: string;
  onClose: () => void;
  onSelect: (channel: AppPayChannel) => void;
}) {
  const insets = useSafeAreaInsets();
  const { height } = useWindowDimensions();
  const sheetHeight = Math.round(height * 0.72);
  const [mounted, setMounted] = useState(visible);
  const [cache, setCache] = useState({ title, hint, icon, channels, selectedCode });
  const backdrop = useRef(new Animated.Value(0)).current;
  const sheetY = useRef(new Animated.Value(sheetHeight)).current;

  useEffect(() => {
    if (visible) {
      setCache({ title, hint, icon, channels, selectedCode });
    }
  }, [visible, title, hint, icon, channels, selectedCode]);

  useEffect(() => {
    if (visible) {
      setMounted(true);
      backdrop.setValue(0);
      sheetY.setValue(sheetHeight);
      Animated.parallel([
        Animated.timing(backdrop, {
          toValue: 1,
          duration: 240,
          useNativeDriver: USE_NATIVE_DRIVER,
        }),
        Animated.spring(sheetY, {
          toValue: 0,
          useNativeDriver: USE_NATIVE_DRIVER,
          friction: 10,
          tension: 64,
          velocity: 0.6,
        }),
      ]).start();
      return;
    }
    if (!mounted) {
      return;
    }
    Animated.parallel([
      Animated.timing(backdrop, {
        toValue: 0,
        duration: 180,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
      Animated.timing(sheetY, {
        toValue: sheetHeight,
        duration: 220,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    ]).start(({ finished }) => {
      if (finished) {
        setMounted(false);
      }
    });
  }, [visible, mounted, backdrop, sheetY, sheetHeight]);

  if (!mounted) {
    return null;
  }

  return (
    <Modal transparent visible animationType="none" onRequestClose={onClose} statusBarTranslucent>
      <View style={styles.sheetHost}>
        <Animated.View style={[styles.sheetMask, { opacity: backdrop }]}>
          <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        </Animated.View>
        <Animated.View
          style={[
            styles.sheet,
            {
              height: sheetHeight,
              paddingBottom: Math.max(insets.bottom, 16),
              transform: [{ translateY: sheetY }],
            },
          ]}
        >
          <View style={styles.sheetHandle} />
          <View style={styles.sheetHead}>
            <Text style={styles.sheetTitle}>{cache.title}</Text>
            <Pressable onPress={onClose} hitSlop={12} style={styles.sheetClose}>
              <Text style={styles.sheetCloseText}>×</Text>
            </Pressable>
          </View>
          {cache.hint ? <Text style={styles.sheetHint}>{cache.hint}</Text> : null}
          <ScrollView
            style={styles.sheetScroll}
            contentContainerStyle={styles.sheetGrid}
            showsVerticalScrollIndicator={false}
          >
            {cache.channels.map((item) => {
              const on = cache.selectedCode === item.channelCode;
              const name = channelDisplayName(item);
              const limit = channelLimitText(item);
              return (
                <Pressable
                  key={item.channelCode}
                  style={[styles.sheetCard, on && styles.channelCardOn]}
                  onPress={() => onSelect(item)}
                >
                  <View style={styles.channelHead}>
                    {cache.icon ? (
                      <Image source={cache.icon} style={styles.channelIcon} contentFit="contain" />
                    ) : null}
                    <Text style={styles.channelName} numberOfLines={1}>
                      {name}
                    </Text>
                  </View>
                  {limit ? <Text style={styles.channelLimit}>{limit}</Text> : null}
                </Pressable>
              );
            })}
          </ScrollView>
        </Animated.View>
      </View>
    </Modal>
  );
}

export default function RechargeScreen() {
  const router = useRouter();
  const amountRef = useRef<ComponentRef<typeof TextInput>>(null);
  const lastOrderNo = useRef('');
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState(methods[0].key);
  const [submitting, setSubmitting] = useState(false);
  const [wallet, setWallet] = useState<AppWallet | null>(null);
  const [channels, setChannels] = useState<AppPayChannel[]>([]);
  const [channelByScene, setChannelByScene] = useState<Record<string, string>>({});
  const [sheetScene, setSheetScene] = useState<string | null>(null);
  const [cashier, setCashier] = useState<{ payUrl: string; outTradeNo: string } | null>(null);

  const load = useCallback(async () => {
    try {
      setWallet(await fetchAppWallet());
    } catch {
    }
    try {
      setChannels(await fetchAppPayChannels());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取充值通道失败');
      }
    }
  }, []);


  const cny = wallet?.cnyAvailable ?? 0;
  const usdt = wallet?.usdtAvailable ?? 0;
  const selected = methods.find((item) => item.key === method);
  const selectedScene = selected?.scene;
  const sceneChannels = useMemo(
    () => (selectedScene ? listPayChannelsByScene(channels, selectedScene) : []),
    [channels, selectedScene],
  );
  const selectedChannel = useMemo(() => {
    if (!selectedScene) {
      return undefined;
    }
    const code = channelByScene[selectedScene];
    return sceneChannels.find((item) => item.channelCode === code);
  }, [channelByScene, sceneChannels, selectedScene]);

  const sheetMeta = methods.find((item) => item.scene === sheetScene);
  const sheetChannels = useMemo(
    () => (sheetScene ? listPayChannelsByScene(channels, sheetScene) : []),
    [channels, sheetScene],
  );

  const openChannelSheet = (scene: string) => {
    const list = listPayChannelsByScene(channels, scene);
    if (list.length === 0) {
      modalWarning('暂未开放充值');
      return false;
    }
    setSheetScene(scene);
    return true;
  };

  const onSelectMethod = (key: string) => {
    const next = methods.find((item) => item.key === key);
    if (!next) {
      return;
    }
    setMethod(key);
    if (next.toService || !next.scene) {
      setSheetScene(null);
      return;
    }
    openChannelSheet(next.scene);
  };

  const onPickChannel = (channel: AppPayChannel) => {
    if (!sheetScene) {
      return;
    }
    setChannelByScene((prev) => ({ ...prev, [sheetScene]: channel.channelCode }));
    setSheetScene(null);
  };

  const checkOrder = useCallback(async (outTradeNo: string, quiet = false) => {
    const order = await fetchAppPayOrder(outTradeNo);
    if (isPayOrderPaid(order.status)) {
      lastOrderNo.current = '';
      forgetPayOrder();
      setAmount('');
      modalSuccess('充值成功');
      await load();
      return true;
    }
    if (!isPayOrderPending(order.status)) {
      lastOrderNo.current = '';
      forgetPayOrder();
      if (!quiet) {
        modalWarning(order.status === '2' ? '支付失败' : '订单已关闭');
      }
      return true;
    }
    return false;
  }, [load]);

  useFocusEffect(
    useCallback(() => {
      void load();
      const outTradeNo = lastOrderNo.current || readPayOrder();
      if (outTradeNo) {
        lastOrderNo.current = outTradeNo;
        void checkOrder(outTradeNo, true).catch(() => {});
      }
    }, [load, checkOrder]),
  );

  const openPayAndWatch = useCallback((payUrl: string, outTradeNo: string) => {
    lastOrderNo.current = outTradeNo;
    rememberPayOrder(outTradeNo);
    setCashier({ payUrl, outTradeNo });
  }, []);

  const onSubmit = async () => {
    if (!selected) {
      modalWarning('请选择充值方式');
      return;
    }
    if (selected.toService) {
      router.push('/service-chat');
      return;
    }
    if (!selected.scene || sceneChannels.length === 0) {
      modalWarning('暂未开放充值');
      return;
    }
    if (!selectedChannel) {
      if (selected.scene) {
        openChannelSheet(selected.scene);
      }
      return;
    }
    const value = parseAmountInput(amount);
    if (value <= 0) {
      modalWarning('请输入有效充值金额');
      return;
    }
    if (selectedChannel.minAmount != null && value < selectedChannel.minAmount) {
      modalWarning(`最低充值 ${selectedChannel.minAmount}`);
      return;
    }
    if (selectedChannel.maxAmount != null && value > selectedChannel.maxAmount) {
      modalWarning(`最高充值 ${selectedChannel.maxAmount}`);
      return;
    }
    setSubmitting(true);
    try {
      const deposit = await createAppPayDeposit({
        amount: value,
        scene: selected.scene,
        channelCode: selectedChannel.channelCode,
        returnUrl: payReturnUrl(),
      });
      openPayAndWatch(deposit.payUrl, deposit.outTradeNo);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '充值下单失败，请稍后重试');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const limitHint = selectedChannel ? channelLimitText(selectedChannel) : null;

  return (
    <AppBackground>
      <PageHeader title="充值" />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 28, gap: 12 }}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        onRefresh={load}
      >
        <GlassCard>
          <View style={styles.row}>
            <Text style={styles.label}>账户可用余额</Text>
            <Pressable onPress={() => router.push('/recharge-records')}>
              <Text style={styles.link}>充值记录 ›</Text>
            </Pressable>
          </View>
          <View style={[styles.balanceWrap]}>
            <DualBalance cny={cny} usdt={usdt} />
          </View>
        </GlassCard>
        <GlassCard>
          <Text style={styles.label}>充值方式</Text>
          {methods.map((item) => {
            const active = method === item.key;
            return (
              <Pressable key={item.key} style={styles.method} onPress={() => onSelectMethod(item.key)}>
                <View style={[styles.radio, active && styles.radioOn]} />
                <Image source={item.icon} style={styles.icon} contentFit="contain" />
                <Text style={styles.methodText}>{item.label}</Text>
              </Pressable>
            );
          })}
        </GlassCard>
        {!selected?.toService ? (
          <GlassCard>
            <Pressable
              style={styles.pickerRow}
              onPress={() => {
                if (selected?.scene) {
                  openChannelSheet(selected.scene);
                  return;
                }
                modalWarning('请先选择充值方式');
              }}
            >
              <Image source={selected?.icon ?? images.payCard} style={styles.pickerIcon} contentFit="contain" />
              <View style={styles.pickerText}>
                <Text style={styles.pickerTitle} numberOfLines={1}>
                  {selectedChannel ? channelDisplayName(selectedChannel) : '点击选择付款方式'}
                </Text>
                <Text style={styles.pickerSub} numberOfLines={1}>
                  {selectedChannel
                    ? channelLimitText(selectedChannel) || '已选择通道'
                    : '不同方式限额不同，请按需选择'}
                </Text>
              </View>
              <Text style={styles.pickerArrow}>›</Text>
            </Pressable>
          </GlassCard>
        ) : null}
        <GlassCard>
          <Text style={styles.label}>
            充值金额 <Text style={{ color: colors.danger }}>（通道拥堵可联系在线客服充值）</Text>
          </Text>
          <TextInput
            ref={amountRef}
            value={amount}
            onChangeText={setAmount}
            keyboardType="numeric"
            style={styles.input}
            placeholder={selected?.currency === 'USDT' ? 'USDT 0' : '¥ 0'}
            placeholderTextColor={colors.placeholder}
          />
          {limitHint ? <Text style={styles.limit}>{limitHint}</Text> : null}
        </GlassCard>
        <PrimaryButton title="充 值" onPress={() => void onSubmit()} disabled={submitting} />
      </RefreshableScrollView>

      <PayCashierPopup
        visible={cashier != null}
        payUrl={cashier?.payUrl}
        outTradeNo={cashier?.outTradeNo}
        onClose={() => setCashier(null)}
        onSettled={() => {
          const outTradeNo = cashier?.outTradeNo || lastOrderNo.current;
          setCashier(null);
          if (outTradeNo) {
            void checkOrder(outTradeNo, true);
          }
        }}
      />
      <ChannelSheet
        visible={sheetScene != null}
        title="选择支付通道"
        hint={`${sheetMeta?.label || ''} · 请选择可用方式完成充值`}
        icon={sheetMeta?.icon}
        channels={sheetChannels}
        selectedCode={sheetScene ? channelByScene[sheetScene] : undefined}
        onClose={() => setSheetScene(null)}
        onSelect={onPickChannel}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  row: { flexDirection: 'row', justifyContent: 'space-between' },
  label: { color: colors.muted, fontSize: 13 },
  link: { color: colors.text, fontSize: 13 },
  balanceWrap: { marginTop: 8 },
  limit: { marginTop: 8, color: colors.muted, fontSize: 12 },
  input: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '700',
    borderBottomWidth: 1,
    borderBottomColor: '#fff',
    marginTop: 12,
    paddingVertical: 8,
  },
  method: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, gap: 10 },
  radio: { width: 16, height: 16, borderRadius: 8, borderWidth: 1, borderColor: colors.text },
  radioOn: { backgroundColor: colors.accent, borderColor: colors.accent },
  icon: { width: 22, height: 22 },
  methodText: { color: colors.text, fontSize: 15 },
  pickerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  pickerIcon: { width: 22, height: 22 },
  pickerText: { flex: 1 },
  pickerTitle: { color: colors.text, fontSize: 15, fontWeight: '600' },
  pickerSub: { marginTop: 4, color: colors.muted, fontSize: 12 },
  pickerArrow: { color: colors.muted, fontSize: 22, lineHeight: 22, marginTop: -2 },
  channelCardOn: {
    borderColor: colors.accent,
    backgroundColor: 'rgba(61, 139, 255, 0.18)',
  },
  channelHead: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  channelIcon: { width: 18, height: 18 },
  channelName: { flex: 1, color: colors.text, fontSize: 14, fontWeight: '600' },
  channelLimit: { marginTop: 8, color: colors.muted, fontSize: 12 },
  sheetHost: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  sheetMask: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
  },
  sheet: {
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    borderWidth: 1,
    borderBottomWidth: 0,
    borderColor: 'rgba(110, 185, 255, 0.35)',
    backgroundColor: 'rgba(12, 28, 58, 0.98)',
    paddingHorizontal: 18,
    paddingTop: 10,
  },
  sheetHandle: {
    alignSelf: 'center',
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: 'rgba(180, 205, 235, 0.4)',
    marginBottom: 12,
  },
  sheetHead: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  sheetTitle: {
    flex: 1,
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
  },
  sheetHint: {
    color: colors.muted,
    fontSize: 13,
    marginBottom: 12,
  },
  sheetClose: {
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sheetCloseText: {
    color: colors.text,
    fontSize: 24,
    lineHeight: 24,
    fontWeight: '300',
  },
  sheetScroll: {
    flex: 1,
  },
  sheetGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    paddingBottom: 8,
  },
  sheetCard: {
    width: '47%',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(180, 205, 235, 0.22)',
    backgroundColor: 'rgba(8, 22, 48, 0.55)',
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
});
