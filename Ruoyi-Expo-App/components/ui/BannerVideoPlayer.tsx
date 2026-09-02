import { createElement, useEffect, useMemo, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Modal,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  useWindowDimensions,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { WebView } from 'react-native-webview';
import * as ScreenOrientation from 'expo-screen-orientation';

import { useStableSafeTop } from '@/hooks/useStableSafeTop';

import { resolvePlayUrl, revokePlayUri } from '@/utils/video-cache';

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

function escapeHtmlAttr(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/</g, '&lt;');
}

function buildPlayerHtml(src: string) {
  return `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"/>
<style>
  html,body{margin:0;width:100%;height:100%;background:#000;overflow:hidden}
  video{width:100%;height:100%;object-fit:contain;background:#000}
</style>
</head>
<body>
<video id="v" controls autoplay muted playsinline webkit-playsinline preload="auto">
  <source src="${escapeHtmlAttr(src)}" type="video/mp4"/>
</video>
<script>
  var v = document.getElementById('v');
  v.muted = true;
  var play = function () { v.play().catch(function () {}); };
  v.addEventListener('canplay', play);
  play();
</script>
</body>
</html>`;
}

function NativeVideo({ uri, width, height }: { uri: string; width: number; height: number }) {
  const [source, setSource] = useState<{ html?: string; uri?: string } | null>(null);

  useEffect(() => {
    let cancelled = false;
    setSource(null);

    const run = async () => {
      if (uri.startsWith('http://') || uri.startsWith('https://')) {
        if (!cancelled) {
          setSource({ html: buildPlayerHtml(uri) });
        }
        return;
      }

      if (!uri.startsWith('file:')) {
        return;
      }

      const FileSystem = await import('expo-file-system/legacy');
      const fileName = uri.split('/').pop() || 'video.mp4';
      const dir = uri.slice(0, uri.length - fileName.length);
      const htmlPath = `${dir}player.html`;
      await FileSystem.writeAsStringAsync(htmlPath, buildPlayerHtml(fileName));
      if (!cancelled) {
        setSource({ uri: htmlPath });
      }
    };

    void run();
    return () => {
      cancelled = true;
    };
  }, [uri]);

  if (!source) {
    return (
      <View style={[styles.loading, { width, height }]}>
        <ActivityIndicator color="#FFFFFF" />
      </View>
    );
  }

  return (
    <WebView
      source={source.html ? { html: source.html, baseUrl: 'https://localhost/' } : { uri: source.uri! }}
      style={{ width, height, backgroundColor: '#000' }}
      allowsInlineMediaPlayback
      mediaPlaybackRequiresUserAction={false}
      javaScriptEnabled
      mixedContentMode="always"
      originWhitelist={['*', 'file://*']}
      allowFileAccess
      allowFileAccessFromFileURLs
      allowingReadAccessToURL={uri.startsWith('file:') ? uri.replace(/[^/]+$/, '') : undefined}
      allowsFullscreenVideo
      androidLayerType="hardware"
      setSupportMultipleWindows={false}
    />
  );
}

export function BannerVideoPlayer({ uri, cacheId, title, visible, onClose }: Props) {
  const insets = useSafeAreaInsets();
  const topInset = useStableSafeTop();
  const { width, height } = useWindowDimensions();
  const isDeviceLandscape = width > height;
  const [viewMode, setViewMode] = useState<ViewMode>('portrait');
  const [playUri, setPlayUri] = useState('');
  const revokeRef = useRef<string | undefined>(undefined);
  const cacheIdRef = useRef<string | undefined>(undefined);

  useEffect(() => {
    if (visible) {
      setViewMode('portrait');
    }
  }, [visible]);

  useEffect(() => {
    if (Platform.OS === 'web') {
      return;
    }
    if (!visible) {
      void ScreenOrientation.lockAsync(ScreenOrientation.OrientationLock.PORTRAIT_UP);
      return;
    }
    void ScreenOrientation.lockAsync(
      viewMode === 'landscape'
        ? ScreenOrientation.OrientationLock.LANDSCAPE
        : ScreenOrientation.OrientationLock.PORTRAIT_UP,
    );
  }, [visible, viewMode]);

  useEffect(() => {
    if (!visible || !uri) {
      return;
    }

    let cancelled = false;
    setPlayUri('');

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
    })();

    return () => {
      cancelled = true;
    };
  }, [visible, uri, cacheId, onClose]);

  const handleClose = () => {
    if (Platform.OS !== 'web') {
      void ScreenOrientation.lockAsync(ScreenOrientation.OrientationLock.PORTRAIT_UP);
    }
    revokePlayUri(revokeRef.current, cacheIdRef.current);
    revokeRef.current = undefined;
    setPlayUri('');
    onClose();
  };

  const stageStyle = useMemo(() => {
    if (Platform.OS !== 'web') {
      return {
        width,
        height,
        transform: undefined as undefined,
      };
    }
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

  const topPad = Math.max(topInset, 12) + 4;
  const rightPad = Math.max(insets.right, 12) + 4;
  const leftPad = Math.max(insets.left, 12) + 8;

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={handleClose}>
      <View style={styles.viewport}>
        <View style={[styles.stage, stageStyle]}>
          {visible && playUri ? (
            Platform.OS === 'web' ? (
              <WebVideo uri={playUri} />
            ) : (
              <NativeVideo uri={playUri} width={stageStyle.width} height={stageStyle.height} />
            )
          ) : (
            <View style={styles.loading}>
              <ActivityIndicator color="#FFFFFF" />
              <Text style={styles.loadingText}>加载中…</Text>
            </View>
          )}
        </View>

        <View style={[styles.toolbar, { top: topPad, right: rightPad }]}>
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
  nativeVideo: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#000',
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
    zIndex: 10,
    elevation: 10,
    color: 'rgba(255, 255, 255, 0.9)',
    fontSize: 14,
    fontWeight: '600',
  },
  toolbar: {
    position: 'absolute',
    zIndex: 10,
    elevation: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
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
