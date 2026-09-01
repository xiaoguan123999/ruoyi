import { router, useFocusEffect } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Linking,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import QRCode from 'react-native-qrcode-svg';

import { isGroupQrImageUrl } from '@/api/app-group-chat';
import { fetchAppServiceCenter } from '@/api/app-service';
import { ApiError } from '@/api/request';
import type { AppServiceCenter, AppServiceChannel, RuoyiUser } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { useAuth } from '@/hooks/useAuth';
import { colors } from '@/theme/colors';
import { isEmbeddableChatChannel, withChatVisitorParams } from '@/utils/online-chat';
import { modalError, modalWarning } from '@/utils/toast';

function ChannelQr({ channel }: { channel: AppServiceChannel }) {
  const qrUrl = channel.qrUrl?.trim() || '';
  const link = channel.linkUrl?.trim() || channel.value?.trim() || '';
  if (qrUrl) {
    if (isGroupQrImageUrl(qrUrl)) {
      return <Image source={{ uri: qrUrl }} style={styles.qrImage} contentFit="contain" />;
    }
    return (
      <View style={styles.qrCodeWrap}>
        <QRCode value={qrUrl} size={160} backgroundColor="#FFFFFF" color="#0B1730" />
      </View>
    );
  }
  if (channel.channelType === 'QR' && link) {
    return (
      <View style={styles.qrCodeWrap}>
        <QRCode value={link} size={160} backgroundColor="#FFFFFF" color="#0B1730" />
      </View>
    );
  }
  return null;
}

async function openChannel(channel: AppServiceChannel, user?: RuoyiUser | null) {
  const type = String(channel.channelType || '').toUpperCase();
  const value = channel.value?.trim() || '';
  const linkUrl = channel.linkUrl?.trim() || '';

  if (type === 'PHONE' && value) {
    const ok = await Linking.canOpenURL(`tel:${value}`);
    if (!ok) {
      modalWarning('无法拨打电话');
      return;
    }
    await Linking.openURL(`tel:${value}`);
    return;
  }

  const target = withChatVisitorParams(linkUrl || value, user);
  if (target && /^https?:\/\//i.test(target)) {
    if (isEmbeddableChatChannel({ ...channel, linkUrl: target })) {
      router.push('/service-chat');
      return;
    }
    const ok = await Linking.canOpenURL(target);
    if (!ok) {
      modalWarning('无法打开链接');
      return;
    }
    await Linking.openURL(target);
    return;
  }

  if (value) {
    modalWarning(value);
    return;
  }
  modalWarning('暂无可联系方式');
}

export default function ServiceScreen() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [center, setCenter] = useState<AppServiceCenter | null>(null);

  const load = useCallback(async () => {
    try {
      setCenter(await fetchAppServiceCenter());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取客服信息失败');
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
      <PageHeader title={center?.title?.trim() || '客服中心'} />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.content}
          showsVerticalScrollIndicator={false}
          onRefresh={load}
        >
          <GlassCard>
            <Text style={styles.title}>{center?.title?.trim() || '在线客服'}</Text>
            {center?.workTime ? (
              <Text style={styles.p}>工作时间 {center.workTime}</Text>
            ) : null}
            {center?.hint ? <Text style={styles.hint}>{center.hint}</Text> : null}
          </GlassCard>

          {!center?.channels.length ? (
            <Text style={styles.empty}>暂无客服渠道</Text>
          ) : (
            center.channels.map((channel) => (
              <GlassCard key={channel.channelId} style={styles.channelCard}>
                <Text style={styles.channelName}>{channel.name}</Text>
                {channel.value ? <Text style={styles.channelValue}>{channel.value}</Text> : null}
                {channel.remark ? <Text style={styles.channelRemark}>{channel.remark}</Text> : null}
                <ChannelQr channel={channel} />
                <View style={styles.btnWrap}>
                  <PrimaryButton title="联系客服" onPress={() => void openChannel(channel, user)} />
                </View>
              </GlassCard>
            ))
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
  title: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
  },
  p: {
    color: colors.muted,
    marginTop: 10,
    lineHeight: 22,
  },
  hint: {
    color: colors.muted,
    marginTop: 8,
    lineHeight: 20,
    fontSize: 13,
  },
  empty: {
    color: colors.muted,
    textAlign: 'center',
    marginTop: 24,
  },
  channelCard: {
    gap: 8,
  },
  channelName: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
  },
  channelValue: {
    color: colors.text,
    fontSize: 14,
  },
  channelRemark: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 20,
  },
  qrImage: {
    width: '100%',
    maxWidth: 200,
    aspectRatio: 1,
    alignSelf: 'center',
    marginTop: 8,
    borderRadius: 8,
  },
  qrCodeWrap: {
    alignSelf: 'center',
    marginTop: 8,
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
  },
  btnWrap: {
    marginTop: 8,
  },
});
