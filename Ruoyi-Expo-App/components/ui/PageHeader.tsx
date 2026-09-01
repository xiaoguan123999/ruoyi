import { useRouter } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { useStableSafeTop } from '@/hooks/useStableSafeTop';
import { colors } from '@/theme/colors';

type Props = {
  title: string;
  right?: React.ReactNode;
  showBack?: boolean;
  /** 自定义返回；不传则 router.back() */
  onBack?: () => void;
};

export function PageHeader({ title, right, showBack = true, onBack }: Props) {
  const router = useRouter();
  const top = useStableSafeTop();

  return (
    <View style={[styles.wrap, { paddingTop: top + 6 }]}>
      <View style={styles.side}>
        {showBack ? (
          <Pressable
            onPress={() => {
              if (onBack) {
                onBack();
                return;
              }
              router.back();
            }}
            hitSlop={12}
            style={styles.backBtn}
          >
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
