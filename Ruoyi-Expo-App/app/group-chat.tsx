import { StyleSheet, Text, View } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

export default function GroupChatScreen() {
  return (
    <AppBackground source={images.pageBg} dim={false} contentPosition="top right">
      <PageHeader title="官方群聊" />
      <View style={styles.body}>
        <View style={styles.card}>
          <View style={styles.qrPlaceholder} />
          <Text style={styles.hint}>扫码进群</Text>
        </View>
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  body: {
    flex: 1,
    paddingHorizontal: 28,
    paddingTop: 24,
    alignItems: 'center',
  },
  card: {
    width: '100%',
    maxWidth: 320,
    borderRadius: 14,
    paddingHorizontal: 28,
    paddingTop: 28,
    paddingBottom: 24,
    alignItems: 'center',
    backgroundColor: 'rgba(10, 24, 52, 0.78)',
    borderWidth: 1,
    borderColor: 'rgba(110, 185, 255, 0.28)',
  },
  qrPlaceholder: {
    width: '100%',
    aspectRatio: 1,
    maxWidth: 240,
    borderRadius: 8,
    backgroundColor: '#C9CED6',
  },
  hint: {
    marginTop: 18,
    color: colors.text,
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 1,
  },
});
