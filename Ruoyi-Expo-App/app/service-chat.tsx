import { useEffect, useState } from 'react';
import { ActivityIndicator, Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import { useRouter } from 'expo-router';

import { fetchAppServiceCenter } from '@/api/app-service';
import { ApiError } from '@/api/request';
import { OnlineChatFrame } from '@/components/ui/OnlineChatFrame';
import { useAuth } from '@/hooks/useAuth';
import { useStableSafeTop } from '@/hooks/useStableSafeTop';
import { pickOnlineChatChannel, resolveChatChannelUrl } from '@/utils/online-chat';
import { modalError } from '@/utils/toast';

function useWebKeyboardInset() {
  const [inset, setInset] = useState(0);

  useEffect(() => {
    if (Platform.OS !== 'web' || typeof window === 'undefined') {
      return;
    }
    const viewport = window.visualViewport;
    if (!viewport) {
      return;
    }
    const sync = () => {
      const next = Math.max(0, window.innerHeight - viewport.height - viewport.offsetTop);
      setInset(next > 40 ? next : 0);
    };
    sync();
    viewport.addEventListener('resize', sync);
    viewport.addEventListener('scroll', sync);
    return () => {
      viewport.removeEventListener('resize', sync);
      viewport.removeEventListener('scroll', sync);
    };
  }, []);

  return inset;
}

export default function ServiceChatScreen() {
  const router = useRouter();
  const top = useStableSafeTop();
  const keyboardInset = useWebKeyboardInset();
  const { user } = useAuth();
  const [url, setUrl] = useState('');
  const [title, setTitle] = useState('在线客服');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;
    void (async () => {
      try {
        const center = await fetchAppServiceCenter();
        const channel = pickOnlineChatChannel(center?.channels ?? []);
        const nextUrl = channel ? resolveChatChannelUrl(channel, user) : '';
        if (!alive) {
          return;
        }
        setTitle(channel?.name?.trim() || '在线客服');
        setUrl(nextUrl);
        if (!nextUrl) {
          modalError('暂未配置在线客服链接');
        }
      } catch (error) {
        if (!(error instanceof ApiError) || error.code !== 401) {
          modalError(error instanceof ApiError ? error.message : '获取客服信息失败');
        }
      } finally {
        if (alive) {
          setLoading(false);
        }
      }
    })();
    return () => {
      alive = false;
    };
  }, [user]);

  return (
    <View style={styles.root}>
      <View
        style={[
          styles.bar,
          { paddingTop: top },
          Platform.OS === 'web'
            ? ({
                backgroundImage: 'linear-gradient(90deg, #4EB4FF 0%, #3A78F0 100%)',
              } as Record<string, string>)
            : styles.barNative,
        ]}
      >
        <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
          <Text style={styles.back}>‹</Text>
        </Pressable>
        <Text style={styles.title}>{title}</Text>
        <View style={styles.side} />
      </View>
      <View style={[styles.frame, { paddingBottom: keyboardInset }]}>
        {loading ? (
          <View style={styles.loading}>
            <ActivityIndicator color="#FFFFFF" />
          </View>
        ) : url ? (
          <OnlineChatFrame url={url} />
        ) : (
          <Text style={styles.empty}>暂未配置在线客服链接</Text>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#3A78F0',
  },
  bar: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 44,
  },
  barNative: {
    backgroundColor: '#4596F5',
  },
  side: {
    width: 44,
  },
  backBtn: {
    width: 44,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  back: {
    color: '#FFFFFF',
    fontSize: 32,
    lineHeight: 34,
    fontWeight: '300',
  },
  title: {
    flex: 1,
    textAlign: 'center',
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
  },
  frame: {
    flex: 1,
    overflow: 'hidden',
    backgroundColor: '#F3F5F8',
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#3A78F0',
  },
  empty: {
    color: '#5A6A80',
    textAlign: 'center',
    marginTop: 48,
    fontSize: 14,
  },
});
