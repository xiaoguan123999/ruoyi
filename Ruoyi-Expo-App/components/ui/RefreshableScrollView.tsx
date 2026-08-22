import { useCallback, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Platform,
  RefreshControl,
  ScrollView,
  StyleSheet,
  View,
  type NativeScrollEvent,
  type NativeSyntheticEvent,
  type ScrollViewProps,
  type StyleProp,
  type ViewStyle,
} from 'react-native';

import { colors } from '@/theme/colors';

const PULL_THRESHOLD = 68;
const PULL_MAX = 88;

type Props = Omit<ScrollViewProps, 'refreshControl'> & {
  /** 下拉刷新回调；支持同步/异步 */
  onRefresh: () => void | Promise<void>;
  /** 外部控制刷新态时传入；不传则组件内部管理 */
  refreshing?: boolean;
};

/**
 * 真机：系统 RefreshControl
 * H5：RN Web 无 RefreshControl，手势下拉实现，行为对齐真机
 */
export function RefreshableScrollView({
  onRefresh,
  refreshing: refreshingProp,
  children,
  onScroll,
  scrollEventThrottle = 16,
  contentContainerStyle,
  style,
  ...rest
}: Props) {
  const [internalRefreshing, setInternalRefreshing] = useState(false);
  const refreshing = refreshingProp ?? internalRefreshing;

  const atTopRef = useRef(true);
  const startYRef = useRef(0);
  const pullingRef = useRef(false);
  const [pull, setPull] = useState(0);

  const runRefresh = useCallback(async () => {
    if (refreshing) {
      return;
    }
    const manageInternal = refreshingProp === undefined;
    if (manageInternal) {
      setInternalRefreshing(true);
    }
    try {
      await onRefresh();
    } finally {
      if (manageInternal) {
        setInternalRefreshing(false);
      }
      setPull(0);
      pullingRef.current = false;
    }
  }, [onRefresh, refreshing, refreshingProp]);

  const handleScroll = useCallback(
    (e: NativeSyntheticEvent<NativeScrollEvent>) => {
      atTopRef.current = e.nativeEvent.contentOffset.y <= 1;
      onScroll?.(e);
    },
    [onScroll],
  );

  if (Platform.OS !== 'web') {
    return (
      <ScrollView
        {...rest}
        style={style}
        contentContainerStyle={contentContainerStyle}
        onScroll={handleScroll}
        scrollEventThrottle={scrollEventThrottle}
        alwaysBounceVertical
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => {
              void runRefresh();
            }}
            tintColor={colors.accent}
            colors={[colors.accent]}
            progressBackgroundColor="#0B1730"
          />
        }
      >
        {children}
      </ScrollView>
    );
  }

  const onTouchStart = (pageY: number) => {
    startYRef.current = pageY;
    pullingRef.current = atTopRef.current && !refreshing;
  };

  const onTouchMove = (pageY: number) => {
    if (!pullingRef.current || refreshing) {
      return;
    }
    if (!atTopRef.current) {
      setPull(0);
      return;
    }
    const delta = pageY - startYRef.current;
    if (delta <= 0) {
      setPull(0);
      return;
    }
    setPull(Math.min(delta * 0.42, PULL_MAX));
  };

  const onTouchEnd = () => {
    if (!pullingRef.current) {
      setPull(0);
      return;
    }
    if (pull >= PULL_THRESHOLD && !refreshing) {
      void runRefresh();
    } else {
      setPull(0);
    }
    pullingRef.current = false;
  };

  const indicatorH = refreshing ? 44 : pull;

  return (
    <View style={[styles.webWrap, style as StyleProp<ViewStyle>]}>
      <View style={[styles.webIndicator, { height: indicatorH }]}>
        {(refreshing || pull > 12) && <ActivityIndicator color={colors.accent} size="small" />}
      </View>
      <ScrollView
        {...rest}
        style={styles.webScroll}
        contentContainerStyle={[
          contentContainerStyle,
          pull > 0 || refreshing ? { paddingTop: 0 } : null,
        ]}
        onScroll={handleScroll}
        scrollEventThrottle={scrollEventThrottle}
        onTouchStart={(e) => {
          onTouchStart(e.nativeEvent.touches[0]?.pageY ?? 0);
          rest.onTouchStart?.(e);
        }}
        onTouchMove={(e) => {
          onTouchMove(e.nativeEvent.touches[0]?.pageY ?? 0);
          rest.onTouchMove?.(e);
        }}
        onTouchEnd={(e) => {
          onTouchEnd();
          rest.onTouchEnd?.(e);
        }}
        onTouchCancel={(e) => {
          onTouchEnd();
          rest.onTouchCancel?.(e);
        }}
        // 桌面 H5：鼠标拖拽下拉
        {...({
          onMouseDown: (e: { pageY: number }) => onTouchStart(e.pageY),
          onMouseMove: (e: { pageY: number; buttons: number }) => {
            if (e.buttons === 1) {
              onTouchMove(e.pageY);
            }
          },
          onMouseUp: () => onTouchEnd(),
          onMouseLeave: () => {
            if (!refreshing) {
              setPull(0);
              pullingRef.current = false;
            }
          },
        } as object)}
      >
        {children}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  webWrap: {
    flex: 1,
    minHeight: 0,
  },
  webScroll: {
    flex: 1,
  },
  webIndicator: {
    alignItems: 'center',
    justifyContent: 'flex-end',
    overflow: 'hidden',
    paddingBottom: 6,
  },
});
