import { config } from '@/config';

/** 跨域 R2 没有 CORS 头时走业务 API 代理 */
export function toApiR2ProxyUrl(uri: string): string | null {
  const api = config.API_URL;
  if (!api || typeof uri !== 'string' || !uri.trim()) {
    return null;
  }
  try {
    const href =
      typeof window !== 'undefined' && window.location?.href ? window.location.href : api;
    const target = new URL(uri, href);
    const apiOrigin = new URL(api).origin;
    if (target.origin === apiOrigin) {
      return null;
    }
    const proxyIdx = target.pathname.indexOf('/common/r2/');
    if (proxyIdx >= 0) {
      return `${api}${target.pathname.slice(proxyIdx)}${target.search}`;
    }
    const isR2Host = /\.r2\.dev$/i.test(target.hostname);
    const key = target.pathname.replace(/^\//, '');
    if (!key || (!isR2Host && !/\.(pdf|mp4|webm|mov)$/i.test(target.pathname))) {
      return null;
    }
    return `${api}/common/r2/${key}${target.search}`;
  } catch {
    return null;
  }
}

export function resolvePdfFetchUrl(uri: string): string {
  return toApiR2ProxyUrl(uri) ?? uri;
}
