import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Image } from 'expo-image';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, { Defs, LinearGradient, Stop, Text as SvgText } from 'react-native-svg';
import * as Clipboard from 'expo-clipboard';

import { fetchAppInvite } from '@/api/app-member';
import { ApiError } from '@/api/request';
import type { AppInviteInfo } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { useAuth } from '@/hooks/useAuth';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess } from '@/utils/toast';

const softTitleFont = Platform.select({
  ios: 'PingFangSC-Medium',
  android: 'sans-serif-medium',
  default: 'system-ui',
});

export default function InviteScreen() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [invite, setInvite] = useState<AppInviteInfo>({
    inviteCode: user?.inviteCode ?? '',
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchAppInvite();
      setInvite(data);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取邀请信息失败');
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

  const inviteCode = invite.inviteCode || user?.inviteCode || '—';

  const onCopy = async () => {
    if (!inviteCode || inviteCode === '—') {
      return;
    }
    try {
      await Clipboard.setStringAsync(invite.inviteUrl || inviteCode);
      modalSuccess(invite.inviteUrl ? '邀请链接已复制' : '邀请码已复制');
    } catch {
      modalError('复制失败');
    }
  };

  return (
    <AppBackground source={images.pageBg} dim={false} contentPosition="top right">
      <PageHeader title="邀请好友" />
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
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

          {loading ? (
            <ActivityIndicator color={colors.accent} style={{ marginVertical: 24 }} />
          ) : (
            <>
              <Pressable style={styles.codeBox} onPress={() => void onCopy()}>
                <Text style={styles.code}>{inviteCode}</Text>
              </Pressable>
              <Text style={styles.copyHint}>点击邀请码可复制</Text>

              {invite.qrCode ? (
                <Image source={{ uri: invite.qrCode }} style={styles.qr} contentFit="contain" />
              ) : (
                <View style={styles.qr} />
              )}

              {invite.inviteCount !== undefined ? (
                <Text style={styles.count}>已邀请 {invite.inviteCount} 人</Text>
              ) : null}
            </>
          )}

          <Image source={images.inviteFlow} style={styles.flow} contentFit="contain" />
        </View>
      </ScrollView>
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
  copyHint: {
    color: colors.muted,
    fontSize: 12,
    marginTop: 8,
  },
  qr: {
    width: 148,
    height: 148,
    backgroundColor: '#D9D9D9',
    borderRadius: 8,
    marginTop: 18,
    marginBottom: 12,
  },
  count: {
    color: colors.muted,
    fontSize: 13,
    marginBottom: 8,
  },
  flow: {
    width: '100%',
    height: 168,
  },
});
