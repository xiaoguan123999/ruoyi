import { StyleSheet, Text, View } from 'react-native';

export type MockDataBannerMode = 'full' | 'partial';

type Props = {
  /** full：整页未对接；partial：部分字段使用 UI 占位 */
  mode?: MockDataBannerMode;
  message?: string;
};

const DEFAULT_MESSAGE: Record<MockDataBannerMode, string> = {
  full: '当前页面使用模拟数据，尚未对接后端接口',
  partial: '部分展示数据为 UI 占位，接口字段未完整对接',
};

export function MockDataBanner({ mode = 'full', message }: Props) {
  const label = mode === 'full' ? '模拟数据' : '部分模拟';
  const text = message ?? DEFAULT_MESSAGE[mode];

  return (
    <View style={[styles.banner, mode === 'partial' && styles.bannerPartial]}>
      <View style={[styles.badge, mode === 'partial' && styles.badgePartial]}>
        <Text style={[styles.badgeText, mode === 'partial' && styles.badgeTextPartial]}>
          {label}
        </Text>
      </View>
      <Text style={[styles.message, mode === 'partial' && styles.messagePartial]}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginHorizontal: 16,
    marginBottom: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 180, 80, 0.45)',
    backgroundColor: 'rgba(255, 160, 60, 0.12)',
  },
  bannerPartial: {
    borderColor: 'rgba(120, 185, 255, 0.45)',
    backgroundColor: 'rgba(61, 139, 255, 0.12)',
  },
  badge: {
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
    backgroundColor: 'rgba(255, 160, 60, 0.28)',
  },
  badgePartial: {
    backgroundColor: 'rgba(61, 139, 255, 0.28)',
  },
  badgeText: {
    color: '#FFD89A',
    fontSize: 11,
    fontWeight: '700',
  },
  badgeTextPartial: {
    color: '#A8D4FF',
  },
  message: {
    flex: 1,
    color: 'rgba(255, 230, 200, 0.92)',
    fontSize: 11,
    lineHeight: 16,
  },
  messagePartial: {
    color: 'rgba(200, 225, 255, 0.92)',
  },
});
