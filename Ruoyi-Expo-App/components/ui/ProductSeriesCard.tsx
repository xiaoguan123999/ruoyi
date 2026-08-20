import type { ImageSource } from 'expo-image';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, View } from 'react-native';

type Props = {
  name: string;
  cover: ImageSource;
  onPress: () => void;
};

export function ProductSeriesCard({ name, cover, onPress }: Props) {
  return (
    <Pressable onPress={onPress} style={styles.wrap}>
      <View style={styles.card}>
        <Image source={cover} style={styles.heroImg} contentFit="cover" />
        <View style={styles.footer}>
          <Text style={styles.plan}>{name}</Text>
          <Text style={styles.more}>了解详情 ›</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  wrap: {
    marginHorizontal: 16,
  },
  card: {
    borderRadius: 14,
    overflow: 'hidden',
    backgroundColor: '#0A1528',
    borderWidth: 1,
    borderColor: 'rgba(88, 148, 220, 0.28)',
  },
  heroImg: {
    width: '100%',
    aspectRatio: 16 / 9,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  plan: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 15,
  },
  more: {
    color: 'rgba(180, 200, 230, 0.75)',
    fontSize: 13,
  },
});
