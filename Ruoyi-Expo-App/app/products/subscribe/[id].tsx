import { useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { StyleSheet } from 'react-native';

import { fetchAppProductById } from '@/api/app-product';
import { subscribeAppProduct } from '@/api/app-trade';
import { ApiError } from '@/api/request';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { ProductSubscribePanel } from '@/components/ui/ProductSubscribePanel';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import type { ProductItem } from '@/types/product';
import { images } from '@/constants/images';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

export default function ProductSubscribeScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [item, setItem] = useState<ProductItem | undefined>();
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    try {
      setItem(await fetchAppProductById(id));
    } catch {
      setItem(undefined);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  const onSubscribe = async (currency: 'CNY' | 'USDT') => {
    if (!item || submitting) {
      return;
    }
    const productId = item.apiId ?? Number(item.id);
    if (!Number.isFinite(productId) || productId <= 0) {
      modalWarning('产品暂未开放认购');
      return;
    }
    // 0 / 空表示不支持该币种；金额由后台产品配置决定，客户端不传 amount
    const supported = currency === 'USDT' ? item.amount > 0 : item.amountCny > 0;
    if (!supported) {
      modalWarning(currency === 'USDT' ? '该产品暂不支持 USDT 认购' : '该产品暂不支持 RMB 认购');
      return;
    }
    setSubmitting(true);
    try {
      const message = await subscribeAppProduct({ productId, currency });
      modalSuccess(message);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '认购失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (!item) {
    return (
      <AppBackground source={images.pageBg} dim={false}>
        <PageHeader title="产品信息" />
        <RefreshableScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.empty}
          onRefresh={load}
        />
      </AppBackground>
    );
  }

  return (
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="产品信息" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        onRefresh={load}
      >
        <ProductSubscribePanel
          item={item}
          submitting={submitting}
          onSubscribeCny={() => void onSubscribe('CNY')}
          onSubscribeUsdt={() => void onSubscribe('USDT')}
        />
      </RefreshableScrollView>
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
});
