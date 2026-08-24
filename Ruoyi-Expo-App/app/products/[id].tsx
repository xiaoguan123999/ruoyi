import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { fetchAppProductSeriesWithItems } from '@/api/app-product';
import { PageHeader } from '@/components/ui/PageHeader';
import { ProductCard } from '@/components/ui/ProductCard';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import type { ProductSeries } from '@/types/product';
import { colors } from '@/theme/colors';

export default function ProductSeriesScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const [series, setSeries] = useState<ProductSeries | null>(null);

  const load = useCallback(async () => {
    if (!id) {
      setSeries(null);
      return;
    }
    try {
      setSeries(await fetchAppProductSeriesWithItems(id));
    } catch {
      setSeries(null);
    }
  }, [id]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <PageHeader title={series?.name ?? '产品系列'} />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        onRefresh={load}
      >
        {series?.items.map((item) => (
          <ProductCard
            key={item.id}
            item={item}
            onPress={() => router.push(`/products/subscribe/${item.id}`)}
          />
        ))}
        {!series?.items.length ? <Text style={styles.empty}>暂无产品</Text> : null}
      </RefreshableScrollView>
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
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 40,
    fontSize: 14,
  },
});
