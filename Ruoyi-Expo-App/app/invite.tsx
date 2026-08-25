import { useFocusEffect } from 'expo-router';
import { useCallback, useMemo, useState } from 'react';
import { Image } from 'expo-image';
import { Platform, StyleSheet, Text, View } from 'react-native';
import QRCode from 'react-native-qrcode-svg';
import Svg, { Defs, LinearGradient, Stop, Text as SvgText } from 'react-native-svg';

import { displayText } from '@/api/app-auth';
import { fetchAppInvite } from '@/api/app-member';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { useAuth } from '@/hooks/useAuth';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { buildInviteRegisterUrl } from '@/utils/invite';

const softTitleFont = Platform.select({
  ios: 'PingFangSC-Medium',
  android: 'sans-serif-medium',
  default: 'system-ui',
});

export default function InviteScreen() {
  const { user } = useAuth();
  const [inviteCode, setInviteCode] = useState(displayText(user?.inviteCode));
  const [ruleText, setRuleText] = useState('');

  const load = useCallback(async () => {
    try {
      const data = await fetchAppInvite();
      setInviteCode(displayText(data.inviteCode || user?.inviteCode));
      setRuleText(data.ruleText?.trim() || '');
    } catch {
      setInviteCode(displayText(user?.inviteCode));
      setRuleText('');
    }
  }, [user?.inviteCode]);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  // 手机浏览器扫码 → H5 /sign-up，并回填邀请码（不用后端 API 地址）
  const registerUrl = useMemo(
    () => buildInviteRegisterUrl(inviteCode === '--' ? '' : inviteCode),
    [inviteCode],
  );

  return (
    <AppBackground source={images.pageBg} dim={false} contentPosition="top right">
      <PageHeader title="邀请好友" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
        onRefresh={load}
      >
        <View style={styles.hero}>
          <Svg width="100%" height={40}>
            <SvgText
              x={2}
              y={30}
              fill="#FFFFFF"
              fontSize={28}
              fontWeight="700"
              fontFamily={softTitleFont}
              letterSpacing={2}
              transform="skewX(-12)"
            >
              邀请好友
            </SvgText>
          </Svg>
          <Svg width="100%" height={34} style={styles.h2Svg}>
            <Defs>
              <LinearGradient id="inviteSubGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <Stop offset="0%" stopColor="#FFFFFF" />
                <Stop offset="35%" stopColor="#FFF8F0" />
                <Stop offset="100%" stopColor="#F0C9A8" />
              </LinearGradient>
            </Defs>
            <SvgText
              x={2}
              y={26}
              fill="url(#inviteSubGrad)"
              fontSize={22}
              fontWeight="700"
              fontFamily={softTitleFont}
              letterSpacing={1.6}
              transform="skewX(-12)"
            >
              一起探索星辰大海
            </SvgText>
          </Svg>
        </View>

        <View style={styles.card}>
          <View style={styles.captionRow}>
            <View style={styles.captionLine} />
            <Text style={styles.caption}>我的邀请码</Text>
            <View style={styles.captionLine} />
          </View>

          <View style={styles.codeBox}>
            <Text style={styles.code}>{inviteCode}</Text>
          </View>

          <View style={styles.qrWrap}>
            {registerUrl ? (
              <QRCode value={registerUrl} size={132} backgroundColor="#FFFFFF" color="#0B1730" />
            ) : (
              <View style={styles.qrPlaceholder} />
            )}
          </View>

          {ruleText ? <Text style={styles.ruleText}>{ruleText}</Text> : null}

          <Image source={images.inviteFlow} style={styles.flow} contentFit="contain" />
        </View>
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 20,
    paddingBottom: 28,
  },
  hero: {
    marginTop: 22,
    marginBottom: 8,
  },
  h2Svg: {
    marginTop: -2,
  },
  card: {
    marginTop: 18,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    backgroundColor: 'rgba(18, 36, 78, 0.92)',
    paddingHorizontal: 16,
    paddingTop: 18,
    paddingBottom: 16,
    alignItems: 'center',
  },
  captionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 14,
  },
  captionLine: {
    width: 48,
    height: StyleSheet.hairlineWidth,
    backgroundColor: 'rgba(180, 205, 240, 0.55)',
  },
  caption: {
    color: 'rgba(210, 225, 255, 0.9)',
    fontSize: 14,
  },
  codeBox: {
    minWidth: 168,
    borderWidth: 1,
    borderColor: 'rgba(140, 190, 255, 0.55)',
    borderRadius: 6,
    paddingHorizontal: 28,
    paddingVertical: 10,
    alignItems: 'center',
  },
  code: {
    color: colors.text,
    fontSize: 28,
    fontWeight: '800',
    letterSpacing: 4,
  },
  qrWrap: {
    width: 148,
    height: 148,
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    marginTop: 18,
    marginBottom: 18,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  qrPlaceholder: {
    width: 148,
    height: 148,
    backgroundColor: '#D9D9D9',
  },
  ruleText: {
    alignSelf: 'stretch',
    marginTop: 4,
    marginBottom: 14,
    color: 'rgba(200, 215, 245, 0.88)',
    fontSize: 13,
    lineHeight: 20,
    textAlign: 'left',
  },
  flow: {
    width: '100%',
    height: 168,
  },
});
