import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native';
import { Text } from '@/components/ui/AppText';
import { useStableSafeTop } from '@/hooks/useStableSafeTop';

import { fetchAppProfile } from '@/api/app-auth';
import {
  fetchAppProductSeriesList,
  fetchAppProductSeriesWithItems,
} from '@/api/app-product';
import { subscribeAppProduct } from '@/api/app-trade';
import { setAppPayPassword } from '@/api/app-member';
import { ApiError } from '@/api/request';
import { ProductCard } from '@/components/ui/ProductCard';
import { PayPasswordModal, type PayPasswordMode } from '@/components/ui/PayPasswordModal';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import type { ProductItem, ProductSeries } from '@/types/product';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

export default function ProductsScreen() {
  const top = useStableSafeTop();
  const router = useRouter();
  const { seriesId: seriesIdParam } = useLocalSearchParams<{ seriesId?: string }>();
  const [seriesList, setSeriesList] = useState<ProductSeries[]>([]);
  const [activeSeriesId, setActiveSeriesId] = useState<string>('');
  const [items, setItems] = useState<ProductItem[]>([]);
  const [loadingSeries, setLoadingSeries] = useState(true);
  const [loadingItems, setLoadingItems] = useState(false);
  const activeSeriesIdRef = useRef(activeSeriesId);
  activeSeriesIdRef.current = activeSeriesId;

  const [hasPayPassword, setHasPayPassword] = useState(true);
  const [payVisible, setPayVisible] = useState(false);
  const [payMode, setPayMode] = useState<PayPasswordMode>('verify');
  const [submitting, setSubmitting] = useState(false);
  const [pendingItem, setPendingItem] = useState<ProductItem | null>(null);
  const [pendingCurrency, setPendingCurrency] = useState<'CNY' | 'USDT' | null>(null);

  const loadItems = useCallback(async (seriesId: string) => {
    if (!seriesId) {
      setItems([]);
      return;
    }
    setLoadingItems(true);
    try {
      const detail = await fetchAppProductSeriesWithItems(seriesId);
      setItems(detail?.items ?? []);
      if (detail?.tip) {
        setSeriesList((prev) =>
          prev.map((s) => (s.id === seriesId ? { ...s, tip: detail.tip } : s)),
        );
      }
    } catch {
      setItems([]);
    } finally {
      setLoadingItems(false);
    }
  }, []);

  const loadSeries = useCallback(async () => {
    setLoadingSeries(true);
    try {
      const [list, profile] = await Promise.all([
        fetchAppProductSeriesList(),
        fetchAppProfile().catch(() => null),
      ]);
      setHasPayPassword(profile?.hasPayPassword !== false);
      setSeriesList(list);
      const preferred =
        (typeof seriesIdParam === 'string' && list.some((item) => item.id === seriesIdParam)
          ? seriesIdParam
          : null) ||
        (activeSeriesIdRef.current && list.some((item) => item.id === activeSeriesIdRef.current)
          ? activeSeriesIdRef.current
          : null) ||
        list[0]?.id ||
        '';
      setActiveSeriesId(preferred);
      if (preferred) {
        await loadItems(preferred);
      } else {
        setItems([]);
      }
    } catch {
      setSeriesList([]);
      setActiveSeriesId('');
      setItems([]);
    } finally {
      setLoadingSeries(false);
    }
  }, [loadItems, seriesIdParam]);

  useFocusEffect(
    useCallback(() => {
      void loadSeries();
    }, [loadSeries]),
  );

  useEffect(() => {
    if (
      typeof seriesIdParam === 'string' &&
      seriesIdParam &&
      seriesIdParam !== activeSeriesIdRef.current &&
      seriesList.some((item) => item.id === seriesIdParam)
    ) {
      setActiveSeriesId(seriesIdParam);
      void loadItems(seriesIdParam);
    }
  }, [seriesIdParam, seriesList, loadItems]);

  const onSelectSeries = (seriesId: string) => {
    if (seriesId === activeSeriesId) {
      return;
    }
    setActiveSeriesId(seriesId);
    void loadItems(seriesId);
  };

  const onRefresh = useCallback(async () => {
    if (activeSeriesId) {
      await loadItems(activeSeriesId);
      return;
    }
    await loadSeries();
  }, [activeSeriesId, loadItems, loadSeries]);

  const isSkipDetail = (item: ProductItem) => item.skipDetailFlag === true;

  const isSplitLayout = (item: ProductItem) =>
    String(item.layoutType || '').toUpperCase() === 'SPLIT';

  const requestSubscribe = (item: ProductItem, currency: 'CNY' | 'USDT') => {
    if (submitting) {
      return;
    }
    if (item.onSaleFlag !== true) {
      modalWarning('暂未开放');
      return;
    }
    const productId = item.apiId ?? Number(item.id);
    if (!Number.isFinite(productId) || productId <= 0) {
      modalWarning('产品暂未开放认购');
      return;
    }
    const supported = currency === 'USDT' ? item.amount > 0 : item.amountCny > 0;
    if (!supported) {
      modalWarning(currency === 'USDT' ? '该产品暂不支持 USDT 认购' : '该产品暂不支持 RMB 认购');
      return;
    }
    setPendingItem(item);
    setPendingCurrency(currency);
    setPayMode(hasPayPassword ? 'verify' : 'set');
    setPayVisible(true);
  };

  const openDetail = (item: ProductItem) => {
    if (item.onSaleFlag !== true) {
      modalWarning('暂未开放');
      return;
    }
    // 跳过二级页：列表直购（与业务模式无关）
    if (isSkipDetail(item)) {
      // SPLIT 双按钮走 onSubscribe；单按钮布局点主 CTA 时按已配价选币种
      if (isSplitLayout(item)) {
        return;
      }
      const currency: 'CNY' | 'USDT' =
        item.amountCny > 0 ? 'CNY' : item.amount > 0 ? 'USDT' : 'CNY';
      requestSubscribe(item, currency);
      return;
    }
    router.push(`/products/subscribe/${item.id}`);
  };

  const closePaySheet = () => {
    setPayVisible(false);
    setPendingItem(null);
    setPendingCurrency(null);
    setPayMode('verify');
  };

  const onConfirmPay = async (payPassword: string) => {
    if (!pendingItem || !pendingCurrency || submitting) {
      return;
    }
    if (payPassword.length < 4) {
      modalWarning('请输入交易密码');
      return;
    }
    const productId = pendingItem.apiId ?? Number(pendingItem.id);
    setSubmitting(true);
    try {
      if (payMode === 'set') {
        await setAppPayPassword(payPassword);
        setHasPayPassword(true);
      }
      const message = await subscribeAppProduct({
        productId,
        currency: pendingCurrency,
        payPassword,
        quantity: 1,
      });
      closePaySheet();
      requestAnimationFrame(() => modalSuccess(message));
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '认购失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const activeSeries = seriesList.find((s) => s.id === activeSeriesId);
  const seriesTip = (activeSeries?.tip || '').trim();

  return (
    <View style={styles.page}>
      <View style={[styles.header, { paddingTop: Math.max(top, 12) + 8 }]}>
        {seriesList.length > 0 ? (
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.tabs}
          >
            {seriesList.map((series) => {
              const active = series.id === activeSeriesId;
              return (
                <Pressable
                  key={series.id}
                  style={styles.tabItem}
                  onPress={() => onSelectSeries(series.id)}
                >
                  <Text style={[styles.tabText, active && styles.tabTextActive]} numberOfLines={1}>
                    {series.name}
                  </Text>
                  <View style={[styles.tabBar, active && styles.tabBarActive]} />
                </Pressable>
              );
            })}
          </ScrollView>
      ) : (
        <Text style={styles.headerPlaceholder}>{loadingSeries ? '加载中…' : '暂无产品系列'}</Text>
        )}
        {seriesTip ? <Text style={styles.seriesTip}>{seriesTip}</Text> : null}
      </View>

      {loadingSeries && seriesList.length === 0 ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.content}
          onRefresh={onRefresh}
        >
          {loadingItems ? (
            <View style={styles.listLoading}>
              <ActivityIndicator color={colors.accent} />
            </View>
          ) : items.length === 0 ? (
            <Text style={styles.empty}>产品筹备中，敬请期待...</Text>
          ) : (
            items.map((item) => {
              const skipDetail = isSkipDetail(item);
              const split = isSplitLayout(item);
              return (
                <ProductCard
                  key={item.id}
                  item={item}
                  onPress={() => openDetail(item)}
                  onSubscribe={
                    skipDetail && split
                      ? (currency) => requestSubscribe(item, currency)
                      : undefined
                  }
                />
              );
            })
          )}
        </RefreshableScrollView>
      )}

      <PayPasswordModal
        visible={payVisible}
        mode={payMode}
        submitting={submitting}
        onCancel={closePaySheet}
        onConfirm={(pwd) => void onConfirmPay(pwd)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  header: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(120, 170, 230, 0.18)',
    paddingBottom: 4,
  },
  tabs: {
    paddingHorizontal: 10,
    minHeight: 36,
    alignItems: 'center',
  },
  tabItem: {
    paddingHorizontal: 10,
    paddingTop: 2,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabText: {
    color: 'rgba(180, 200, 230, 0.72)',
    fontSize: 13,
    lineHeight: 18,
    fontWeight: '500',
    maxWidth: 148,
  },
  tabTextActive: {
    color: colors.text,
    fontWeight: '600',
  },
  tabBar: {
    marginTop: 6,
    width: 28,
    height: 3,
    borderRadius: 2,
    backgroundColor: 'transparent',
  },
  tabBarActive: {
    backgroundColor: '#FF2A2A',
  },
  headerPlaceholder: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 18,
    paddingHorizontal: 16,
    paddingVertical: 8,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 14,
    paddingBottom: 32,
    gap: 16,
  },
  seriesTip: {
    color: colors.text,
    fontSize: 12,
    lineHeight: 18,
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 4,
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  listLoading: {
    paddingTop: 48,
    alignItems: 'center',
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 40,
    fontSize: 14,
  },
});
