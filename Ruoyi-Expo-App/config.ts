import Constants from 'expo-constants';

type Extra = {
  appEnv?: string;
  apiUrl?: string;
  h5Url?: string;
};

const extra = (Constants.expoConfig?.extra ?? {}) as Extra;

export const APP_ENV = extra.appEnv ?? process.env.APP_ENV ?? 'development';

const raw = process.env.EXPO_PUBLIC_API_URL?.trim() || extra.apiUrl?.trim() || '';
const rawH5 = process.env.EXPO_PUBLIC_H5_URL?.trim() || extra.h5Url?.trim() || '';

export const config = {
  APP_ENV,
  API_URL: raw.replace(/\/+$/, ''),
  /** H5 站点 origin，用于邀请二维码（手机浏览器打开注册页） */
  H5_URL: rawH5.replace(/\/+$/, ''),
};
