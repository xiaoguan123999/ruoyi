import { Image } from 'expo-image';
import { Pressable } from 'react-native';
import { Text, YStack } from 'tamagui';

type Props = {
  uri: string;
  onPress: () => void;
  hint: string;
};

export function CaptchaImage({ uri, onPress, hint }: Props) {
  return (
    <Pressable onPress={onPress} accessibilityLabel={hint}>
      <YStack
        background="$color4"
        style={{
          width: 120,
          height: 44,
          borderRadius: 8,
          overflow: 'hidden',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {uri ? (
          <Image source={{ uri }} style={{ width: 120, height: 44 }} contentFit="contain" />
        ) : (
          <Text fontSize={12} color="$color10">
            {hint}
          </Text>
        )}
      </YStack>
    </Pressable>
  );
}
