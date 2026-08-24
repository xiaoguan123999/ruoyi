import { useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { StyleSheet } from 'react-native';

import { fetchAppProfile } from '@/api/app-auth';
import { setAppPayPassword } from '@/api/app-member';
import { fetchAppProductById } from '@/api/app-product';
import { subscribeAppProduct } from '@/api/app-trade';
import { ApiError } from '@/api/request';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { PayPasswordModal, type PayPasswordMode } from '@/components/ui/PayPasswordModal';
import { ProductSubscribePanel } from '@/components/ui/ProductSubscribePanel';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import type { ProductItem } from '@/types/product';
import { images } from '@/constants/images';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

export default function ProductSubscribeScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [item, setItem] = useState<ProductItem | undefined>();
  const [submitting, setSubmitting] = useState(false);
  const [payVisible, setPayVisible] = useState(false);
  const [payMode, setPayMode] = useState<PayPasswordMode>('verify');
  const [hasPayPassword, setHasPayPassword] = useState(true);
  const [pendingCurrency, setPendingCurrency] = useState<'CNY' | 'USDT' | null>(null);

  const load = useCallback(async () => {
    try {
      const [product, profile] = await Promise.all([
        fetchAppProductById(id),
        fetchAppProfile().catch(() => null),
      ]);
      setItem(product);
      setHasPayPassword(profile?.hasPayPassword !== false);
    } catch {
      setItem(undefined);
    }
  }, [id]);

  useEffect(() => {
    void load();
  }, [load]);

  const requestSubscribe = (currency: 'CNY' | 'USDT') => {
    if (!item || submitting) {
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
    setPendingCurrency(currency);
    setPayMode(hasPayPassword ? 'verify' : 'set');
    setPayVisible(true);
  };

  const closePaySheet = () => {
    setPayVisible(false);
    setPendingCurrency(null);
    setPayMode('verify');
  };

  const doSubscribe = async (payPassword: string) => {
    if (!item || !pendingCurrency) {
      return;
    }
    const productId = item.apiId ?? Number(item.id);
    const message = await subscribeAppProduct({
      productId,
      currency: pendingCurrency,
      payPassword,
    });
    closePaySheet();
    requestAnimationFrame(() => modalSuccess(message));
  };

  const onConfirmPay = async (payPassword: string) => {
    if (!item || !pendingCurrency || submitting) {
      return;
    }
    if (payPassword.length < 4) {
      modalWarning('请输入支付密码');
      return;
    }

    setSubmitting(true);
    try {
      if (payMode === 'set') {
        await setAppPayPassword(payPassword);
        setHasPayPassword(true);
        await doSubscribe(payPassword);
        return;
      }
      await doSubscribe(payPassword);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        const message = error instanceof ApiError ? error.message : '认购失败';
        modalError(message);
        // 后端提示未设置时，切到设置模式
        if (message.includes('请先设置支付密码')) {
          setHasPayPassword(false);
          setPayMode('set');
        }
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
          onSubscribeCny={() => requestSubscribe('CNY')}
          onSubscribeUsdt={() => requestSubscribe('USDT')}
        />
      </RefreshableScrollView>

      <PayPasswordModal
        visible={payVisible}
        mode={payMode}
        submitting={submitting}
        title={
          payMode === 'set'
            ? '设置支付密码'
            : pendingCurrency === 'USDT'
              ? 'USDT 认购验证'
              : 'RMB 认购验证'
        }
        onCancel={() => {
          if (submitting) {
            return;
          }
          closePaySheet();
        }}
        onConfirm={(pwd) => void onConfirmPay(pwd)}
      />
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
