import Constants from 'expo-constants';

type Extra = {
  appEnv?: string;
  apiUrl?: string;
};

const extra = (Constants.expoConfig?.extra ?? {}) as Extra;

export const APP_ENV = extra.appEnv ?? process.env.APP_ENV ?? 'development';

const raw = process.env.EXPO_PUBLIC_API_URL?.trim() || extra.apiUrl?.trim() || '';

export const config = {
  APP_ENV,
  API_URL: raw.replace(/\/+$/, ''),
};
