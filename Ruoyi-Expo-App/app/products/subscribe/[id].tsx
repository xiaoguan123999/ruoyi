import { useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, View } from 'react-native';

import { ApiError } from '@/api/request';
import { fetchAppProductItems, subscribeAppProduct } from '@/api/app-trade';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { ProductSubscribePanel } from '@/components/ui/ProductSubscribePanel';
import type { ProductItem } from '@/constants/mock';
import { getProductItem } from '@/constants/mock';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

export default function ProductSubscribeScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [item, setItem] = useState<ProductItem | undefined>(() => getProductItem(id)?.item);
  const [loading, setLoading] = useState(!item);
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const list = await fetchAppProductItems();
      const found = list.find((p) => p.id === String(id));
      if (found) {
        setItem(found);
      } else {
        setItem(getProductItem(id)?.item);
      }
    } catch (error) {
      setItem(getProductItem(id)?.item);
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取产品失败');
      }
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  const onSubscribe = async (currency: 'CNY' | 'USDT') => {
    if (!item || submitting) {
      return;
    }
    const productId = Number(item.id);
    if (!Number.isFinite(productId) || productId <= 0) {
      modalWarning('产品信息无效');
      return;
    }
    const amount = currency === 'USDT' ? item.amount : item.amountCny;
    if (amount <= 0) {
      modalWarning('认购金额无效');
      return;
    }
    setSubmitting(true);
    try {
      const message = await subscribeAppProduct({
        productId,
        amount,
        currency,
      });
      modalSuccess(message);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '认购失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <AppBackground source={images.pageBg} dim={false}>
        <PageHeader title="产品信息" />
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      </AppBackground>
    );
  }

  if (!item) {
    return (
      <AppBackground source={images.pageBg} dim={false}>
        <PageHeader title="产品信息" />
        <View style={styles.empty} />
      </AppBackground>
    );
  }

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="产品信息" />
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
      >
        <ProductSubscribePanel
          item={item}
          onSubscribeCny={() => void onSubscribe('CNY')}
          onSubscribeUsdt={() => void onSubscribe('USDT')}
        />
      </ScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  empty: {
    flex: 1,
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
