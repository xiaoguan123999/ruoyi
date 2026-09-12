import { Image } from 'expo-image';
import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  useWindowDimensions,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { MAX_FONT_SIZE_MULTIPLIER } from '@/theme/typography';

/** 与 login-bg / 注册切图一致的设计画布 */
const BG_W = 402;
const BG_H = 874;
/** 设计稿中 Slogan 下沿（表单不得越过） */
const BRAND_SAFE_Y = 268;
/** Slogan 到首个表单项的额外间距（设计稿像素） */
const BRAND_FORM_GAP = 28;

type Props = {
  children: React.ReactNode;
  /** 顶部 Logo 区占视口高度比例（不得小于背景文案安全区） */
  formStart?: number;
  /** 表单行数（输入框+按钮等），用于按剩余高度均分行高/间距 */
  rows?: number;
  onRefresh?: () => void | Promise<void>;
};

type Viewport = { width: number; height: number };

type AuthMetrics = {
  rowHeight: number;
  gap: number;
  padX: number;
  fontSize: number;
  iconSize: number;
};

const AuthMetricsContext = createContext<AuthMetrics>({
  rowHeight: 46,
  gap: 12,
  padX: 20,
  fontSize: 14,
  iconSize: 18,
});

export function useAuthMetrics() {
  return useContext(AuthMetricsContext);
}

function clamp(n: number, min: number, max: number) {
  return Math.min(max, Math.max(min, n));
}

function useViewportSize(): Viewport {
  const windowSize = useWindowDimensions();
  const [webSize, setWebSize] = useState<Viewport>({
    width: windowSize.width,
    height: windowSize.height,
  });

  useEffect(() => {
    if (Platform.OS !== 'web' || typeof window === 'undefined') {
      return;
    }
    const read = () => {
      const vv = window.visualViewport;
      setWebSize({
        width: Math.round(vv?.width ?? window.innerWidth),
        height: Math.round(vv?.height ?? window.innerHeight),
      });
    };
    read();
    window.visualViewport?.addEventListener('resize', read);
    window.visualViewport?.addEventListener('scroll', read);
    window.addEventListener('resize', read);
    return () => {
      window.visualViewport?.removeEventListener('resize', read);
      window.visualViewport?.removeEventListener('scroll', read);
      window.removeEventListener('resize', read);
    };
  }, []);

  if (Platform.OS === 'web') {
    return webSize;
  }
  return { width: windowSize.width, height: windowSize.height };
}

/** cover + top 时，设计稿 Y 映射到屏幕像素 */
function designYToScreen(designY: number, availW: number, availH: number) {
  const coverScale = Math.max(availW / BG_W, availH / BG_H);
  return designY * coverScale;
}

export function AuthScreen({ children, formStart = 0.45, rows = 5, onRefresh }: Props) {
  const { width, height, fontScale } = useWindowDimensions();
  const { width: viewW, height: viewH } = useViewportSize();
  const insets = useSafeAreaInsets();
  const availW = Math.min(viewW || width, 480);
  const availH = Math.max(viewH || height, 1);
  const scale = Number.isFinite(fontScale) && fontScale > 0 ? fontScale : 1;

  const { metrics, topGap, bottomPad } = useMemo(() => {
    const padX = Math.round(clamp(availW * 0.05, 14, 24));
    const linkBlock = Math.round(clamp(availH * 0.045, 26, 36));
    const safeBottom = Math.round(insets.bottom);
    const bottomPad = Math.round(clamp(availH * 0.02, 10, 20)) + safeBottom;

    // 背景 cover+top 下，Logo/Slogan 实际占用高度 + 与表单的间距
    const brandSafePx = Math.round(
      designYToScreen(BRAND_SAFE_Y + BRAND_FORM_GAP, availW, availH),
    );
    const idealTop = Math.round(availH * formStart);
    let topGap = Math.max(brandSafePx, idealTop);

    // 行高跟着系统字号长，不够就滚动，不把字压回去
    const usedScale = clamp(scale, 1, MAX_FONT_SIZE_MULTIPLIER);
    const minRow = Math.round(14 * usedScale + 22);
    const minForm = rows * minRow + Math.max(0, rows - 1) * 8 + linkBlock;
    const leftover = availH - topGap - bottomPad;
    if (leftover < minForm) {
      const compressedBrand = Math.round(brandSafePx * (usedScale > 1.2 ? 0.7 : 0.82));
      topGap = Math.max(compressedBrand, availH - minForm - bottomPad);
    }

    const formArea = Math.max(availH - topGap - bottomPad - linkBlock, rows * minRow);
    const gapRatio = 14 / 46;
    const unit = formArea / (rows + (rows - 1) * gapRatio);
    const rowHeight = Math.round(clamp(unit, minRow, Math.max(minRow, 54 * usedScale)));
    const gap = Math.round(clamp(unit * gapRatio, 8, 16));

    const fontSize = 14;
    const iconSize = rowHeight >= 56 ? 20 : rowHeight >= 46 ? 18 : 16;

    return {
      metrics: { rowHeight, gap, padX, fontSize, iconSize },
      topGap,
      bottomPad,
    };
  }, [availW, availH, formStart, rows, insets.bottom, scale]);

  return (
    <AuthMetricsContext.Provider value={metrics}>
      <View style={[styles.root, Platform.OS === 'web' && styles.rootWeb]}>
        <Image
          source={images.loginBg}
          style={StyleSheet.absoluteFill}
          contentFit="cover"
          contentPosition="top"
        />
        <KeyboardAvoidingView
          style={styles.flex}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
          <RefreshableScrollView
            style={styles.flex}
            contentContainerStyle={{
              flexGrow: 1,
              paddingHorizontal: metrics.padX,
              paddingTop: topGap,
              paddingBottom: bottomPad + 20,
              gap: metrics.gap,
            }}
            automaticallyAdjustKeyboardInsets
            keyboardShouldPersistTaps="handled"
            showsVerticalScrollIndicator={false}
            onRefresh={onRefresh ?? (() => undefined)}
          >
            {children}
          </RefreshableScrollView>
        </KeyboardAvoidingView>
      </View>
    </AuthMetricsContext.Provider>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  rootWeb: {
    minHeight: '100dvh' as unknown as number,
    height: '100%' as unknown as number,
  },
  flex: {
    flex: 1,
    minHeight: 0,
  },
});
