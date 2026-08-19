import { useRouter } from 'expo-router';
import { Image } from 'expo-image';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { images } from '@/constants/images';
import { mockNews } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function NewsScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  return (
    <AppBackground source={images.newsBg}>
      <ScrollView contentContainerStyle={{ paddingTop: insets.top + 8, paddingBottom: 24, paddingHorizontal: 16 }}>
        <Text style={styles.title}>新闻资讯</Text>
        <View style={{ gap: 12, marginTop: 16 }}>
          {mockNews.map((item) => (
            <Pressable key={item.id} onPress={() => router.push(`/news/${item.id}`)}>
              <GlassCard style={styles.card}>
                <Image source={item.cover} style={styles.cover} contentFit="cover" />
                <View style={styles.body}>
                  <Text style={styles.headline} numberOfLines={2}>
                    {item.title}
                  </Text>
                  <Text style={styles.summary} numberOfLines={2}>
                    {item.summary}
                  </Text>
                  <Text style={styles.date}>{item.date}</Text>
                </View>
                <Text style={styles.chevron}>›</Text>
              </GlassCard>
            </Pressable>
          ))}
        </View>
      </ScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 22, fontWeight: '800', textAlign: 'center' },
  card: { flexDirection: 'row', alignItems: 'center', gap: 10, padding: 10 },
  cover: { width: 72, height: 72, borderRadius: 8 },
  body: { flex: 1 },
  headline: { color: colors.text, fontSize: 14, fontWeight: '700', lineHeight: 20 },
  summary: { color: colors.muted, fontSize: 12, marginTop: 4, lineHeight: 17 },
  date: { color: colors.muted, fontSize: 11, marginTop: 6 },
  chevron: { color: colors.text, fontSize: 22, opacity: 0.6 },
});
