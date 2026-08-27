import { Redirect, useLocalSearchParams } from 'expo-router';

/** 旧系列详情页入口：跳转到产品 Tab，并选中对应系列 */
export default function ProductSeriesRedirect() {
  const { id } = useLocalSearchParams<{ id: string }>();
  return (
    <Redirect
      href={{
        pathname: '/(tabs)/products',
        params: id ? { seriesId: id } : undefined,
      }}
    />
  );
}
