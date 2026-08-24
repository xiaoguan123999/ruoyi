import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Animated,
  Easing,
  LayoutChangeEvent,
  Platform,
  StyleSheet,
  Text,
  View,
  type StyleProp,
  type TextStyle,
  type ViewStyle,
} from 'react-native';

const USE_NATIVE_DRIVER = Platform.OS !== 'web';
/** 恒定横向速度（px/s），Web / 真机体感一致 */
const SCROLL_SPEED = 40;
const GAP = 64;
const SEPARATOR = '　　';
/** 测量容器足够宽，避免真机把文案压窄后出现省略号 */
const MEASURE_WIDTH = 8192;

type NoticeMarqueeProps = {
  texts: string[];
  style?: StyleProp<ViewStyle>;
  textStyle?: StyleProp<TextStyle>;
  emptyText?: string;
};

export function NoticeMarquee({
  texts,
  style,
  textStyle,
  emptyText = '暂无公告',
}: NoticeMarqueeProps) {
  const items = useMemo(
    () => texts.map((t) => t.trim()).filter(Boolean),
    [texts],
  );
  const line = useMemo(
    () => items.map((title, index) => `${index + 1}. ${title}`).join(SEPARATOR),
    [items],
  );

  const translateX = useRef(new Animated.Value(0)).current;
  const [viewportW, setViewportW] = useState(0);
  const [contentW, setContentW] = useState(0);

  useEffect(() => {
    setContentW(0);
  }, [line]);

  const onViewportLayout = (e: LayoutChangeEvent) => {
    const next = Math.round(e.nativeEvent.layout.width);
    if (next > 0 && next !== viewportW) {
      setViewportW(next);
    }
  };

  const onContentLayout = (e: LayoutChangeEvent) => {
    const next = Math.round(e.nativeEvent.layout.width);
    if (next > 0 && next !== contentW) {
      setContentW(next);
    }
  };

  const shouldScroll =
    items.length > 1 || (contentW > 0 && viewportW > 0 && contentW > viewportW + 2);

  useEffect(() => {
    translateX.stopAnimation();
    translateX.setValue(0);

    if (!shouldScroll || contentW <= 0) {
      return;
    }

    const cycle = contentW + GAP;
    const duration = Math.max(2800, Math.round((cycle / SCROLL_SPEED) * 1000));
    const loop = Animated.loop(
      Animated.timing(translateX, {
        toValue: -cycle,
        duration,
        easing: Easing.linear,
        useNativeDriver: USE_NATIVE_DRIVER,
      }),
    );
    loop.start();
    return () => {
      loop.stop();
    };
  }, [contentW, shouldScroll, translateX]);

  if (items.length === 0) {
    return (
      <View style={[styles.viewport, style]} onLayout={onViewportLayout}>
        <Text style={[styles.text, textStyle]} numberOfLines={1} ellipsizeMode="clip">
          {emptyText}
        </Text>
      </View>
    );
  }

  const textNode = (extraStyle?: StyleProp<TextStyle>) => (
    <Text
      style={[styles.text, textStyle, extraStyle]}
      numberOfLines={1}
      // clip：真机单行截断但不显示 "..."；宽度设为完整内容宽后即可完整展示
      ellipsizeMode="clip"
    >
      {line}
    </Text>
  );

  return (
    <View style={[styles.viewport, style]} onLayout={onViewportLayout}>
      {/* 真机：collapsable=false + 非 0 opacity，避免测量节点被优化掉 */}
      <View
        collapsable={false}
        pointerEvents="none"
        style={styles.measureHost}
      >
        <Text
          style={[styles.text, textStyle, styles.measureText]}
          numberOfLines={1}
          ellipsizeMode="clip"
          onLayout={onContentLayout}
        >
          {line}
        </Text>
      </View>

      {contentW > 0 ? (
        <Animated.View
          style={[
            styles.track,
            {
              width: shouldScroll ? contentW * 2 + GAP : contentW,
              transform: shouldScroll ? [{ translateX }] : undefined,
            },
          ]}
        >
          {textNode({ width: contentW })}
          {shouldScroll ? textNode({ width: contentW, marginLeft: GAP }) : null}
        </Animated.View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  viewport: {
    flex: 1,
    overflow: 'hidden',
    justifyContent: 'center',
    minWidth: 0,
  },
  measureHost: {
    position: 'absolute',
    left: 0,
    top: 0,
    width: MEASURE_WIDTH,
    // 不能用 0：Android 可能跳过布局；Web 仍不可见
    opacity: 0.01,
  },
  measureText: {
    alignSelf: 'flex-start',
  },
  track: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'nowrap',
  },
  text: {
    color: '#FFFFFF',
    fontSize: 13,
    lineHeight: 18,
    flexShrink: 0,
    ...(Platform.OS === 'web'
      ? ({
          whiteSpace: 'nowrap',
          wordBreak: 'keep-all',
          overflow: 'visible',
          textOverflow: 'clip',
        } as TextStyle)
      : null),
  },
});
