import { YStack } from 'tamagui';

type Props = {
  children: React.ReactNode;
  center?: boolean;
};

export function Screen({ children, center }: Props) {
  return (
    <YStack
      background="$background"
      style={{
        flex: 1,
        padding: 20,
        gap: 16,
        justifyContent: center ? 'center' : undefined,
      }}
    >
      {children}
    </YStack>
  );
}
