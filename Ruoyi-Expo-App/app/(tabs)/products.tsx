import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { ApiError } from '@/api/request';
import { fetchAppProductItems } from '@/api/app-trade';
import { ProductCard, type ProductItem } from '@/components/ui/ProductCard';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function ProductsScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState<ProductItem[]>([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const list = await fetchAppProductItems();
      setProducts(list);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取产品列表失败');
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  return (
    <View style={styles.page}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={[styles.content, { paddingTop: insets.top + 8 }]}
      >
        <View style={styles.hero}>
          <Text style={styles.heroTitle}>连接星空 智联未来</Text>
          <Text style={styles.heroSub}>以科技连接万物 · 让星辰触手可及</Text>
        </View>

        {loading ? (
          <View style={styles.loadingWrap}>
            <ActivityIndicator color={colors.accent} />
          </View>
        ) : products.length === 0 ? (
          <Text style={styles.empty}>暂无产品</Text>
        ) : (
          <View style={styles.list}>
            {products.map((item) => (
              <ProductCard
                key={item.id}
                item={item}
                onPress={() => router.push(`/products/subscribe/${item.id}`)}
              />
            ))}
          </View>
        )}
      </ScrollView>
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
    paddingHorizontal: 16,
    gap: 16,
  },
  loadingWrap: {
    paddingVertical: 48,
    alignItems: 'center',
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 40,
    fontSize: 14,
  },
});
