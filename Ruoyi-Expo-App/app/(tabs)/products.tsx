import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import {
  fetchAppProductSeriesList,
  fetchAppProductSeriesWithItems,
} from '@/api/app-product';
import { ProductCard } from '@/components/ui/ProductCard';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import type { ProductItem, ProductSeries } from '@/types/product';

export default function ProductsScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { seriesId: seriesIdParam } = useLocalSearchParams<{ seriesId?: string }>();
  const [seriesList, setSeriesList] = useState<ProductSeries[]>([]);
  const [activeSeriesId, setActiveSeriesId] = useState<string>('');
  const [items, setItems] = useState<ProductItem[]>([]);
  const [loadingSeries, setLoadingSeries] = useState(true);
  const [loadingItems, setLoadingItems] = useState(false);
  const activeSeriesIdRef = useRef(activeSeriesId);
  activeSeriesIdRef.current = activeSeriesId;

  const loadItems = useCallback(async (seriesId: string) => {
    if (!seriesId) {
      setItems([]);
      return;
    }
    setLoadingItems(true);
    try {
      const detail = await fetchAppProductSeriesWithItems(seriesId);
      setItems(detail?.items ?? []);
    } catch {
      setItems([]);
    } finally {
      setLoadingItems(false);
    }
  }, []);

  const loadSeries = useCallback(async () => {
    setLoadingSeries(true);
    try {
      const list = await fetchAppProductSeriesList();
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

  return (
    <View style={styles.page}>
      <View style={[styles.header, { paddingTop: Math.max(insets.top, 12) + 8 }]}>
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
            items.map((item) => (
              <ProductCard
                key={item.id}
                item={item}
                onPress={() => router.push(`/products/subscribe/${item.id}`)}
              />
            ))
          )}
        </RefreshableScrollView>
      )}
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
