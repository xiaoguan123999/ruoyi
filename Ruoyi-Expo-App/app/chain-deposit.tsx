import { useFocusEffect, useLocalSearchParams, useRouter, type Href } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';

import {
  fetchAppChainDepositOrder,
  isChainDepositExpired,
  isChainDepositPaid,
} from '@/api/app-chain-deposit';
import { ApiError } from '@/api/request';
import type { AppChainDepositOrder } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { ChainDepositPanel } from '@/components/pay/ChainDepositPanel';
import { Text } from '@/components/ui/AppText';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

function paramText(value: string | string[] | undefined) {
  return (Array.isArray(value) ? value[0] : value)?.trim() || '';
}

type Settled = 'paid' | 'expired';

export default function ChainDepositCashierScreen() {
  const router = useRouter();
  const { outTradeNo: rawNo } = useLocalSearchParams<{ outTradeNo?: string }>();
  const outTradeNo = paramText(rawNo);
  const [order, setOrder] = useState<AppChainDepositOrder | null>(null);
  const [loading, setLoading] = useState(true);
  const [settled, setSettled] = useState<Settled | null>(null);
  const settledRef = useRef<Settled | null>(null);

  const leave = useCallback(() => {
    if (router.canGoBack()) {
      router.back();
      return;
    }
    router.replace('/recharge');
  }, [router]);

  const goBalance = useCallback(() => {
    router.replace('/(tabs)/profile' as Href);
  }, [router]);

  const finishPaid = useCallback(() => {
    settledRef.current = 'paid';
    setSettled('paid');
  }, []);

  const finishExpired = useCallback(() => {
    if (settledRef.current === 'paid') {
      return;
    }
    settledRef.current = 'expired';
    setSettled('expired');
  }, []);

  const loadOrder = useCallback(async (quiet = false) => {
    if (!outTradeNo) {
      setLoading(false);
      return null;
    }
    try {
      const next = await fetchAppChainDepositOrder(outTradeNo);
      setOrder(next);
      if (isChainDepositPaid(next.status)) {
        finishPaid();
      } else if (
        isChainDepositExpired(next.status) ||
        (next.remainSeconds != null && next.remainSeconds <= 0) ||
        (next.expireAt != null && next.expireAt > 0 && next.expireAt <= Date.now())
      ) {
        finishExpired();
      }
      return next;
    } catch (error) {
      if (!quiet && (!(error instanceof ApiError) || error.code !== 401)) {
        modalError(error instanceof ApiError ? error.message : '获取充值单失败');
      }
      return null;
    } finally {
      setLoading(false);
    }
  }, [finishExpired, finishPaid, outTradeNo]);

  useFocusEffect(
    useCallback(() => {
      settledRef.current = null;
      setSettled(null);
      void loadOrder();
    }, [loadOrder]),
  );

  useEffect(() => {
    if (!outTradeNo) {
      return;
    }
    const timer = setInterval(() => {
      if (settledRef.current !== 'paid') {
        void loadOrder(true);
      }
    }, 5000);
    return () => clearInterval(timer);
  }, [loadOrder, outTradeNo]);

  return (
    <AppBackground>
      <PageHeader title="USDT 收银台" onBack={settled === 'paid' ? goBalance : leave} />
      {loading && !order ? (
        <View style={styles.center}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : !outTradeNo || !order ? (
        <View style={styles.center}>
          <Text style={styles.empty}>充值单不存在</Text>
          <PrimaryButton title="返回充值" compact onPress={leave} />
        </View>
      ) : (
        <RefreshableScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.content}
          onRefresh={async () => {
            await loadOrder();
          }}
        >
          <ChainDepositPanel
            order={order}
            settled={settled}
            onExpired={settled === 'paid' ? undefined : finishExpired}
            onBack={leave}
            onDone={goBalance}
          />
        </RefreshableScrollView>
      )}
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
    gap: 16,
  },
  empty: { color: colors.muted, fontSize: 14 },
  content: { paddingHorizontal: 18, paddingBottom: 32, paddingTop: 8 },
});
