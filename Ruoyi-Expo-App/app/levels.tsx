import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import { displayText, formatBalance } from '@/api/app-auth';
import { fetchAppLevels } from '@/api/app-member';
import { ApiError } from '@/api/request';
import type { AppLevel } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { useAuth } from '@/hooks/useAuth';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

export default function LevelsScreen() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [levels, setLevels] = useState<AppLevel[]>([]);

  const load = useCallback(async () => {
    try {
      const list = await fetchAppLevels();
      setLevels(list);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取会员等级失败');
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
    <AppBackground source={images.pageBg} dim={false}>
      <PageHeader title="会员等级" />
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
          {user?.levelName ? (
            <GlassCard style={styles.currentCard}>
              <Text style={styles.currentLabel}>当前等级</Text>
              <Text style={styles.currentName}>{displayText(user.levelName)}</Text>
            </GlassCard>
          ) : null}

          {levels.length === 0 ? (
            <Text style={styles.empty}>暂无等级配置</Text>
          ) : (
            levels.map((level) => {
              const current = user?.levelId === level.levelId || user?.levelName === level.levelName;
              return (
                <GlassCard key={level.levelId} style={[styles.card, current && styles.cardCurrent]}>
                  <View style={styles.head}>
                    <Text style={styles.name}>{level.levelName}</Text>
                    {current ? (
                      <View style={styles.badge}>
                        <Text style={styles.badgeText}>当前</Text>
                      </View>
                    ) : null}
                  </View>
                  <Text style={styles.row}>
                    最低充值（CNY）：{formatBalance(level.minRechargeCny)}
                  </Text>
                  <Text style={styles.row}>
                    最低充值（USDT）：{formatBalance(level.minRechargeUsdt)}
                  </Text>
                  <Text style={styles.row}>
                    最低有效成员：{formatBalance(level.minValidMembers)}
                  </Text>
                  {level.remark ? <Text style={styles.remark}>{level.remark}</Text> : null}
                </GlassCard>
              );
            })
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
    gap: 12,
  },
  currentCard: {
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
  },
  currentLabel: {
    color: colors.muted,
    fontSize: 13,
  },
  currentName: {
    color: colors.gold,
    fontSize: 22,
    fontWeight: '800',
    marginTop: 6,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 40,
  },
  card: {
    backgroundColor: 'rgba(23, 43, 88, 0.94)',
    borderColor: 'rgba(98, 150, 220, 0.24)',
  },
  cardCurrent: {
    borderColor: colors.gold,
  },
  head: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  name: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
  },
  badge: {
    backgroundColor: 'rgba(232, 195, 106, 0.18)',
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderWidth: 1,
    borderColor: 'rgba(232, 195, 106, 0.45)',
  },
  badgeText: {
    color: colors.gold,
    fontSize: 12,
    fontWeight: '600',
  },
  row: {
    color: 'rgba(210, 225, 255, 0.9)',
    fontSize: 14,
    lineHeight: 24,
  },
  remark: {
    color: colors.muted,
    fontSize: 13,
    marginTop: 8,
    lineHeight: 20,
  },
});
