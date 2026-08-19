import { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { mockTeamMembers, mockTeamSummary } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function TeamScreen() {
  const [level, setLevel] = useState<1 | 2 | 3>(1);
  const members = mockTeamMembers[level];

  return (
    <AppBackground>
      <PageHeader title="我的团队" />
      <ScrollView contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 24 }}>
        <GlassCard>
          <View style={styles.headRow}>
            <Text style={styles.empty} />
            <Text style={styles.col}>一级</Text>
            <Text style={styles.col}>二级</Text>
            <Text style={styles.col}>三级</Text>
          </View>
          <Row label="注册人数" a={mockTeamSummary.level1.register} b={mockTeamSummary.level2.register} c={mockTeamSummary.level3.register} />
          <Row label="激活人数" a={mockTeamSummary.level1.active} b={mockTeamSummary.level2.active} c={mockTeamSummary.level3.active} />
          <Row
            label="认购金额"
            a={`$ ${mockTeamSummary.level1.subscribeUsd}\n¥ ${mockTeamSummary.level1.subscribeCny}`}
            b={`${mockTeamSummary.level2.subscribeUsd}\n${mockTeamSummary.level2.subscribeCny}`}
            c={`${mockTeamSummary.level3.subscribeUsd}\n${mockTeamSummary.level3.subscribeCny}`}
          />
        </GlassCard>

        <GlassCard style={{ marginTop: 12 }}>
          <View style={styles.tabs}>
            {([1, 2, 3] as const).map((n) => (
              <Pressable key={n} onPress={() => setLevel(n)} style={styles.tab}>
                <Text style={[styles.tabText, level === n && styles.tabOn]}>{['', '一级', '二级', '三级'][n]}</Text>
                {level === n ? <View style={styles.underline} /> : null}
              </Pressable>
            ))}
          </View>
          <View style={styles.listHead}>
            <Text style={styles.th}>姓名</Text>
            <Text style={styles.th}>电话</Text>
            <Text style={styles.th}>累计充值</Text>
          </View>
          {members.map((m) => (
            <View key={m.phone} style={styles.listRow}>
              <Text style={styles.td}>{m.name}</Text>
              <Text style={styles.td}>{m.phone}</Text>
              <Text style={styles.td}>{`$ ${m.usd}\n¥ ${m.cny}`}</Text>
            </View>
          ))}
        </GlassCard>
      </ScrollView>
    </AppBackground>
  );
}

function Row({ label, a, b, c }: { label: string; a: string | number; b: string | number; c: string | number }) {
  return (
    <View style={styles.headRow}>
      <View style={styles.labelBox}>
        <Text style={styles.label}>{label}</Text>
      </View>
      <Text style={styles.col}>{a}</Text>
      <Text style={styles.col}>{b}</Text>
      <Text style={styles.col}>{c}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  headRow: { flexDirection: 'row', alignItems: 'center', marginTop: 8 },
  empty: { width: 78 },
  col: { flex: 1, color: colors.text, textAlign: 'center', fontSize: 12 },
  labelBox: { width: 78, borderWidth: 1, borderColor: colors.inputBorder, borderRadius: 6, paddingVertical: 4 },
  label: { color: colors.text, fontSize: 11, textAlign: 'center' },
  tabs: { flexDirection: 'row', justifyContent: 'space-around', marginBottom: 8 },
  tab: { alignItems: 'center', paddingVertical: 6, minWidth: 64 },
  tabText: { color: colors.muted },
  tabOn: { color: colors.text, fontWeight: '700' },
  underline: { marginTop: 4, height: 2, width: 28, backgroundColor: '#E24B4B' },
  listHead: { flexDirection: 'row', paddingVertical: 8 },
  listRow: { flexDirection: 'row', paddingVertical: 10, borderTopWidth: StyleSheet.hairlineWidth, borderTopColor: 'rgba(255,255,255,0.08)' },
  th: { flex: 1, color: colors.muted, textAlign: 'center' },
  td: { flex: 1, color: colors.text, textAlign: 'center', fontSize: 12 },
});
