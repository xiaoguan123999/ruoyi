import { existsSync, readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';

import type { ExpoConfig } from 'expo/config';

type AppEnv = 'development' | 'preview' | 'production';

function parseEnvFile(filePath: string): Record<string, string> {
  const result: Record<string, string> = {};
  const text = readFileSync(filePath, 'utf8');
  for (const line of text.split('\n')) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) {
      continue;
    }
    const eq = trimmed.indexOf('=');
    if (eq <= 0) {
      continue;
    }
    const key = trimmed.slice(0, eq).trim();
    let value = trimmed.slice(eq + 1).trim();
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }
    result[key] = value;
  }
  return result;
}

function applyEnv(filePath: string, override: boolean): void {
  if (!existsSync(filePath)) {
    return;
  }
  const parsed = parseEnvFile(filePath);
  for (const [key, value] of Object.entries(parsed)) {
    if (override || !process.env[key]) {
      process.env[key] = value;
    }
  }
}

function loadAppEnv(): AppEnv {
  const raw = process.env.APP_ENV || process.env.EAS_BUILD_PROFILE || '';
  const appEnv: AppEnv =
    raw === 'development' || raw === 'preview' || raw === 'production'
      ? raw
      : process.env.NODE_ENV === 'production'
        ? 'production'
        : 'development';

  const root = process.cwd();
  // 与 .env.example 一致：后者覆盖前者；APP_ENV 对应文件必须能盖掉 Expo 预载的 .env.development
  applyEnv(resolve(root, '.env'), false);
  applyEnv(resolve(root, `.env.${appEnv}`), true);
  applyEnv(resolve(root, '.env.local'), true);
  applyEnv(resolve(root, `.env.${appEnv}.local`), true);
  process.env.APP_ENV = appEnv;
  return appEnv;
}

function loadBundledOtaUpdates() {
  try {
    const raw = JSON.parse(
      readFileSync(join(__dirname, 'config/bundled-ota-updates.json'), 'utf8'),
    ) as { group?: string | null; ids?: unknown };
    const ids = Array.isArray(raw.ids)
      ? raw.ids.filter((id): id is string => typeof id === 'string' && id.length > 0)
      : [];
    return {
      group: typeof raw.group === 'string' && raw.group.length > 0 ? raw.group : null,
      ids,
    };
  } catch {
    return { group: null, ids: [] as string[] };
  }
}

const appEnv = loadAppEnv();
const isProduction = appEnv === 'production';
const easProjectId = (
  process.env.EXPO_PUBLIC_EAS_PROJECT_ID ||
  process.env.EAS_PROJECT_ID ||
  '53c81e46-f43d-4e64-becf-2e7aae3406fd'
).trim();
const expoOwner = (process.env.EXPO_OWNER || '').trim();
const updatesEnabled = Boolean(easProjectId) && appEnv !== 'development';
const bundledOta = loadBundledOtaUpdates();

const config: ExpoConfig = {
  name: '星帆智联',
  slug: 'ruoyi-expo-app',
  ...(expoOwner ? { owner: expoOwner } : {}),
  version: '1.2.6',
  runtimeVersion: {
    policy: 'appVersion',
  },
  orientation: 'default',
  icon: './assets/images/icon.png',
  scheme: 'ruoyi',
  userInterfaceStyle: 'automatic',
  updates: {
    enabled: updatesEnabled,
    url: `https://u.expo.dev/${easProjectId || '53c81e46-f43d-4e64-becf-2e7aae3406fd'}`,
    checkAutomatically: 'ON_ERROR_RECOVERY',
    fallbackToCacheTimeout: 0,
  },
  ios: {
    supportsTablet: true,
    bundleIdentifier: 'com.ruoyi.expoapp',
    requireFullScreen: true,
    infoPlist: {
      NSAppTransportSecurity: {
        NSAllowsLocalNetworking: true,
        NSAllowsArbitraryLoads: !isProduction,
      },
    },
  },
  android: {
    package: 'com.ruoyi.expoapp',
    adaptiveIcon: {
      backgroundColor: '#0B1A33',
      foregroundImage: './assets/images/android-icon-foreground.png',
      backgroundImage: './assets/images/android-icon-background.png',
      monochromeImage: './assets/images/android-icon-monochrome.png',
    },
    predictiveBackGestureEnabled: false,
  },
  web: {
    bundler: 'metro',
    output: 'single',
    favicon: './assets/images/favicon.png',
  },
  plugins: [
    'expo-router',
    'expo-secure-store',
    'expo-localization',
    'expo-web-browser',
    'expo-font',
    'expo-updates',
    [
      'expo-screen-orientation',
      {
        initialOrientation: 'PORTRAIT_UP',
      },
    ],
    [
      'expo-splash-screen',
      {
        image: './assets/images/splash-icon.png',
        resizeMode: 'contain',
        backgroundColor: '#0B1A33',
        dark: {
          backgroundColor: '#0B1A33',
        },
      },
    ],
    [
      'expo-build-properties',
      {
        android: {
          usesCleartextTraffic: !isProduction,
          enableMinifyInReleaseBuilds: true,
          enableShrinkResourcesInReleaseBuilds: true,
          useLegacyPackaging: true,
          buildArchs: ['armeabi-v7a', 'arm64-v8a'],
        },
      },
    ],
    '@config-plugins/react-native-blob-util',
    '@config-plugins/react-native-pdf',
  ],
  extra: {
    appEnv,
    apiUrl: process.env.EXPO_PUBLIC_API_URL ?? '',
    h5Url: process.env.EXPO_PUBLIC_H5_URL ?? '',
    nativeBuildTime: new Date().toISOString(),
    bundledOtaUpdateGroup: bundledOta.group,
    bundledOtaUpdateIds: bundledOta.ids,
    eas: {
      projectId: easProjectId || undefined,
    },
  },
  experiments: {
    typedRoutes: true,
  },
};

export default config;
