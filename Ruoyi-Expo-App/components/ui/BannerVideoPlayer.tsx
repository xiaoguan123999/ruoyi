import { createElement, useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Linking,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { useStableSafeTop } from '@/hooks/useStableSafeTop';

import { resolvePlayUrl, revokePlayUri } from '@/utils/video-cache';
import { modalWarning } from '@/utils/toast';

type Props = {
  /** 远程视频地址 */
  uri: string;
  /** 缓存主键，建议用 carouselId */
  cacheId?: string;
  title?: string;
  visible: boolean;
  onClose: () => void;
};

type ViewMode = 'landscape' | 'portrait';

function WebVideo({ uri }: { uri: string }) {
  const ref = useRef<HTMLVideoElement | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) {
      return;
    }
    el.setAttribute('playsinline', 'true');
    el.setAttribute('webkit-playsinline', 'true');
    el.setAttribute('controlslist', 'nodownload nofullscreen noremoteplayback');
    el.disablePictureInPicture = true;

    const blockNativeFullscreen = (event: Event) => {
      event.preventDefault();
      event.stopPropagation();
    };
    el.addEventListener('fullscreenchange', blockNativeFullscreen);
    el.addEventListener('webkitfullscreenchange', blockNativeFullscreen);

    const play = async () => {
      try {
        await el.play();
      } catch {
        // 浏览器可能拦截自动播放，用户可点 controls 播放
      }
    };
    void play();

    return () => {
      el.removeEventListener('fullscreenchange', blockNativeFullscreen);
      el.removeEventListener('webkitfullscreenchange', blockNativeFullscreen);
      el.pause();
    };
  }, [uri]);

  return createElement('video', {
    ref,
    src: uri,
    controls: true,
    autoPlay: true,
    playsInline: true,
    controlsList: 'nodownload nofullscreen noremoteplayback',
    disablePictureInPicture: true,
    style: {
      width: '100%',
      height: '100%',
      backgroundColor: '#000',
      objectFit: 'contain',
    },
  } as Record<string, unknown>);
}

export function BannerVideoPlayer({ uri, cacheId, title, visible, onClose }: Props) {
  const insets = useSafeAreaInsets();
  const topInset = useStableSafeTop();
  const { width, height } = useWindowDimensions();
  const isDeviceLandscape = width > height;
  const [viewMode, setViewMode] = useState<ViewMode>('portrait');
  const [playUri, setPlayUri] = useState('');
  const [fromCache, setFromCache] = useState(false);
  const [resolving, setResolving] = useState(false);
  const revokeRef = useRef<string | undefined>(undefined);
  const cacheIdRef = useRef<string | undefined>(undefined);

  useEffect(() => {
    if (visible) {
      setViewMode('portrait');
    }
  }, [visible]);

  useEffect(() => {
    if (!visible || !uri) {
      return;
    }

    let cancelled = false;
    setResolving(true);
    setPlayUri('');
    setFromCache(false);

    void (async () => {
      const result = await resolvePlayUrl(cacheId || uri, uri);
      if (cancelled) {
        if (result.revokeUri) {
          revokePlayUri(result.revokeUri, cacheId || uri);
        }
        return;
      }
      revokeRef.current = result.revokeUri;
      cacheIdRef.current = cacheId || uri;
      setPlayUri(result.uri);
      setFromCache(result.fromCache);
      setResolving(false);

      if (Platform.OS !== 'web') {
        void Linking.openURL(result.uri)
          .catch(() => {
            modalWarning('无法打开视频，请稍后重试');
          })
          .finally(() => {
            onClose();
          });
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [visible, uri, cacheId, onClose]);

  const handleClose = () => {
    revokePlayUri(revokeRef.current, cacheIdRef.current);
    revokeRef.current = undefined;
    setPlayUri('');
    setFromCache(false);
    onClose();
  };

  const stageStyle = useMemo(() => {
    const needRotate = (viewMode === 'landscape') !== isDeviceLandscape;
    if (!needRotate) {
      return {
        width,
        height,
        transform: undefined as undefined,
      };
    }
    return {
      width: height,
      height: width,
      transform: [{ rotate: '90deg' as const }],
    };
  }, [viewMode, isDeviceLandscape, width, height]);

  if (Platform.OS !== 'web') {
    return null;
  }

  const topPad = Math.max(topInset, 12) + 4;
  const rightPad = Math.max(insets.right, 12) + 4;
  const leftPad = Math.max(insets.left, 12) + 8;

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
      <View style={styles.viewport}>
        <View style={[styles.stage, stageStyle]}>
          {visible && playUri && !resolving ? <WebVideo uri={playUri} /> : null}
          {resolving ? (
            <View style={styles.loading}>
              <ActivityIndicator color="#FFFFFF" />
              <Text style={styles.loadingText}>加载中…</Text>
            </View>
          ) : null}
        </View>

        <View style={[styles.toolbar, { top: topPad, right: rightPad }]}>
          {fromCache ? (
            <View style={styles.cacheTag}>
              <Text style={styles.cacheTagText}>已缓存</Text>
            </View>
          ) : null}
          <Pressable
            style={[styles.toolBtn, viewMode === 'portrait' && styles.toolBtnActive]}
            onPress={() => setViewMode('portrait')}
            hitSlop={8}
          >
            <Text style={[styles.toolText, viewMode === 'portrait' && styles.toolTextActive]}>
              竖屏
            </Text>
          </Pressable>
          <Pressable
            style={[styles.toolBtn, viewMode === 'landscape' && styles.toolBtnActive]}
            onPress={() => setViewMode('landscape')}
            hitSlop={8}
          >
            <Text style={[styles.toolText, viewMode === 'landscape' && styles.toolTextActive]}>
              横屏
            </Text>
          </Pressable>
          <Pressable style={styles.closeBtn} onPress={handleClose} hitSlop={12}>
            <Text style={styles.closeText}>关闭</Text>
          </Pressable>
        </View>

        {title ? (
          <Text
            style={[styles.title, { top: topPad + 4, left: leftPad, right: 220 }]}
            numberOfLines={1}
          >
            {title}
          </Text>
        ) : null}
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  viewport: {
    flex: 1,
    backgroundColor: '#000',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  stage: {
    backgroundColor: '#000',
    overflow: 'hidden',
  },
  loading: {
    ...StyleSheet.absoluteFillObject,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  loadingText: {
    color: 'rgba(255, 255, 255, 0.75)',
    fontSize: 13,
  },
  title: {
    position: 'absolute',
    color: 'rgba(255, 255, 255, 0.9)',
    fontSize: 14,
    fontWeight: '600',
  },
  toolbar: {
    position: 'absolute',
    zIndex: 2,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  cacheTag: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 16,
    backgroundColor: 'rgba(61, 220, 132, 0.28)',
    borderWidth: 1,
    borderColor: 'rgba(61, 220, 132, 0.55)',
  },
  cacheTagText: {
    color: '#B8F5D0',
    fontSize: 12,
    fontWeight: '700',
  },
  toolBtn: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.18)',
  },
  toolBtnActive: {
    backgroundColor: 'rgba(61, 139, 255, 0.35)',
    borderColor: 'rgba(120, 185, 255, 0.65)',
  },
  toolText: {
    color: 'rgba(255, 255, 255, 0.78)',
    fontSize: 13,
    fontWeight: '600',
  },
  toolTextActive: {
    color: '#FFFFFF',
  },
  closeBtn: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.18)',
  },
  closeText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '600',
  },
});
