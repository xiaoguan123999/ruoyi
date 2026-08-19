import { useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { images } from '@/constants/images';
import { mockUser } from '@/constants/mock';
import { colors } from '@/theme/colors';
import { mockSignOut } from '@/utils/mock-auth';
import { toast } from '@/utils/toast';

export default function ProfileScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  const logout = async () => {
    await mockSignOut();
    toast('已退出登录');
    router.replace('/sign-in');
  };

  return (
    <AppBackground>
      <ScrollView contentContainerStyle={{ paddingTop: insets.top + 12, paddingBottom: 28, paddingHorizontal: 16 }}>
        <View style={styles.user}>
          <Image source={images.avatar} style={styles.avatar} contentFit="cover" />
          <View style={{ flex: 1 }}>
            <View style={styles.nameRow}>
              <Text style={styles.name}>{mockUser.nickName}</Text>
              {mockUser.verified ? (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>已认证</Text>
                </View>
              ) : null}
            </View>
            <Text style={styles.phone}>{mockUser.phone}</Text>
            <Text style={styles.slogan}>连接星空 · 智联未来</Text>
          </View>
        </View>

        <GlassCard style={{ marginTop: 16 }}>
          <View style={styles.assetRow}>
            <Asset label="余额" value={`¥ ${mockUser.balanceCny}`} sub={`USDT ${mockUser.balanceUsdt}`} />
            <Asset label="产品收益" value={`${mockUser.productIncome}`} />
            <Asset label="助力值" value={`${mockUser.assistValue}`} />
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
            <Quick icon={images.iconVip} label="会员等级" onPress={() => toast('会员等级功能开发中')} />
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
  nameRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  name: { color: colors.text, fontSize: 22, fontWeight: '800' },
  badge: { backgroundColor: colors.accent, borderRadius: 8, paddingHorizontal: 6, paddingVertical: 2 },
  badgeText: { color: colors.text, fontSize: 11 },
  phone: { color: colors.muted, marginTop: 4 },
  slogan: { color: colors.muted, marginTop: 4, fontSize: 12 },
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
