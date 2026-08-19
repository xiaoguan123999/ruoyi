import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

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
  applyEnv(resolve(root, '.env'), false);
  applyEnv(resolve(root, `.env.${appEnv}`), false);
  applyEnv(resolve(root, '.env.local'), true);
  applyEnv(resolve(root, `.env.${appEnv}.local`), true);
  process.env.APP_ENV = appEnv;
  return appEnv;
}

const appEnv = loadAppEnv();
const isProduction = appEnv === 'production';

const config: ExpoConfig = {
  name: '星帆智联',
  slug: 'ruoyi-expo-app',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/images/icon.png',
  scheme: 'ruoyi',
  userInterfaceStyle: 'automatic',
  ios: {
    supportsTablet: true,
    bundleIdentifier: 'com.ruoyi.expoapp',
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
      backgroundColor: '#E6F4FE',
      foregroundImage: './assets/images/android-icon-foreground.png',
      backgroundImage: './assets/images/android-icon-background.png',
      monochromeImage: './assets/images/android-icon-monochrome.png',
    },
    predictiveBackGestureEnabled: false,
  },
  web: {
    bundler: 'metro',
    output: 'static',
    favicon: './assets/images/favicon.png',
  },
  plugins: [
    'expo-router',
    'expo-secure-store',
    'expo-localization',
    'expo-web-browser',
    'expo-font',
    [
      'expo-splash-screen',
      {
        image: './assets/images/splash-icon.png',
        resizeMode: 'contain',
        backgroundColor: '#ffffff',
        dark: {
          backgroundColor: '#000000',
        },
      },
    ],
    [
      'expo-build-properties',
      {
        android: {
          usesCleartextTraffic: !isProduction,
        },
      },
    ],
  ],
  extra: {
    appEnv,
    apiUrl: process.env.EXPO_PUBLIC_API_URL ?? '',
  },
  experiments: {
    typedRoutes: true,
  },
};

export default config;
