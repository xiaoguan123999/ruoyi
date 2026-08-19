import * as Localization from 'expo-localization';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from '@/i18n/en';
import zh from '@/i18n/zh';

const languageCode = Localization.getLocales()[0]?.languageCode;
const lng = languageCode?.startsWith('zh') ? 'zh' : languageCode === 'en' ? 'en' : 'zh';

void i18n.use(initReactI18next).init({
  lng,
  fallbackLng: 'zh',
  resources: {
    zh: { translation: zh },
    en: { translation: en },
  },
  interpolation: { escapeValue: false },
});

export default i18n;
