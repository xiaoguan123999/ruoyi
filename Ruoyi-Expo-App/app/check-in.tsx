import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { toast } from '@/utils/toast';

export default function CheckInScreen() {
  const [checked, setChecked] = useState(18);
  const days = useMemo(() => Array.from({ length: 31 }, (_, i) => i + 1), []);

  return (
    <AppBackground source={images.checkinBg}>
      <PageHeader title="每日签到" />
      <View style={styles.body}>
        <GlassCard>
          <Text style={styles.streak}>连续签到 {checked} 天</Text>
        </GlassCard>
        <GlassCard style={{ marginTop: 12 }}>
          <Text style={styles.month}>2026年8月</Text>
          <View style={styles.week}>
            {['日', '一', '二', '三', '四', '五', '六'].map((d) => (
              <Text key={d} style={styles.weekItem}>
                {d}
              </Text>
            ))}
          </View>
          <View style={styles.grid}>
            {days.map((d) => (
              <View key={d} style={[styles.day, d <= checked && styles.dayOn]}>
                <Text style={styles.dayText}>{d}</Text>
              </View>
            ))}
          </View>
          <View style={{ marginTop: 16 }}>
            <PrimaryButton
              title="立即签到"
              onPress={() => {
                setChecked((v) => Math.min(31, v + 1));
                toast('签到成功，获得 2 元');
              }}
            />
          </View>
        </GlassCard>
        <GlassCard style={{ marginTop: 12 }}>
          <Text style={styles.ruleTitle}>签到规则</Text>
          <Text style={styles.rule}>1、每天签到可以获得2元</Text>
          <Text style={styles.rule}>2、连续签到可累计奖励</Text>
        </GlassCard>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  body: { paddingHorizontal: 16 },
  streak: { color: colors.text, fontSize: 18, fontWeight: '700' },
  month: { color: colors.text, textAlign: 'center', fontSize: 16, marginBottom: 10 },
  week: { flexDirection: 'row' },
  weekItem: { flex: 1, textAlign: 'center', color: colors.muted, fontSize: 12 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', marginTop: 8 },
  day: { width: '14.28%', alignItems: 'center', paddingVertical: 6 },
  dayOn: { backgroundColor: 'rgba(140,70,50,0.55)', borderRadius: 6 },
  dayText: { color: colors.text },
  ruleTitle: { color: colors.text, fontWeight: '700', marginBottom: 8 },
  rule: { color: colors.muted, lineHeight: 22 },
});
