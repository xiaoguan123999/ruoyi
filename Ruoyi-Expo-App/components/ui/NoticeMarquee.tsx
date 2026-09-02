import { createElement, useEffect, useMemo, useRef, useState } from 'react';
import {
  Animated,
  Easing,
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
const WEB_KEYFRAMES_ID = 'xfzl-notice-marquee-kf';

type NoticeMarqueeProps = {
  texts: string[];
  style?: StyleProp<ViewStyle>;
  textStyle?: StyleProp<TextStyle>;
  emptyText?: string;
};

function estimateLineWidth(text: string) {
  return Math.ceil(text.length * FONT_SIZE * 0.92);
}

function ensureWebKeyframes() {
  if (Platform.OS !== 'web' || typeof document === 'undefined') {
    return;
  }
  if (document.getElementById(WEB_KEYFRAMES_ID)) {
    return;
  }
  const style = document.createElement('style');
  style.id = WEB_KEYFRAMES_ID;
  style.textContent =
    '@keyframes xfzlNoticeMarquee{from{transform:translate3d(0,0,0)}to{transform:translate3d(-50%,0,0)}}';
  document.head.appendChild(style);
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
  const [unitW, setUnitW] = useState(0);

  useEffect(() => {
    setUnitW(0);
  }, [line]);

  useEffect(() => {
    ensureWebKeyframes();
  }, []);

  const contentW = Math.max(unitW, estimateLineWidth(line));
  const shouldScroll = items.length > 0;
  const duration = Math.max(2800, Math.round(((contentW + GAP) / SCROLL_SPEED) * 1000));

  useEffect(() => {
    if (Platform.OS === 'web' || !shouldScroll || contentW <= 0) {
      return;
    }
    translateX.stopAnimation();
    translateX.setValue(0);
    const cycle = contentW + GAP;
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
  }, [contentW, duration, shouldScroll, translateX]);

  if (items.length === 0) {
    return (
      <View style={[styles.viewport, style]}>
        <Text style={[styles.text, textStyle]} numberOfLines={1} ellipsizeMode="clip">
          {emptyText}
        </Text>
      </View>
    );
  }

  const textNode = (key: string) => (
    <Text
      key={key}
      style={[styles.text, textStyle, styles.unitText]}
      numberOfLines={1}
      ellipsizeMode="clip"
    >
      {line}
    </Text>
  );

  if (Platform.OS === 'web') {
    const flatText = StyleSheet.flatten([styles.text, textStyle]);
    const webTrack = createElement(
      'div',
      {
        style: {
          display: 'flex',
          flexDirection: 'row',
          flexWrap: 'nowrap',
          width: 'max-content',
          willChange: 'transform',
          animation: shouldScroll ? `xfzlNoticeMarquee ${duration}ms linear infinite` : 'none',
        },
      },
      createElement('span', { style: { paddingRight: GAP, whiteSpace: 'nowrap' } }, line),
      createElement('span', { style: { paddingRight: GAP, whiteSpace: 'nowrap' } }, line),
    );

    return (
      <View style={[styles.wrap, style]}>
        <View style={styles.viewport}>
          {createElement(
            'div',
            {
              style: {
                overflow: 'hidden',
                width: '100%',
                color: flatText.color,
                fontSize: flatText.fontSize,
                fontWeight: flatText.fontWeight,
                lineHeight: `${flatText.lineHeight ?? 18}px`,
                whiteSpace: 'nowrap',
              },
            },
            webTrack,
          )}
        </View>
      </View>
    );
  }

  return (
    <View style={[styles.wrap, style]}>
      <View pointerEvents="none" collapsable={false} style={styles.measureHost}>
        <Text
          numberOfLines={1}
          ellipsizeMode="clip"
          style={[styles.text, textStyle, styles.measureText]}
          onTextLayout={(e) => {
            const next = Math.ceil(e.nativeEvent.lines.reduce((sum, item) => sum + item.width, 0));
            if (next > 0 && next !== unitW) {
              setUnitW(next);
            }
          }}
        >
          {line}
        </Text>
      </View>
      <View style={styles.viewport}>
        <Animated.View
          style={[
            styles.track,
            {
              transform: [{ translateX }],
            },
          ]}
        >
          <View style={[styles.unit, contentW > 0 ? { width: contentW + GAP } : null]}>
            {textNode('a')}
          </View>
          <View style={[styles.unit, contentW > 0 ? { width: contentW + GAP } : null]}>
            {textNode('b')}
          </View>
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
    overflow: 'hidden',
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
    width: 16384,
  },
  track: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'nowrap',
  },
  unit: {
    flexDirection: 'row',
    alignItems: 'center',
    flexShrink: 0,
    paddingRight: GAP,
  },
  unitText: {
    flexShrink: 0,
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
