import type { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { Image } from 'expo-image';
import { Tabs } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { images } from '@/constants/images';
import { colors } from '@/theme/colors';

const TAB_ITEMS = [
  { name: 'index', label: '首页', idle: images.tabHome, active: images.tabHomeActive },
  { name: 'news', label: '新闻', idle: images.tabNews, active: images.tabNewsActive },
  { name: 'products', label: '产品', idle: images.tabProduct, active: images.tabProductActive },
  { name: 'profile', label: '我的', idle: images.tabMine, active: images.tabMineActive },
] as const;

function AppTabBar({ state, navigation }: BottomTabBarProps) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.bar, { paddingBottom: Math.max(insets.bottom, 18) }]}>
      {state.routes.map((route, index) => {
        const item = TAB_ITEMS.find((tab) => tab.name === route.name) ?? TAB_ITEMS[index];
        const focused = state.index === index;
        return (
          <Pressable
            key={route.key}
            onPress={() => navigation.navigate(route.name)}
            style={styles.item}
          >
            <Image
              source={focused ? item.active : item.idle}
              style={styles.icon}
              contentFit="contain"
            />
            <Text style={[styles.label, focused && styles.labelActive]}>{item.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

export default function TabLayout() {
  return (
    <Tabs
      tabBar={(props) => <AppTabBar {...props} />}
      screenOptions={{ headerShown: false }}
    >
      <Tabs.Screen name="index" />
      <Tabs.Screen name="news" />
      <Tabs.Screen name="products" />
      <Tabs.Screen name="profile" />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  bar: {
    flexDirection: 'row',
    backgroundColor: colors.tabBar,
    borderTopWidth: StyleSheet.hairlineWidth,
    borderTopColor: 'rgba(90, 160, 230, 0.18)',
    paddingTop: 8,
    overflow: 'visible',
  },
  item: {
    flex: 1,
    alignItems: 'center',
    paddingTop: 4,
  },
  icon: {
    width: 22,
    height: 22,
  },
  label: {
    marginTop: 4,
    marginBottom: 2,
    fontSize: 11,
    lineHeight: 18,
    height: 18,
    color: '#9AA8BE',
    fontWeight: '600',
    textAlign: 'center',
  },
  labelActive: {
    color: '#4DA3FF',
  },
});
