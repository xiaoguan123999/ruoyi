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
  const labelR =
    n <= 3 ? radius * 0.62 : n <= 5 ? radius * 0.58 : n <= 7 ? radius * 0.54 : radius * 0.5;
  const chord = 2 * labelR * Math.sin((slice * Math.PI) / 360);
  const boxW = Math.max(52, Math.min(chord * 0.82, radius * (n <= 4 ? 0.72 : n <= 6 ? 0.58 : 0.48)));
  const boxH = Math.max(52, Math.min(radius * (n <= 4 ? 0.55 : n <= 6 ? 0.48 : 0.4), chord * 1.05));

  if (n <= 3) {
    return { slice, labelR, boxW, boxH, nameSize: 12, descSize: 9, img: 52, maxDesc: 3, hubRatio: 0.34 };
  }
  if (n === 4) {
    return { slice, labelR, boxW, boxH, nameSize: 11, descSize: 9, img: 46, maxDesc: 3, hubRatio: 0.32 };
  }
  if (n <= 6) {
    return { slice, labelR, boxW, boxH, nameSize: 10, descSize: 8, img: 36, maxDesc: 2, hubRatio: 0.3 };
  }
  return { slice, labelR, boxW, boxH, nameSize: 9, descSize: 8, img: 28, maxDesc: 2, hubRatio: 0.28 };
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

function sectorBoxSize(chrome: ReturnType<typeof sectorChrome>, side: boolean) {
  if (side) {
    return {
      width: Math.max(48, chrome.boxW * 0.92),
      height: Math.max(64, chrome.boxH * 1.22),
    };
  }
  return {
    width: Math.max(80, chrome.boxW * 1.28),
    height: Math.max(52, chrome.boxH * 0.92),
  };
}

/** 第 i 扇区中心角 = i * slice，首项正对顶部指针 */
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
        const rad = ((mid - 90) * Math.PI) / 180;
        const cx = r + chrome.labelR * Math.cos(rad);
        const cy = r + chrome.labelR * Math.sin(rad);
        const screenMid = mid + layoutRotationDeg;
        const side = isSideSector(screenMid);
        const box = sectorBoxSize(chrome, side);
        const lines = descLines(prize.prizeDesc, side ? Math.min(chrome.maxDesc, 2) : chrome.maxDesc);
        const imgStyle = side
          ? { width: chrome.img, height: chrome.img, marginTop: 2 }
          : { width: chrome.img, height: chrome.img };

        const textBlock = (
          <View style={side ? styles.textStack : styles.textStackRow}>
            <Text
              style={[
                styles.prizeName,
                {
                  fontSize: chrome.nameSize,
                  lineHeight: chrome.nameSize + 2,
                  textAlign: side ? 'center' : 'left',
                },
                prize.nameColor ? { color: prize.nameColor } : null,
              ]}
              numberOfLines={2}
            >
              {prize.prizeName}
            </Text>
            {lines.map((line, lineIdx) => (
              <Text
                key={`${prize.prizeId || index}-d-${lineIdx}`}
                style={[
                  styles.prizeDesc,
                  {
                    fontSize: chrome.descSize,
                    lineHeight: chrome.descSize + 2,
                    textAlign: side ? 'center' : 'left',
                  },
                  prize.descColor ? { color: prize.descColor } : null,
                ]}
                numberOfLines={1}
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
            <View style={[styles.cellInner, side ? styles.cellCol : styles.cellRow]}>
              {textBlock}
              {prize.imageUrl ? (
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
  const rotate = useRef(new Animated.Value(0)).current;
  const labelOpacity = useRef(new Animated.Value(1)).current;
  const angleRef = useRef(0);
  const layoutListenerRef = useRef<string | null>(null);
  const layoutSwitchedRef = useRef(false);
  const [spinning, setSpinning] = useState(false);
  /** 屏幕布局角度：减速末段只切换一次到终值 */
  const [layoutRotationDeg, setLayoutRotationDeg] = useState(0);
  const [current, setCurrent] = useState<AppLotteryCurrent | null>(null);

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
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取抽奖信息失败');
      }
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
      modalInfo('活动加载中，请稍候');
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
            <View style={styles.hubWrap} pointerEvents="box-none">
              <View style={styles.hubFace} pointerEvents="none">
                <Svg width={78} height={94} viewBox="0 0 78 94">
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
                  pressed && !spinning && styles.hubPressed,
                  !canPress && styles.hubDisabled,
                ]}
              >
                <Text style={styles.hubText}>立即</Text>
                <Text style={styles.hubText}>抽奖</Text>
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
      </RefreshableScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  scroll: {
    paddingHorizontal: 16,
    paddingBottom: 28,
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
    overflow: 'hidden',
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
    gap: 2,
    paddingHorizontal: 2,
  },
  textStack: {
    alignItems: 'center',
    justifyContent: 'center',
    maxWidth: '100%',
  },
  textStackRow: {
    flexShrink: 1,
    alignItems: 'flex-start',
    justifyContent: 'center',
    maxWidth: '58%',
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
