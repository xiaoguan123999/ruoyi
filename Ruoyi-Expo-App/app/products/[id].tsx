import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { ActivityIndicator, ScrollView, StyleSheet, Text, View } from 'react-native';

import { ApiError } from '@/api/request';
import { fetchAppProductItems } from '@/api/app-trade';
import { PageHeader } from '@/components/ui/PageHeader';
import { ProductCard, type ProductItem } from '@/components/ui/ProductCard';
import { getProductSeries } from '@/constants/mock';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function ProductSeriesScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const series = getProductSeries(id);
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState<ProductItem[]>(series?.items ?? []);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const list = await fetchAppProductItems();
      setProducts(list.length ? list : series?.items ?? []);
    } catch (error) {
      setProducts(series?.items ?? []);
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取产品失败');
      }
    } finally {
      setLoading(false);
    }
  }, [series?.items]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <PageHeader title={series?.name ?? '产品列表'} />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.content}
        >
          {products.length === 0 ? (
            <Text style={styles.empty}>暂无产品</Text>
          ) : (
            products.map((item) => (
              <ProductCard
                key={item.id}
                item={item}
                onPress={() => router.push(`/products/subscribe/${item.id}`)}
              />
            ))
          )}
        </ScrollView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
    gap: 16,
  },
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 40,
  },
});
