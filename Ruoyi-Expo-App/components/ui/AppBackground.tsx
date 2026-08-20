import type { ImageContentPosition, ImageSource } from 'expo-image';
import { Image } from 'expo-image';
import { StyleSheet, View } from 'react-native';

import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

type Props = {
  children: React.ReactNode;
  source?: ImageSource;
  dim?: boolean;
  contentPosition?: ImageContentPosition;
};

export function AppBackground({
  children,
  source = images.pageBg,
  dim = true,
  contentPosition = 'top',
}: Props) {
  return (
    <View style={styles.root}>
      <Image
        source={source}
        style={StyleSheet.absoluteFill}
        contentFit="cover"
        contentPosition={contentPosition}
      />
      {dim ? <View style={styles.dim} /> : null}
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  dim: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(4, 10, 24, 0.28)',
  },
});
