import { useRouter, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { fetchAppProductSeriesList } from '@/api/app-product';
import { ProductSeriesCard } from '@/components/ui/ProductSeriesCard';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import type { ProductSeries } from '@/types/product';
import { colors } from '@/theme/colors';

export default function ProductsScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [seriesList, setSeriesList] = useState<ProductSeries[]>([]);

  const load = useCallback(async () => {
    try {
      setSeriesList(await fetchAppProductSeriesList());
    } catch {
      setSeriesList([]);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={[styles.content, { paddingTop: insets.top + 8 }]}
        onRefresh={load}
      >
        <View style={styles.hero}>
          <Text style={styles.heroTitle}>连接星空 智联未来</Text>
          <Text style={styles.heroSub}>以科技连接万物 · 让星辰触手可及</Text>
        </View>

        <View style={styles.list}>
          {seriesList.map((series) => (
            <ProductSeriesCard
              key={series.id}
              name={series.name}
              cover={series.cover}
              onPress={() => router.push(`/products/${series.id}`)}
            />
          ))}
        </View>
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
    paddingBottom: 24,
  },
  hero: {
    paddingHorizontal: 16,
    paddingBottom: 12,
  },
  heroTitle: {
    color: colors.text,
    fontSize: 24,
    fontWeight: '800',
  },
  heroSub: {
    color: colors.muted,
    marginTop: 6,
    fontSize: 13,
  },
  list: {
    marginTop: 12,
    gap: 16,
  },
});
