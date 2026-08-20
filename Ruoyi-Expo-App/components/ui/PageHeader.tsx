import { useRouter } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { colors } from '@/theme/colors';

type Props = {
  title: string;
  right?: React.ReactNode;
  showBack?: boolean;
};

export function PageHeader({ title, right, showBack = true }: Props) {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.wrap, { paddingTop: insets.top + 6 }]}>
      <View style={styles.side}>
        {showBack ? (
          <Pressable onPress={() => router.back()} hitSlop={12} style={styles.backBtn}>
            <Text style={styles.back}>‹</Text>
          </Pressable>
        ) : null}
      </View>
      <Text style={styles.title}>{title}</Text>
      <View style={styles.side}>{right}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingBottom: 10,
  },
  side: { width: 44, alignItems: 'center', justifyContent: 'center' },
  backBtn: { width: 44, alignItems: 'center', justifyContent: 'center' },
  back: { color: colors.text, fontSize: 32, lineHeight: 34, fontWeight: '300' },
  title: { flex: 1, textAlign: 'center', color: colors.text, fontSize: 18, fontWeight: '600' },
});
