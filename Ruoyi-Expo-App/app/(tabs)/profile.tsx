import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback } from 'react';
import { Image } from 'expo-image';
import { ActivityIndicator, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import {
  appLogout,
  fetchAppProfile,
  formatBalance,
  isKycVerified,
  maskPhone,
} from '@/api/app-auth';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { useAuth } from '@/hooks/useAuth';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { toastThenNavigate } from '@/utils/toast';

export default function ProfileScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { user, hydrated } = useAuth();

  useFocusEffect(
    useCallback(() => {
      void fetchAppProfile().catch(() => {
      });
    }, []),
  );

  const logout = async () => {
    await appLogout();
    toastThenNavigate('已退出登录', () => router.replace('/sign-in'), { type: 'success' });
  };

  const displayName = user?.nickName || user?.userName || '会员';
  const displayPhone = maskPhone(user?.phone || user?.userName);
  const verified = isKycVerified(user?.kycStatus);

  return (
    <AppBackground>
      <ScrollView contentContainerStyle={{ paddingTop: insets.top + 12, paddingBottom: 28, paddingHorizontal: 16 }}>
        <View style={styles.user}>
          <Image source={images.avatar} style={styles.avatar} contentFit="cover" />
          <View style={{ flex: 1 }}>
            <View style={styles.nameRow}>
              <Text style={styles.name}>{displayName}</Text>
              {verified ? (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>已认证</Text>
                </View>
              ) : null}
              {user?.levelName ? (
                <View style={styles.levelBadge}>
                  <Text style={styles.levelBadgeText}>{user.levelName}</Text>
                </View>
              ) : null}
            </View>
            <Text style={styles.phone}>{displayPhone || '—'}</Text>
            <Text style={styles.slogan}>连接星空 · 智联未来</Text>
          </View>
        </View>

        {!hydrated || !user ? (
          <View style={styles.loading}>
            <ActivityIndicator color={colors.accent} />
          </View>
        ) : (
          <>
            <GlassCard style={{ marginTop: 16 }}>
              <View style={styles.assetRow}>
                <Asset
                  label="余额"
                  value={`¥ ${formatBalance(user.cnyAvailable)}`}
                  sub={`USDT ${formatBalance(user.usdtAvailable)}`}
                />
                <Asset label="冻结(CNY)" value={formatBalance(user.cnyFrozen)} />
                <Asset label="团队人数" value={formatBalance(user.teamCount)} />
              </View>
              <View style={styles.actions}>
                <Pressable style={styles.pill} onPress={() => router.push('/recharge')}>
                  <Text style={styles.pillText}>充值</Text>
                </Pressable>
                <Pressable style={styles.pill} onPress={() => router.push('/withdraw')}>
                  <Text style={styles.pillText}>提现</Text>
                </Pressable>
              </View>
            </GlassCard>

            <GlassCard style={{ marginTop: 12 }}>
              <View style={styles.quick}>
                <Quick icon={images.iconRecords} label="认购记录" onPress={() => router.push('/subscribe-records')} />
                <Quick icon={images.iconVip} label="会员等级" onPress={() => router.push('/levels')} />
                <Quick icon={images.iconFund} label="资金明细" onPress={() => router.push('/fund-details')} />
              </View>
            </GlassCard>

            <GlassCard style={{ marginTop: 12, paddingVertical: 4 }}>
              <Menu icon={images.iconIdcard} label="实名认证" onPress={() => router.push('/kyc')} />
              <Menu icon={images.iconTeamSmall} label="我的团队" onPress={() => router.push('/team')} />
              <Menu icon={images.iconInfo} label="关于我们" onPress={() => router.push('/about')} />
              <Menu icon={images.iconPassword} label="密码设置" onPress={() => router.push('/password')} />
              <Menu icon={images.iconLogout} label="退出登录" onPress={() => void logout()} last />
            </GlassCard>
          </>
        )}
      </ScrollView>
    </AppBackground>
  );
}

function Asset({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <View style={{ flex: 1, alignItems: 'center' }}>
      <Text style={{ color: colors.muted, fontSize: 12 }}>{label}</Text>
      <Text style={{ color: colors.text, fontSize: 18, fontWeight: '800', marginTop: 6 }}>{value}</Text>
      {sub ? <Text style={{ color: colors.muted, fontSize: 12, marginTop: 2 }}>{sub}</Text> : null}
    </View>
  );
}

function Quick({ icon, label, onPress }: { icon: number; label: string; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={{ flex: 1, alignItems: 'center', gap: 8 }}>
      <Image source={icon} style={{ width: 42, height: 42 }} contentFit="contain" />
      <Text style={{ color: colors.text, fontSize: 12 }}>{label}</Text>
    </Pressable>
  );
}

function Menu({
  icon,
  label,
  onPress,
  last,
}: {
  icon: number;
  label: string;
  onPress: () => void;
  last?: boolean;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={{
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 13,
        borderBottomWidth: last ? 0 : StyleSheet.hairlineWidth,
        borderBottomColor: 'rgba(255,255,255,0.08)',
      }}
    >
      <Image source={icon} style={{ width: 22, height: 22, marginRight: 10 }} contentFit="contain" />
      <Text style={{ color: colors.text, flex: 1, fontSize: 15 }}>{label}</Text>
      <Text style={{ color: colors.muted, fontSize: 18 }}>›</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  user: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: { width: 64, height: 64, borderRadius: 32 },
  nameRow: { flexDirection: 'row', alignItems: 'center', gap: 8, flexWrap: 'wrap' },
  name: { color: colors.text, fontSize: 22, fontWeight: '800' },
  badge: { backgroundColor: colors.accent, borderRadius: 8, paddingHorizontal: 6, paddingVertical: 2 },
  badgeText: { color: colors.text, fontSize: 11 },
  levelBadge: {
    backgroundColor: 'rgba(232, 195, 106, 0.2)',
    borderRadius: 8,
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderWidth: 1,
    borderColor: 'rgba(232, 195, 106, 0.35)',
  },
  levelBadgeText: { color: colors.gold, fontSize: 11 },
  phone: { color: colors.muted, marginTop: 4 },
  slogan: { color: colors.muted, marginTop: 4, fontSize: 12 },
  loading: { marginTop: 40, alignItems: 'center' },
  assetRow: { flexDirection: 'row', paddingVertical: 6 },
  actions: { flexDirection: 'row', gap: 12, marginTop: 14 },
  pill: {
    flex: 1,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pillText: { color: colors.text, fontWeight: '700', fontSize: 15 },
  quick: { flexDirection: 'row' },
});
