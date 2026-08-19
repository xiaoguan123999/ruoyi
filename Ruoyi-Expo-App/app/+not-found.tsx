import { Link, Stack } from 'expo-router';
import { useTranslation } from 'react-i18next';
import { Text, YStack } from 'tamagui';

export default function NotFoundScreen() {
  const { t } = useTranslation();

  return (
    <>
      <Stack.Screen options={{ title: t('notFound'), headerShown: true }} />
      <YStack
        style={{ flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12, padding: 20 }}
      >
        <Text fontSize={18}>{t('notFound')}</Text>
        <Link href="/">
          <Text color="$accent10">{t('backHome')}</Text>
        </Link>
      </YStack>
    </>
  );
}
