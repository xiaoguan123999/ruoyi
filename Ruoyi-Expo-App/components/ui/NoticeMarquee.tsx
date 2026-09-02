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
const SCROLL_SPEED = 40;
const GAP = 64;
const SEPARATOR = '　　';
const FONT_SIZE = 13;

type NoticeMarqueeProps = {
  texts: string[];
  style?: StyleProp<ViewStyle>;
  textStyle?: StyleProp<TextStyle>;
  emptyText?: string;
};

function estimateLineWidth(text: string) {
  return Math.ceil(text.length * FONT_SIZE * 0.92);
}

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
  const [measuredW, setMeasuredW] = useState(0);

  useEffect(() => {
    setMeasuredW(0);
  }, [line]);

  const contentW = Math.max(measuredW, estimateLineWidth(line));
  const shouldScroll = items.length > 0;

  const onViewportLayout = (e: LayoutChangeEvent) => {
    const next = Math.round(e.nativeEvent.layout.width);
    if (next > 0 && next !== viewportW) {
      setViewportW(next);
    }
  };

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

  const textNode = (key: string) => (
    <Text
      key={key}
      style={[styles.text, textStyle, { width: contentW }]}
      numberOfLines={1}
      ellipsizeMode="clip"
    >
      {line}
    </Text>
  );

  return (
    <View style={[styles.wrap, style]}>
      <View pointerEvents="none" collapsable={false} style={styles.measureHost}>
        <Text
          numberOfLines={1}
          ellipsizeMode="clip"
          style={[styles.text, textStyle, styles.measureText]}
          onTextLayout={(e) => {
            const next = Math.ceil(e.nativeEvent.lines.reduce((sum, item) => sum + item.width, 0));
            if (next > 0 && next !== measuredW) {
              setMeasuredW(next);
            }
          }}
        >
          {line}
        </Text>
      </View>
      <View style={styles.viewport} onLayout={onViewportLayout}>
        <Animated.View
          style={[
            styles.track,
            {
              width: shouldScroll ? contentW * 2 + GAP : contentW,
              transform: shouldScroll ? [{ translateX }] : undefined,
            },
          ]}
        >
          {textNode('a')}
          {shouldScroll ? (
            <Text
              key="b"
              style={[styles.text, textStyle, { width: contentW, marginLeft: GAP }]}
              numberOfLines={1}
              ellipsizeMode="clip"
            >
              {line}
            </Text>
          ) : null}
        </Animated.View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flex: 1,
    minWidth: 0,
    justifyContent: 'center',
  },
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
    opacity: 0.01,
  },
  measureText: {
    width: 4096,
  },
  track: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'nowrap',
  },
  text: {
    color: '#FFFFFF',
    fontSize: FONT_SIZE,
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
