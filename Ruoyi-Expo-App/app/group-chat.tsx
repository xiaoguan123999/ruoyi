import { useFocusEffect } from 'expo-router';
import { Image } from 'expo-image';
import { useCallback, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import QRCode from 'react-native-qrcode-svg';

import { fetchAppGroupChat, isGroupQrImageUrl } from '@/api/app-group-chat';
import { ApiError } from '@/api/request';
import type { AppGroupChatItem } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError } from '@/utils/toast';

function GroupQr({ item }: { item: AppGroupChatItem }) {
  const qrUrl = item.qrUrl?.trim() || '';
  if (!qrUrl) {
    return <View style={styles.qrPlaceholder} />;
  }
  if (isGroupQrImageUrl(qrUrl)) {
    return <Image source={{ uri: qrUrl }} style={styles.qrImage} contentFit="contain" />;
  }
  return (
    <View style={styles.qrCodeWrap}>
      <QRCode value={qrUrl} size={200} backgroundColor="#FFFFFF" color="#0B1730" />
    </View>
  );
}

export default function GroupChatScreen() {
  const [loading, setLoading] = useState(true);
  const [items, setItems] = useState<AppGroupChatItem[]>([]);

  const load = useCallback(async () => {
    try {
      setItems(await fetchAppGroupChat());
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取官方群聊失败');
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
    <AppBackground source={images.pageBg} dim={false} contentPosition="top right">
      <PageHeader title="官方群聊" />
      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator color={colors.accent} />
        </View>
      ) : (
        <RefreshableScrollView
          style={{ flex: 1 }}
          contentContainerStyle={styles.body}
          showsVerticalScrollIndicator={false}
          onRefresh={load}
        >
          {items.length === 0 ? (
            <Text style={styles.empty}>暂无群聊信息</Text>
          ) : (
            items.map((item) => (
              <View key={item.id} style={styles.card}>
                <GroupQr item={item} />
                <Text style={styles.hint}>{item.hint || '扫码进群'}</Text>
                {item.remark ? <Text style={styles.remark}>{item.remark}</Text> : null}
              </View>
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
  body: {
    flexGrow: 1,
    paddingHorizontal: 28,
    paddingTop: 24,
    paddingBottom: 28,
    alignItems: 'center',
    gap: 16,
  },
  card: {
    width: '100%',
    maxWidth: 320,
    borderRadius: 14,
    paddingHorizontal: 28,
    paddingTop: 28,
    paddingBottom: 24,
    alignItems: 'center',
    backgroundColor: 'rgba(10, 24, 52, 0.78)',
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.28)',
  },
  qrPlaceholder: {
    width: '100%',
    aspectRatio: 1,
    maxWidth: 240,
    borderRadius: 8,
    backgroundColor: '#C9CED6',
  },
  qrImage: {
    width: '100%',
    aspectRatio: 1,
    maxWidth: 240,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
  },
  qrCodeWrap: {
    padding: 12,
    borderRadius: 8,
    backgroundColor: '#FFFFFF',
  },
  hint: {
    marginTop: 18,
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 1,
  },
  remark: {
    marginTop: 8,
    color: colors.muted,
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 20,
  },
  empty: {
    color: colors.muted,
    marginTop: 24,
  },
});
