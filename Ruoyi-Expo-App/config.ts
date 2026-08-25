import Constants from 'expo-constants';

type Extra = {
  appEnv?: string;
  apiUrl?: string;
  h5Url?: string;
};

const extra = (Constants.expoConfig?.extra ?? {}) as Extra;

export const APP_ENV = extra.appEnv ?? process.env.APP_ENV ?? 'development';

// 优先用 app.config 按 APP_ENV 写入的 extra（已正确覆盖），再回退到 EXPO_PUBLIC_*
const raw = extra.apiUrl?.trim() || process.env.EXPO_PUBLIC_API_URL?.trim() || '';
const rawH5 = extra.h5Url?.trim() || process.env.EXPO_PUBLIC_H5_URL?.trim() || '';

export const config = {
  APP_ENV,
  API_URL: raw.replace(/\/+$/, ''),
  /** H5 站点 origin，用于邀请二维码（手机浏览器打开注册页） */
  H5_URL: rawH5.replace(/\/+$/, ''),
};
