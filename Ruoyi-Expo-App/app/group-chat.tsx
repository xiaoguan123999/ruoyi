import { Linking, StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { colors } from '@/theme/colors';
import { toast } from '@/utils/toast';

export default function GroupChatScreen() {
  return (
    <AppBackground>
      <PageHeader title="官方群聊" />
      <View style={{ paddingHorizontal: 16 }}>
        <GlassCard>
          <Text style={styles.title}>星帆智联官方交流群</Text>
          <Text style={styles.desc}>加入官方群获取项目动态、收益说明与客服支持。演示环境不会跳转真实社群。</Text>
          <View style={{ marginTop: 16 }}>
            <PrimaryButton
              title="立即加入"
              onPress={() => {
                toast('演示环境，暂不跳转');
                void Linking.canOpenURL('https://example.com');
              }}
            />
          </View>
        </GlassCard>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 18, fontWeight: '700' },
  desc: { color: colors.muted, marginTop: 10, lineHeight: 22 },
});
