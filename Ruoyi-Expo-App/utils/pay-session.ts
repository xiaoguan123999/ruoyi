import { Platform } from 'react-native';

import { config } from '@/config';

const LAST_ORDER_KEY = 'app.recharge.lastOrderNo';

function canUseSession() {
  return Platform.OS === 'web' && typeof sessionStorage !== 'undefined';
}

export function rememberPayOrder(outTradeNo: string) {
  if (canUseSession()) {
    sessionStorage.setItem(LAST_ORDER_KEY, outTradeNo);
  }
}

export function readPayOrder() {
  if (!canUseSession()) {
    return '';
  }
  return sessionStorage.getItem(LAST_ORDER_KEY) || '';
}

export function forgetPayOrder() {
  if (canUseSession()) {
    sessionStorage.removeItem(LAST_ORDER_KEY);
  }
}

export function payReturnUrl() {
  if (Platform.OS === 'web' && typeof window !== 'undefined') {
    return `${window.location.origin}/recharge`;
  }
  return config.H5_URL ? `${config.H5_URL}/recharge` : undefined;
}

export function isPayReturnUrl(url: string) {
  if (!url) {
    return false;
  }
  try {
    const next = new URL(url);
    const path = next.pathname.replace(/\/+$/, '') || '/';
    if (path === '/recharge') {
      return true;
    }
  } catch {
  }
  return /\/recharge(?:\?|$|#)/.test(url);
}
