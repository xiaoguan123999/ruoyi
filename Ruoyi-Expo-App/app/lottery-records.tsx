import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';

import { fetchLotteryRecords, type AppLotteryRecord } from '@/api/app-lottery';
import { ApiError } from '@/api/request';
import { Text } from '@/components/ui/AppText';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

function RecordRow({ item }: { item: AppLotteryRecord }) {
  return (
    <View style={styles.card}>
      <View style={styles.cardTop}>
        <Text style={styles.prizeName} numberOfLines={1}>
          {item.prizeName}
        </Text>
        <Text style={styles.status}>{item.grantStatusLabel}</Text>
      </View>
      <View style={styles.cardMeta}>
        <Text style={styles.meta}>第 {item.drawCount} 次</Text>
        {item.createTime ? <Text style={styles.meta}>{item.createTime}</Text> : null}
      </View>
    </View>
  );
}

export default function LotteryRecordsScreen() {
  const [loading, setLoading] = useState(true);
  const [records, setRecords] = useState<AppLotteryRecord[]>([]);

  const load = useCallback(async () => {
    try {
      const list = await fetchLotteryRecords({ pageNum: 1, pageSize: 50 });
      setRecords(list);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取中奖记录失败');
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
    <AppBackground>
      <PageHeader title="中奖记录" />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          onRefresh={load}
        >
          {records.length === 0 ? (
            <Text style={styles.emptyText}>暂无中奖记录</Text>
          ) : (
            records.map((item) => <RecordRow key={item.recordId} item={item} />)
          )}
        </RefreshableScrollView>
      )}
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  loadingWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 28,
    gap: 10,
  },
  emptyText: {
    marginTop: 80,
    textAlign: 'center',
    color: colors.muted,
    fontSize: 14,
  },
  card: {
    backgroundColor: '#0B1730',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 14,
    gap: 8,
  },
  cardTop: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  prizeName: {
    flex: 1,
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
  },
  status: {
    color: colors.accent,
    fontSize: 13,
    fontWeight: '600',
  },
  cardMeta: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  meta: {
    color: 'rgba(210, 222, 240, 0.75)',
    fontSize: 12,
  },
});
