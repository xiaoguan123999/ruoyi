import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { Image } from 'expo-image';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, { Defs, LinearGradient, Path, Rect, Stop } from 'react-native-svg';
import { useStableSafeTop } from '@/hooks/useStableSafeTop';

import {
  appLogout,
  fetchAppProfile,
  formatBalance,
  isKycVerified,
  maskPhone,
  toNumberOrZero,
} from '@/api/app-auth';
import { fetchAppWallet } from '@/api/app-trade';
import type { AppWallet } from '@/api/types';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { UpdateConfirmModal } from '@/components/ui/UpdateConfirmModal';
import { useAuth } from '@/hooks/useAuth';
import { useVersionUpdatePrompt } from '@/hooks/useVersionUpdatePrompt';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalWarning, toastSuccess, toastThenNavigate } from '@/utils/toast';

function GradientPill({
  title,
  onPress,
}: {
  title: string;
  onPress: () => void;
}) {
  const [size, setSize] = useState({ width: 0, height: 0 });
  const gradId = `pill-${title}`;

  return (
    <Pressable
      onPress={onPress}
      onLayout={(event) => {
        const { width, height } = event.nativeEvent.layout;
        setSize({ width, height });
      }}
      style={({ pressed }) => [styles.pill, pressed && styles.pillPressed]}
    >
      {size.width > 0 ? (
        <Svg
          width={size.width}
          height={size.height}
          style={StyleSheet.absoluteFill}
        >
          <Defs>
            <LinearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
              <Stop offset="0%" stopColor="#5BA8FF" />
              <Stop offset="100%" stopColor="#2F7BFF" />
            </LinearGradient>
          </Defs>
          <Rect
            x="0"
            y="0"
            width={size.width}
            height={size.height}
            rx={21}
            ry={21}
            fill={`url(#${gradId})`}
          />
        </Svg>
      ) : (
        <View style={[StyleSheet.absoluteFill, styles.pillFallback]} />
      )}
      <Text style={styles.pillText}>{title}</Text>
    </Pressable>
  );
}
function BoostValueBox({ value }: { value: number }) {
  const [size, setSize] = useState({ width: 0, height: 0 });

  return (
    <View
      onLayout={(event) => {
        const { width, height } = event.nativeEvent.layout;
        setSize({ width, height });
      }}
      style={styles.boostBox}
    >
      {size.width > 0 ? (
        <Svg width={size.width} height={size.height} style={StyleSheet.absoluteFill}>
          <Defs>
            <LinearGradient id="boostGrad" x1="0" y1="0" x2="0" y2="1">
              <Stop offset="0%" stopColor="#F8D6AC" />
              <Stop offset="100%" stopColor="#EFBA8A" />
            </LinearGradient>
          </Defs>
          <Rect
            x="0"
            y="0"
            width={size.width}
            height={size.height}
            rx={8}
            ry={8}
            fill="url(#boostGrad)"
          />
        </Svg>
      ) : (
        <View style={[StyleSheet.absoluteFill, styles.boostFallback]} />
      )}
      <Text style={styles.boostLabel}>助力值</Text>
      <Text style={styles.boostValue}>{formatBalance(value)}</Text>
    </View>
  );
}

export default function ProfileScreen() {
  const router = useRouter();
  const top = useStableSafeTop();
  const { user, hydrated, isLoggedIn } = useAuth();
  const [wallet, setWallet] = useState<AppWallet | null>(null);
  const { isChecking, runVersionUpdateCheck, prompt, closePrompt } = useVersionUpdatePrompt();

  const load = useCallback(async () => {
    const [, nextWallet] = await Promise.all([
      fetchAppProfile().catch(() => null),
      fetchAppWallet().catch(() => null),
    ]);
    if (nextWallet) {
      setWallet(nextWallet);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  const logout = async () => {
    await appLogout();
    toastThenNavigate('已退出登录', () => router.replace('/sign-in'), { type: 'success' });
  };

  const checkUpdate = async () => {
    await runVersionUpdateCheck({
      onLatest: () => toastSuccess('已是最新版本'),
      onError: () => modalError('检查更新失败'),
      onOpenUrlFailed: () => modalError('无法打开下载地址'),
      onUnsupported: () => modalWarning('网页版请直接刷新页面'),
    });
  };

  const displayName = user?.realName?.trim() || '';
  const account = maskPhone(user?.phone || user?.userName);
  const inviteCode = user?.inviteCode?.trim() || '';
  const verified = isKycVerified(user?.kycStatus);
  const cnyAvailable = toNumberOrZero(wallet?.cnyAvailable);
  const usdtAvailable = toNumberOrZero(wallet?.usdtAvailable);
  const cnyProductIncome = toNumberOrZero(wallet?.cnyProductIncome);
  const usdtProductIncome = toNumberOrZero(wallet?.usdtProductIncome);
  const cnyAssistValue = toNumberOrZero(wallet?.cnyAssistValue);
  const usdtAssistValue = toNumberOrZero(wallet?.usdtAssistValue);
  const boostCny = toNumberOrZero(wallet?.cnyAssistWallet);
  const boostUsdt = toNumberOrZero(wallet?.usdtAssistWallet);
  const boostValue = boostCny > 0 ? boostCny : boostUsdt;

  // 有 token 就展示资产/菜单；不要等 user 对象，否则会出现整页空白
  const showBody = hydrated && isLoggedIn;

  return (
    <View style={styles.page}>
      <Image
        source={images.profileBg}
        style={StyleSheet.absoluteFill}
        contentFit="cover"
        contentPosition="top"
      />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{
          paddingTop: top + 18,
          paddingBottom: 24,
          paddingHorizontal: 18,
        }}
        onRefresh={load}
      >
        <View style={styles.user}>
          <Image source={images.avatar} style={styles.avatar} contentFit="cover" />
          <View style={styles.userMeta}>
            <View style={styles.userMainRow}>
              <View style={styles.userSubText}>
                <View style={styles.nameRow}>
                  {displayName ? (
                    <Text style={styles.name} numberOfLines={1}>
                      {displayName}
                    </Text>
                  ) : null}
                  <View style={[styles.badge, !verified && styles.badgeUnverified]}>
                    <Text style={styles.badgeText}>{verified ? '已认证' : '未认证'}</Text>
                  </View>
                </View>
                <Text style={styles.metaLine} numberOfLines={1}>
                  账号 {account || '--'}
                </Text>
                <Text style={styles.metaLine} numberOfLines={1}>
                  {inviteCode ? `邀请码 ${inviteCode}` : '邀请码 --'}
                </Text>
                <Text style={styles.slogan} numberOfLines={1}>
                  连接星空 · 智联未来
                </Text>
              </View>
              <BoostValueBox value={boostValue} />
            </View>
          </View>
        </View>

        {!showBody ? (
          <View style={styles.loading}>
            <ActivityIndicator color={colors.accent} />
          </View>
        ) : (
          <>
            <View style={[styles.card, styles.assetCard]}>
              <View style={styles.assetLabels}>
                <View style={styles.assetUnitCol} />
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=recharge')}
                >
                  <Text style={styles.assetLabel}>余额</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=product')}
                >
                  <Text style={styles.assetLabel}>产品收益</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=promo')}
                >
                  <Text style={styles.assetLabel}>推广收益</Text>
                </Pressable>
              </View>
              <View style={styles.assetDivider} />
              <View style={styles.assetValueRow}>
                <Text style={styles.assetUnit}>¥</Text>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=recharge')}
                >
                  <Text style={styles.assetValue}>{formatBalance(cnyAvailable)}</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=product')}
                >
                  <Text style={styles.assetValue}>{formatBalance(cnyProductIncome)}</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=promo')}
                >
                  <Text style={styles.assetValue}>{formatBalance(cnyAssistValue)}</Text>
                </Pressable>
              </View>
              <View style={styles.assetValueRow}>
                <Text style={styles.assetUnit}>USDT</Text>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=recharge')}
                >
                  <Text style={styles.assetValue}>{formatBalance(usdtAvailable)}</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=product')}
                >
                  <Text style={styles.assetValue}>{formatBalance(usdtProductIncome)}</Text>
                </Pressable>
                <Pressable
                  style={styles.assetCol}
                  onPress={() => router.push('/fund-details?tab=promo')}
                >
                  <Text style={styles.assetValue}>{formatBalance(usdtAssistValue)}</Text>
                </Pressable>
              </View>
              <View style={styles.actions}>
                <GradientPill title="充值" onPress={() => router.push('/recharge')} />
                <GradientPill title="提现" onPress={() => router.push('/withdraw')} />
              </View>
            </View>

            <View style={[styles.card, styles.quickCard]}>
              <View style={styles.quick}>
                <Quick icon={images.iconRecords} label="认购记录" onPress={() => router.push('/subscribe-records')} />
                <Quick icon={images.iconVip} label="会员等级" onPress={() => router.push('/levels')} />
                <Quick icon={images.iconFund} label="资金明细" onPress={() => router.push('/fund-details')} />
              </View>
            </View>

            <View style={[styles.card, styles.menuCard]}>
              <Menu icon={images.iconIdcard} label="实名认证" onPress={() => router.push('/kyc')} />
              <Menu icon={images.iconWallet} label="钱包管理" onPress={() => router.push('/wallet')} />
              <Menu icon={images.iconTeamSmall} label="我的团队" onPress={() => router.push('/team')} />
              <Menu icon={images.iconInfo} label="关于我们" onPress={() => router.push('/about')} />
              <Menu
                iconNode={<UpdateIcon />}
                label={isChecking ? '检查中…' : '检查更新'}
                onPress={() => void checkUpdate()}
              />
              <Menu icon={images.iconPassword} label="密码设置" onPress={() => router.push('/password')} />
              <Menu icon={images.iconLogout} label="退出登录" onPress={() => void logout()} last />
            </View>
          </>
        )}
      </RefreshableScrollView>
      <UpdateConfirmModal
        options={prompt?.options ?? null}
        onConfirm={() => closePrompt(true)}
        onCancel={() => closePrompt(false)}
      />
    </View>
  );
}

function Quick({ icon, label, onPress }: { icon: number; label: string; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={styles.quickItem}>
      <Image source={icon} style={styles.quickIcon} contentFit="contain" />
      <Text style={styles.quickLabel}>{label}</Text>
    </Pressable>
  );
}

function UpdateIcon() {
  return (
    <Svg width={22} height={22} viewBox="0 0 24 24" fill="none">
      <Path
        d="M20.5 12a8.5 8.5 0 1 1-2.5-6.02"
        stroke="#7EC8FF"
        strokeWidth={1.8}
        strokeLinecap="round"
      />
      <Path
        d="M20.5 4.5v5h-5"
        stroke="#7EC8FF"
        strokeWidth={1.8}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

function Menu({
  icon,
  iconNode,
  label,
  onPress,
  last,
}: {
  icon?: number;
  iconNode?: React.ReactNode;
  label: string;
  onPress: () => void;
  last?: boolean;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={[styles.menuRow, !last && styles.menuRowBorder]}
    >
      {iconNode ? (
        <View style={styles.menuIcon}>{iconNode}</View>
      ) : (
        <Image source={icon} style={styles.menuIcon} contentFit="contain" />
      )}
      <Text style={styles.menuLabel}>{label}</Text>
      <Text style={styles.menuChevron}>›</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  user: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    marginBottom: 36,
  },
  avatar: {
    width: 62,
    height: 62,
    borderRadius: 31,
  },
  userMeta: {
    flex: 1,
    minWidth: 0,
  },
  userMainRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  userSubText: {
    flex: 1,
    minWidth: 0,
    justifyContent: 'center',
    gap: 4,
  },
  boostBox: {
    width: 58,
    borderRadius: 8,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 4,
    paddingHorizontal: 6,
  },
  boostFallback: {
    backgroundColor: '#F8D6AC',
    borderRadius: 8,
  },
  boostLabel: {
    color: '#2A2208',
    fontSize: 12,
    fontWeight: '600',
    lineHeight: 16,
  },
  boostValue: {
    marginTop: 1,
    color: '#1A1505',
    fontSize: 16,
    fontWeight: '700',
    lineHeight: 20,
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    flexWrap: 'wrap',
  },
  name: {
    color: colors.text,
    fontSize: 22,
    fontWeight: '800',
    lineHeight: 28,
  },
  badge: {
    backgroundColor: 'rgba(90, 170, 255, 0.55)',
    borderRadius: 6,
    paddingHorizontal: 7,
    paddingVertical: 2,
  },
  badgeText: {
    color: colors.text,
    fontSize: 11,
    fontWeight: '600',
  },
  badgeUnverified: {
    backgroundColor: 'rgba(140, 160, 185, 0.4)',
  },
  metaLine: {
    color: 'rgba(210, 225, 245, 0.78)',
    fontSize: 14,
    lineHeight: 20,
  },
  slogan: {
    color: 'rgba(170, 195, 225, 0.72)',
    fontSize: 12,
    lineHeight: 16,
  },
  loading: {
    marginTop: 40,
    alignItems: 'center',
  },
  card: {
    backgroundColor: 'rgba(12, 28, 58, 0.78)',
    borderColor: 'rgba(100, 165, 230, 0.32)',
    borderWidth: 1,
    borderRadius: 14,
    marginBottom: 14,
  },
  assetCard: {
    paddingHorizontal: 12,
    paddingTop: 14,
    paddingBottom: 14,
  },
  assetLabels: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  assetUnitCol: {
    width: 44,
    marginRight: 6,
  },
  assetCol: {
    flex: 1,
    textAlign: 'left',
    paddingRight: 4,
  },
  assetDivider: {
    height: StyleSheet.hairlineWidth,
    backgroundColor: 'rgba(180, 205, 235, 0.35)',
    marginBottom: 12,
  },
  assetValueRow: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 26,
    marginBottom: 4,
  },
  assetUnit: {
    width: 44,
    marginRight: 6,
    color: colors.text,
    fontSize: 14,
    fontWeight: '700',
    textAlign: 'center',
  },
  assetLabel: {
    color: 'rgba(180, 200, 230, 0.78)',
    fontSize: 13,
  },
  assetValue: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '800',
    lineHeight: 26,
  },
  actions: {
    flexDirection: 'row',
    gap: 14,
    marginTop: 16,
  },
  pill: {
    flex: 1,
    height: 42,
    borderRadius: 21,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  pillPressed: {
    opacity: 0.88,
  },
  pillFallback: {
    backgroundColor: '#3B8CFF',
    borderRadius: 21,
  },
  pillText: {
    color: colors.text,
    fontWeight: '700',
    fontSize: 16,
    zIndex: 1,
  },
  quickCard: {
    paddingVertical: 16,
    paddingHorizontal: 8,
  },
  quick: {
    flexDirection: 'row',
  },
  quickItem: {
    flex: 1,
    alignItems: 'center',
    gap: 8,
  },
  quickIcon: {
    width: 48,
    height: 48,
  },
  quickLabel: {
    color: colors.text,
    fontSize: 13,
  },
  menuCard: {
    paddingHorizontal: 14,
    paddingVertical: 2,
    marginBottom: 8,
  },
  menuRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
  },
  menuRowBorder: {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: 'rgba(255,255,255,0.08)',
  },
  menuIcon: {
    width: 22,
    height: 22,
    marginRight: 12,
  },
  menuLabel: {
    color: colors.text,
    flex: 1,
    fontSize: 15,
  },
  menuChevron: {
    color: 'rgba(180, 200, 230, 0.65)',
    fontSize: 20,
    lineHeight: 22,
  },
});
