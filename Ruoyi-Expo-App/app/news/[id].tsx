import { useLocalSearchParams } from 'expo-router';
import { ScrollView, StyleSheet, Text, View } from 'react-native';

import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { mockNews } from '@/constants/mock';
import { colors } from '@/theme/colors';

export default function NewsDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const item = mockNews.find((n) => n.id === id) ?? mockNews[0];

  return (
    <View style={styles.page}>
      <PageHeader title="" />
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.content}
      >
        <GlassCard>
          <Text style={styles.title}>{item.title}</Text>
          <Text style={styles.date}>{item.date}</Text>
          <Text style={styles.body}>{item.body}</Text>
        </GlassCard>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  page: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  content: {
    paddingHorizontal: 16,
    paddingBottom: 32,
  },
  title: {
    color: colors.text,
    fontSize: 20,
    fontWeight: '800',
    lineHeight: 28,
  },
  date: {
    color: 'rgba(150, 175, 210, 0.75)',
    marginTop: 8,
    marginBottom: 16,
    fontSize: 13,
  },
  body: {
    color: colors.text,
    fontSize: 14,
    lineHeight: 24,
  },
});
