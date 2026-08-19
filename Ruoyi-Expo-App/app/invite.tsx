import { Image } from 'expo-image';
import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { mockUser } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function InviteScreen() {
  return (
    <AppBackground>
      <PageHeader title="邀请好友" />
      <View style={{ paddingHorizontal: 16 }}>
        <Text style={styles.h1}>邀请好友</Text>
        <Text style={styles.h2}>一起探索星辰大海</Text>
        <GlassCard style={{ marginTop: 16, alignItems: 'center' }}>
          <Text style={styles.caption}>我的邀请码</Text>
          <View style={styles.codeBox}>
            <Text style={styles.code}>{mockUser.inviteCode}</Text>
          </View>
          <View style={styles.qr} />
          <Image source={images.inviteFlow} style={styles.flow} contentFit="contain" />
        </GlassCard>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  h1: { color: colors.text, fontSize: 28, fontWeight: '800' },
  h2: { color: colors.gold, marginTop: 6, fontSize: 16 },
  caption: { color: colors.muted, marginBottom: 10 },
  codeBox: {
    borderWidth: 1,
    borderColor: colors.inputBorder,
    borderRadius: 8,
    paddingHorizontal: 28,
    paddingVertical: 8,
  },
  code: { color: colors.text, fontSize: 28, fontWeight: '800', letterSpacing: 4 },
  qr: { width: 120, height: 120, backgroundColor: '#D9D9D9', marginVertical: 16 },
  flow: { width: '100%', height: 150 },
});
