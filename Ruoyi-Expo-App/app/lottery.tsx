import { useCallback, useMemo, useRef, useState } from 'react';
import { useFocusEffect, useRouter } from 'expo-router';
import { Image } from 'expo-image';
import {
  Animated,
  Easing,
  Pressable,
  StyleSheet,
  useWindowDimensions,
  View,
} from 'react-native';
import Svg, { Circle, Defs, Line, LinearGradient, Path, Stop } from 'react-native-svg';

import {
  drawLottery,
  fetchLotteryCurrent,
  type AppLotteryCurrent,
  type AppLotteryPrize,
} from '@/api/app-lottery';
import { ApiError } from '@/api/request';
import { Text } from '@/components/ui/AppText';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalInfo, modalSuccess } from '@/utils/toast';

const TITLE = '#111827';
const DESC = '#374151';
/** 距目标角度多少度内切换为最终屏幕布局（只触发一次） */
const LAYOUT_NEAR_END_DEG = 100;

const DEFAULT_SECTOR_COLORS = ['#FFF8EC', '#F5F1FC', '#F6F4FA', '#F0F6FC', '#F8F3E8', '#EEF3FA'];

const DEFAULT_RULE_LINES = [
  '1、签到3天并推广3人实名注册即可获得一次抽奖机会；',
  '2、抽取一次后，每隔72小时后抽取下一次；',
  '3、一等奖、二等奖、三等奖抽到请联系客服发放，参与奖则会自动发放至账户助力值余额；',
  '规则如有调整将会提前通知，最终解释权归星帆智联所有',
];

function buildRuleLines(ruleText?: string): string[] {
  const fromApi = ruleText?.trim();
  if (!fromApi) {
    return DEFAULT_RULE_LINES;
  }
  const lines = fromApi
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean);
  return lines.length > 0 ? lines : DEFAULT_RULE_LINES;
}

function pieSlice(cx: number, cy: number, radius: number, startDeg: number, endDeg: number) {
  const rad = (deg: number) => ((deg - 90) * Math.PI) / 180;
  const p = (deg: number) => ({
    x: cx + radius * Math.cos(rad(deg)),
    y: cy + radius * Math.sin(rad(deg)),
  });
  const a = p(startDeg);
  const b = p(endDeg);
  const large = endDeg - startDeg > 180 ? 1 : 0;
  return `M ${cx} ${cy} L ${a.x} ${a.y} A ${radius} ${radius} 0 ${large} 1 ${b.x} ${b.y} Z`;
}

function descLines(desc: string, maxLines: number): string[] {
  const text = desc.trim();
  if (!text || maxLines <= 0) {
    return [];
  }
  return text
    .split(/\n+/)
    .map((s) => s.trim())
    .filter(Boolean)
    .slice(0, maxLines);
}

/**
 * 布局参数：labelR 必须是「半径 r」的比例，不能用直径 size，
 * 否则标签会飞到转盘外。
 */
function sectorChrome(count: number, radius: number) {
  const n = Math.max(count, 1);
  const slice = 360 / n;
  const scale = radius / 118;
  const labelR =
    n <= 3 ? radius * 0.62 : n <= 5 ? radius * 0.58 : n <= 7 ? radius * 0.54 : radius * 0.5;
  const chord = 2 * labelR * Math.sin((slice * Math.PI) / 360);
  const boxW = Math.min(chord * 0.82, radius * (n <= 4 ? 0.72 : n <= 6 ? 0.58 : 0.48));
  const boxH = Math.min(radius * (n <= 4 ? 0.55 : n <= 6 ? 0.48 : 0.4), chord * 1.05);
  const px = (value: number) => Math.max(8, Math.round(value * scale));

  if (n <= 3) {
    return { slice, labelR, boxW, boxH, nameSize: px(12), descSize: px(9), img: px(52), maxDesc: 3, hubRatio: 0.34 };
  }
  if (n === 4) {
    return { slice, labelR, boxW, boxH, nameSize: px(11), descSize: px(9), img: px(46), maxDesc: 3, hubRatio: 0.32 };
  }
  if (n <= 6) {
    return { slice, labelR, boxW, boxH, nameSize: px(10), descSize: px(8), img: px(36), maxDesc: 2, hubRatio: 0.3 };
  }
  return { slice, labelR, boxW, boxH, nameSize: px(9), descSize: px(8), img: px(28), maxDesc: 2, hubRatio: 0.28 };
}

/**
 * 扇区中心角（0=顶，顺时针）：上下位用左右图文，左右位用上下图文。
 */
function isSideSector(midDeg: number): boolean {
  const a = ((midDeg % 360) + 360) % 360;
  const toTopBottom = Math.min(a, Math.abs(a - 180), 360 - a);
  const toLeftRight = Math.min(Math.abs(a - 90), Math.abs(a - 270));
  return toLeftRight < toTopBottom;
}

function sectorFrame(radius: number, sliceDeg: number, side: boolean, screenMid: number) {
  const turned = ((screenMid % 360) + 360) % 360;
  const nearTop = Math.min(turned, 360 - turned) < 32;
  const unit = radius / 112;
  const hub = (nearTop ? 54 : 40) * unit;
  const outer = Math.max(hub + 16 * unit, radius - 4 * unit);
  const halfAngle = ((sliceDeg / 2) * 0.88 * Math.PI) / 180;
  const fallback = side
    ? { width: 40 * unit, height: 48 * unit, labelR: (hub + outer) / 2 }
    : { width: 56 * unit, height: 28 * unit, labelR: (hub + outer) / 2 };
  let best = { ...fallback, score: 0 };
  if (!side) {
    const room = outer - hub - 2 * unit;
    for (let height = Math.min(radius * 0.38, room); height >= 18 * unit; height -= 2) {
      const inner = hub + 2 * unit;
      const labelR = inner + height / 2;
      let width = Math.min(2 * inner * Math.tan(halfAngle), radius * 0.9);
      while (width > 20 * unit && Math.hypot(labelR + height / 2, width / 2) > outer) width -= 2;
      const score = height >= 32 * unit ? width * 3 + height : width + height;
      if (score > best.score) best = { width, height, labelR, score };
    }
  } else {
    const room = outer - hub - 2 * unit;
    for (let width = Math.min(radius * 0.58, room); width >= 28 * unit; width -= 2) {
      const inner = hub + 2 * unit;
      const labelR = inner + width / 2;
      let height = Math.min(2 * inner * Math.tan(halfAngle), radius * 0.75);
      while (height > 20 * unit && Math.hypot(labelR + width / 2, height / 2) > outer) height -= 2;
      const score = height >= 52 * unit ? width * 4 + height : height * 2 + width;
      if (score > best.score) best = { width, height, labelR, score };
    }
  }
  return { width: best.width, height: best.height, labelR: best.labelR };
}

function linePx(value: string, fontSize: number) {
  return Array.from(value.trim()).length * fontSize * 1.08;
}

function fitSectorContent(options: {
  side: boolean;
  boxW: number;
  boxH: number;
  name: string;
  lines: string[];
  nameSize: number;
  descSize: number;
  img: number;
  hasImage: boolean;
}) {
  let nameSize = options.nameSize;
  let descSize = options.descSize;
  let img = options.hasImage ? options.img : 0;
  const gap = 4;
  const nameMin = linePx(options.name, 8);
  const maxImg = Math.max(0, options.boxW - gap - nameMin);
  const imageFloor = options.hasImage
    ? Math.min(options.img, options.boxH - 2, maxImg, Math.max(18, Math.round(options.boxW * 0.42)))
    : 0;

  const pack = () => {
    const nameW = Math.max(linePx(options.name, nameSize), nameSize + 2);
    if (options.side) {
      const slot = Math.max(nameSize + 2, options.boxW - 2);
      const nameLines = Math.max(1, Math.ceil(nameW / slot));
      let descH = 0;
      for (const line of options.lines) {
        descH += Math.max(1, Math.ceil(linePx(line, descSize) / slot)) * (descSize + 2);
      }
      const textH = nameLines * (nameSize + 2) + descH;
      const imgFit = Math.max(0, Math.min(img, slot, options.boxH - textH - gap));
      const usedH = textH + (imgFit > 0 ? gap + imgFit : 0);
      const descOneLine = options.lines.every((line) => linePx(line, descSize) <= slot + 0.5);
      return {
        textW: slot,
        img: imgFit,
        descOneLine,
        ok: nameLines === 1 && usedH <= options.boxH + 0.5,
      };
    }
    const imgFit = Math.max(0, Math.min(img, options.boxH - 2, options.boxW - gap - (nameSize + 2)));
    const slot = Math.max(nameSize + 2, options.boxW - gap - imgFit);
    const nameLines = nameW <= slot + 0.5 ? 1 : Math.ceil(nameW / Math.max(slot, 1));
    const descOneLine = options.lines.every((line) => linePx(line, descSize) <= slot + 0.5);
    let descH = 0;
    for (const line of options.lines) {
      descH += Math.max(1, Math.ceil(linePx(line, descSize) / Math.max(slot, 1))) * (descSize + 2);
    }
    const textH = nameLines * (nameSize + 2) + descH;
    return {
      textW: slot,
      img: imgFit,
      descOneLine,
      ok:
        nameLines === 1 &&
        textH <= options.boxH + 0.5 &&
        slot + (imgFit > 0 ? gap + imgFit : 0) <= options.boxW + 0.5,
    };
  };

  for (let step = 0; step < 80; step += 1) {
    const measured = pack();
    if (options.side) {
      if (measured.ok && (measured.descOneLine || descSize <= 7)) {
        return { nameSize, descSize, img: measured.img, textWidth: measured.textW };
      }
      if (measured.ok && !measured.descOneLine) {
        descSize = Math.max(7, descSize - 0.5);
        continue;
      }
      if (img > 0) {
        img = Math.max(0, img - 2);
        continue;
      }
      if (nameSize > 8) {
        nameSize -= 0.5;
        descSize = Math.max(7, descSize - 0.5);
        continue;
      }
      descSize = Math.max(7, descSize - 0.5);
      continue;
    }
    if (measured.ok && measured.descOneLine && measured.img + 0.5 >= imageFloor) {
      return { nameSize, descSize, img: measured.img, textWidth: measured.textW };
    }
    if (img > imageFloor) {
      img = Math.max(imageFloor, img - 2);
      continue;
    }
    if (measured.ok) {
      return { nameSize, descSize, img: measured.img, textWidth: measured.textW };
    }
    if (descSize > 7) {
      descSize = Math.max(7, descSize - 0.5);
      continue;
    }
    if (nameSize > 8) {
      nameSize = Math.max(8, nameSize - 0.5);
      continue;
    }
    return { nameSize, descSize, img: measured.img, textWidth: measured.textW };
  }
  const measured = pack();
  return { nameSize, descSize, img: measured.img, textWidth: measured.textW };
}

/** 第 i 扇区中心角 = i * slice，首项正对顶部指针 */

function isBeforeStart(startTime?: string) {
  if (!startTime) {
    return false;
  }
  const matched = startTime.trim().match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?/);
  if (!matched) {
    const time = Date.parse(startTime);
    return Number.isFinite(time) && time > Date.now();
  }
  const time = new Date(
    Number(matched[1]),
    Number(matched[2]) - 1,
    Number(matched[3]),
    Number(matched[4]),
    Number(matched[5]),
    Number(matched[6] || 0),
  ).getTime();
  return time > Date.now();
}

function targetRotationForIndex(index: number, count: number, currentAngle: number): number {
  const slice = 360 / Math.max(count, 1);
  const center = index * slice;
  const needed = ((360 - center) % 360 + 360) % 360;
  const base = Math.ceil(currentAngle / 360) * 360 + 360 * 5;
  return base + needed;
}

function LotteryDisk({
  size,
  prizes,
  layoutRotationDeg = 0,
  rotateAnim,
  labelOpacity,
}: {
  size: number;
  prizes: AppLotteryPrize[];
  /** 屏幕布局用角度（减速末段一次切到终值，避免连续 setState 卡顿） */
  layoutRotationDeg?: number;
  /** 与转盘同值，用于标签反向旋转保持正向 */
  rotateAnim: Animated.Value;
  labelOpacity: Animated.Value;
}) {
  const r = size / 2;
  const rim = Math.max(5, Math.round(size * 0.026));
  const rOut = r - rim;
  const count = prizes.length;

  const counterRotate = rotateAnim.interpolate({
    inputRange: [-72000, 72000],
    outputRange: ['72000deg', '-72000deg'],
  });

  if (count === 0) {
    return (
      <View style={{ width: size, height: size, borderRadius: r, overflow: 'hidden' }}>
        <Svg width={size} height={size}>
          <Circle cx={r} cy={r} r={r - 0.6} fill="#3B7EE8" />
          <Circle cx={r} cy={r} r={rOut} fill="#FFF8EC" />
        </Svg>
      </View>
    );
  }

  const chrome = sectorChrome(count, r);
  const offset = -chrome.slice / 2;
  const rHub = r * chrome.hubRatio;

  return (
    <View style={{ width: size, height: size, borderRadius: r, overflow: 'hidden' }}>
      <Svg width={size} height={size}>
        <Circle cx={r} cy={r} r={r - 0.6} fill="#3B7EE8" />
        {prizes.map((prize, index) => {
          const start = offset + index * chrome.slice;
          const end = offset + (index + 1) * chrome.slice;
          const fill = prize.sectorBgColor || DEFAULT_SECTOR_COLORS[index % DEFAULT_SECTOR_COLORS.length];
          return (
            <Path key={`slice-${prize.prizeId || index}`} d={pieSlice(r, r, rOut, start, end)} fill={fill} />
          );
        })}
        {prizes.map((_, index) => {
          const deg = offset + index * chrome.slice;
          const rad = ((deg - 90) * Math.PI) / 180;
          return (
            <Line
              key={`sep-${index}`}
              x1={r + rHub * Math.cos(rad)}
              y1={r + rHub * Math.sin(rad)}
              x2={r + rOut * Math.cos(rad)}
              y2={r + rOut * Math.sin(rad)}
              stroke="#E4E7ED"
              strokeWidth={1}
            />
          );
        })}
        <Circle cx={r} cy={r} r={rOut} fill="none" stroke="#8BB8F2" strokeWidth={1.2} />
      </Svg>

      {prizes.map((prize, index) => {
        const mid = index * chrome.slice;
        const screenMid = mid + layoutRotationDeg;
        const side = isSideSector(screenMid);
        const box = sectorFrame(rOut, chrome.slice, side, screenMid);
        const place = ((mid - 90) * Math.PI) / 180;
        const cx = r + box.labelR * Math.cos(place);
        const cy = r + box.labelR * Math.sin(place);
        const lines = descLines(prize.prizeDesc, chrome.maxDesc);
        const fitted = fitSectorContent({
          side,
          boxW: box.width,
          boxH: box.height,
          name: prize.prizeName,
          lines,
          nameSize: chrome.nameSize,
          descSize: chrome.descSize,
          img: chrome.img,
          hasImage: Boolean(prize.imageUrl),
        });
        const img = fitted.img;
        const textWidth = fitted.textWidth;
        const imgStyle = { width: img, height: img, flexShrink: 0 };

        const textBlock = (
          <View
            collapsable={false}
            style={[side ? styles.textStack : styles.textStackRow, { width: textWidth, maxWidth: textWidth, flexShrink: 0 }]}
          >
            <Text
              style={[
                styles.prizeName,
                {
                  width: textWidth,
                  fontSize: fitted.nameSize,
                  lineHeight: fitted.nameSize + 3,
                  textAlign: 'center',
                },
                prize.nameColor ? { color: prize.nameColor } : null,
              ]}
            >
              {prize.prizeName}
            </Text>
            {lines.map((line, lineIdx) => (
              <Text
                key={`${prize.prizeId || index}-d-${lineIdx}`}
                style={[
                  styles.prizeDesc,
                  {
                    width: textWidth,
                    fontSize: fitted.descSize,
                    lineHeight: fitted.descSize + 3,
                    textAlign: 'center',
                  },
                  prize.descColor ? { color: prize.descColor } : null,
                ]}
              >
                {line}
              </Text>
            ))}
          </View>
        );

        return (
          <Animated.View
            key={`label-${prize.prizeId || index}`}
            style={[
              styles.cell,
              {
                left: cx - box.width / 2,
                top: cy - box.height / 2,
                width: box.width,
                height: box.height,
                opacity: labelOpacity,
                transform: [{ rotate: counterRotate }],
              },
            ]}
            pointerEvents="none"
          >
            <View collapsable={false} style={[styles.cellInner, side ? styles.cellCol : styles.cellRow, { width: box.width, height: box.height }]}>
              {textBlock}
              {prize.imageUrl && img > 0 ? (
                <Image source={{ uri: prize.imageUrl }} style={imgStyle} contentFit="contain" />
              ) : null}
            </View>
          </Animated.View>
        );
      })}
    </View>
  );
}

export default function LotteryScreen() {
  const router = useRouter();
  const { width } = useWindowDimensions();
  const stageWidth = width - 32;
  const wheelSize = Math.min(236, Math.round(stageWidth * 0.58));
  const hubScale = wheelSize / 236;
  const hubWrapSize = Math.round(78 * hubScale);
  const hubButton = Math.round(74 * hubScale);
  const hubSvgH = Math.round(94 * hubScale);
  const hubFont = Math.max(11, Math.round(15 * hubScale));
  const hubLine = Math.max(14, Math.round(20 * hubScale));
  const rotate = useRef(new Animated.Value(0)).current;
  const labelOpacity = useRef(new Animated.Value(1)).current;
  const angleRef = useRef(0);
  const layoutListenerRef = useRef<string | null>(null);
  const layoutSwitchedRef = useRef(false);
  const [spinning, setSpinning] = useState(false);
  /** 屏幕布局角度：减速末段只切换一次到终值 */
  const [layoutRotationDeg, setLayoutRotationDeg] = useState(0);
  const [current, setCurrent] = useState<AppLotteryCurrent | null>(null);
  const [loaded, setLoaded] = useState(false);

  const ruleLines = useMemo(() => buildRuleLines(current?.ruleText), [current?.ruleText]);
  const prizes = current?.prizes ?? [];
  const chanceText = `${current?.chanceBalance ?? 0}次`;
  const canPress = Boolean(current?.canDraw) && !spinning && prizes.length > 0;

  const clearLayoutListener = useCallback(() => {
    if (layoutListenerRef.current != null) {
      rotate.removeListener(layoutListenerRef.current);
      layoutListenerRef.current = null;
    }
  }, [rotate]);

  /** 减速末段：淡出 → 一次切到最终布局 → 淡入，避免连续重排卡顿 */
  const watchNearEndLayout = useCallback(
    (targetDeg: number) => {
      clearLayoutListener();
      layoutSwitchedRef.current = false;
      layoutListenerRef.current = rotate.addListener(({ value }) => {
        if (layoutSwitchedRef.current || value < targetDeg - LAYOUT_NEAR_END_DEG) {
          return;
        }
        layoutSwitchedRef.current = true;
        clearLayoutListener();
        Animated.timing(labelOpacity, {
          toValue: 0,
          duration: 90,
          useNativeDriver: true,
        }).start(({ finished }) => {
          if (!finished) {
            return;
          }
          setLayoutRotationDeg(targetDeg);
          Animated.timing(labelOpacity, {
            toValue: 1,
            duration: 160,
            useNativeDriver: true,
          }).start();
        });
      });
    },
    [clearLayoutListener, labelOpacity, rotate],
  );

  const finishSpinLayout = useCallback(
    (targetDeg: number) => {
      clearLayoutListener();
      setLayoutRotationDeg(targetDeg);
      labelOpacity.setValue(1);
    },
    [clearLayoutListener, labelOpacity],
  );

  const load = useCallback(async () => {
    try {
      const next = await fetchLotteryCurrent();
      setCurrent(next);
    } catch (error) {
      setCurrent(null);
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取抽奖信息失败');
      }
    } finally {
      setLoaded(true);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
  );

  const spin = async () => {
    if (spinning) {
      return;
    }
    if (!current) {
      modalInfo(loaded ? '暂无进行中的抽奖活动' : '活动加载中，请稍候');
      return;
    }
    if (isBeforeStart(current.startTime)) {
      modalInfo('活动尚未开始');
      return;
    }
    if (prizes.length === 0) {
      modalInfo('活动奖品未配置');
      return;
    }
    if (!current.canDraw) {
      if ((current.chanceBalance ?? 0) <= 0) {
        modalInfo('暂无抽奖次数，请先完成获次条件');
      } else if (current.nextDrawTime) {
        modalInfo(`冷冻期内，下次可抽：${current.nextDrawTime}`);
      } else {
        modalInfo('当前不可抽奖');
      }
      return;
    }

    setSpinning(true);
    try {
      const result = await drawLottery();
      if (result.isMeltdown) {
        const next = angleRef.current + 360 * 5;
        angleRef.current = next;
        watchNearEndLayout(next);
        Animated.timing(rotate, {
          toValue: next,
          duration: 2200,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: true,
        }).start(() => {
          finishSpinLayout(next);
          setSpinning(false);
          modalInfo(result.msg || '大奖过于火爆，正在紧急打包中');
          void load();
        });
        return;
      }

      let winIndex = prizes.findIndex((p) => result.prizeId != null && p.prizeId === result.prizeId);
      if (winIndex < 0 && result.position != null) {
        winIndex = prizes.findIndex((p) => p.position === result.position);
      }
      if (winIndex < 0) {
        winIndex = 0;
      }

      const next = targetRotationForIndex(winIndex, prizes.length, angleRef.current);
      angleRef.current = next;
      watchNearEndLayout(next);
      Animated.timing(rotate, {
        toValue: next,
        duration: 3200,
        easing: Easing.out(Easing.cubic),
        useNativeDriver: true,
      }).start(() => {
        finishSpinLayout(next);
        setSpinning(false);
        const name = result.prizeName || prizes[winIndex]?.prizeName || '奖品';
        modalSuccess(result.msg || `恭喜获得：${name}`);
        void load();
      });
    } catch (error) {
      clearLayoutListener();
      setSpinning(false);
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '抽奖失败');
      }
      void load();
    }
  };

  return (
    <AppBackground dim={false}>
      <PageHeader title="抽奖" />
      <RefreshableScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scroll}
        onRefresh={load}
      >
        {loaded && !current ? (
          <View style={styles.emptyWrap}>
            <Text style={styles.emptyText}>暂无进行中的抽奖活动</Text>
          </View>
        ) : (
          <>
        <View style={styles.hero}>
          <Image source={images.lotteryStage} style={styles.stage} contentFit="cover" contentPosition="bottom" />
          <View style={[styles.wheelWrap, { width: wheelSize, height: wheelSize }]}>
            <Animated.View
              style={{
                transform: [
                  {
                    rotate: rotate.interpolate({
                      inputRange: [0, 360],
                      outputRange: ['0deg', '360deg'],
                    }),
                  },
                ],
              }}
            >
              <LotteryDisk
                size={wheelSize}
                prizes={prizes}
                layoutRotationDeg={layoutRotationDeg}
                rotateAnim={rotate}
                labelOpacity={labelOpacity}
              />
            </Animated.View>
            <View
              style={[
                styles.hubWrap,
                {
                  width: hubWrapSize,
                  height: hubWrapSize,
                  marginLeft: -hubWrapSize / 2,
                  marginTop: -hubWrapSize / 2,
                },
              ]}
              pointerEvents="box-none"
            >
              <View style={[styles.hubFace, { top: Math.round(-17 * hubScale) }]} pointerEvents="none">
                <Svg width={hubWrapSize} height={hubSvgH} viewBox="0 0 78 94">
                  <Defs>
                    <LinearGradient id="hubGold" x1="0" y1="0" x2="0" y2="1">
                      <Stop offset="0" stopColor="#F2D56A" />
                      <Stop offset="1" stopColor="#E0B43C" />
                    </LinearGradient>
                  </Defs>
                  <Path d="M39 4 L49 22 L29 22 Z" fill="url(#hubGold)" />
                  <Circle cx={39} cy={56} r={36} fill="url(#hubGold)" />
                </Svg>
              </View>
              <Pressable
                onPress={() => {
                  void spin();
                }}
                disabled={spinning}
                style={({ pressed }) => [
                  styles.hub,
                  {
                    width: hubButton,
                    height: hubButton,
                    borderRadius: hubButton / 2,
                  },
                  pressed && !spinning && styles.hubPressed,
                  !canPress && styles.hubDisabled,
                ]}
              >
                <Text style={[styles.hubText, { fontSize: hubFont, lineHeight: hubLine }]}>立即</Text>
                <Text style={[styles.hubText, { fontSize: hubFont, lineHeight: hubLine }]}>抽奖</Text>
              </Pressable>
            </View>
          </View>
        </View>

        <View style={styles.infoRow}>
          <View style={styles.infoCard}>
            <Image source={images.lotteryIconChance} style={styles.infoIcon} contentFit="contain" />
            <View style={styles.infoCopy}>
              <Text style={styles.infoLabel}>我的抽奖次数:</Text>
              <Text style={styles.infoValue}>{chanceText}</Text>
            </View>
          </View>
          <Pressable style={styles.infoCard} onPress={() => router.push('/lottery-records')}>
            <Image source={images.lotteryIconRecords} style={styles.infoIcon} contentFit="contain" />
            <Text style={styles.infoLink}>中奖记录</Text>
            <Text style={styles.infoChevron}>{'>'}</Text>
          </Pressable>
        </View>

        <View style={styles.ruleCard}>
          <Text style={styles.ruleTitle}>抽奖规则：</Text>
          {ruleLines.map((line, index) => (
            <Text
              key={`${index}-${line.slice(0, 12)}`}
              style={[
                styles.ruleLine,
                index === ruleLines.length - 1 && line.includes('解释权') ? styles.ruleFoot : null,
              ]}
            >
              {line}
            </Text>
          ))}
        </View>
          </>
        )}
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 16,
    paddingBottom: 28,
  },
  emptyWrap: {
    minHeight: 220,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyText: {
    color: 'rgba(210, 222, 240, 0.8)',
    fontSize: 15,
  },
  hero: {
    width: '100%',
    aspectRatio: 358 / 274,
    alignItems: 'center',
    justifyContent: 'flex-end',
    paddingBottom: '17%',
    marginBottom: 12,
  },
  stage: {
    ...StyleSheet.absoluteFillObject,
  },
  wheelWrap: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  cell: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  cellInner: {
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
  },
  cellCol: {
    flexDirection: 'column',
  },
  cellRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  textStack: {
    alignItems: 'center',
    justifyContent: 'center',
    maxWidth: '100%',
  },
  textStackRow: {
    flexShrink: 0,
    alignItems: 'center',
    justifyContent: 'center',
  },
  prizeName: {
    color: TITLE,
    fontWeight: '700',
    textAlign: 'center',
  },
  prizeDesc: {
    color: DESC,
    textAlign: 'center',
  },
  hubWrap: {
    position: 'absolute',
    left: '50%',
    top: '50%',
    width: 78,
    height: 78,
    marginLeft: -39,
    marginTop: -39,
    alignItems: 'center',
    justifyContent: 'center',
  },
  hubFace: {
    position: 'absolute',
    top: -17,
    left: 0,
  },
  hub: {
    width: 74,
    height: 74,
    borderRadius: 37,
    alignItems: 'center',
    justifyContent: 'center',
  },
  hubPressed: { opacity: 0.88 },
  hubDisabled: { opacity: 0.55 },
  hubText: {
    color: '#3F2A08',
    fontSize: 15,
    fontWeight: '800',
    lineHeight: 20,
  },
  infoRow: {
    flexDirection: 'row',
    gap: 10,
  },
  infoCard: {
    flex: 1,
    minHeight: 72,
    backgroundColor: '#0B1730',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  infoIcon: {
    width: 28,
    height: 28,
  },
  infoCopy: {
    flex: 1,
  },
  infoLabel: {
    color: 'rgba(210, 222, 240, 0.9)',
    fontSize: 12,
  },
  infoValue: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '800',
    marginTop: 2,
  },
  infoLink: {
    flex: 1,
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
  },
  infoChevron: {
    color: 'rgba(200, 214, 236, 0.7)',
    fontSize: 16,
  },
  ruleCard: {
    marginTop: 12,
    backgroundColor: '#0B1730',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 16,
    gap: 8,
  },
  ruleTitle: {
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
  },
  ruleLine: {
    color: 'rgba(210, 222, 240, 0.9)',
    fontSize: 13,
    lineHeight: 21,
  },
  ruleFoot: {
    marginTop: 4,
  },
});
