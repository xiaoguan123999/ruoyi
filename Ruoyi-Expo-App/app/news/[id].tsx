import { useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, Text } from 'react-native';

import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { images } from '@/constants/images';
import { mockNews } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function NewsDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const item = mockNews.find((n) => n.id === id) ?? mockNews[0];

  return (
    <AppBackground source={images.newsBg}>
      <PageHeader title="" />
      <ScrollView contentContainerStyle={{ padding: 16, paddingBottom: 32 }}>
        <GlassCard>
          <Text style={styles.title}>{item.title}</Text>
          <Text style={styles.date}>{item.date}</Text>
          <Text style={styles.body}>{item.body}</Text>
        </GlassCard>
      </ScrollView>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  title: { color: colors.text, fontSize: 20, fontWeight: '800', lineHeight: 28 },
  date: { color: colors.muted, marginTop: 8, marginBottom: 16 },
  body: { color: colors.text, fontSize: 14, lineHeight: 24 },
});
