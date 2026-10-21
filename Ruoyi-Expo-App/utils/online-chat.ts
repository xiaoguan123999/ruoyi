import type { AppServiceChannel, RuoyiUser } from '@/api/types';

/** 后台未配置或链接打不开时使用 */
export const FALLBACK_CHAT_URL =
  'https://chat.rgoslz.com/chat/index?channelId=4819681885c54f5ab5d5fcc4c25a7639';

function channelHttpUrl(channel: AppServiceChannel): string {
  const url = (channel.linkUrl || channel.value || '').trim();
  return /^https?:\/\//i.test(url) ? url : '';
}

/** 后台「链接」类型且配置了跳转链接的渠道，嵌到应用内打开 */
export function isEmbeddableChatChannel(channel: AppServiceChannel): boolean {
  const type = String(channel.channelType || '').toUpperCase();
  if (type && type !== 'LINK') {
    return false;
  }
  return Boolean(channelHttpUrl(channel));
}

export function pickOnlineChatChannel(channels: AppServiceChannel[]): AppServiceChannel | null {
  return channels.find((item) => isEmbeddableChatChannel(item)) ?? null;
}

/** 客服页「来源」，链接里已有 source 时不覆盖 */
const CHAT_VISITOR_SOURCE = 'website_a';

function setParamIfAbsent(params: URLSearchParams, key: string, value: string) {
  if (value && !params.get(key)) {
    params.set(key, value);
  }
}

/**
 * 在后台跳转链接上补访客参数，不改 site / channelId / 域名。
 * 客服页展示认 name、phone、user_id、source；同时保留 userName、pid。
 */
export function withChatVisitorParams(url: string, user?: RuoyiUser | null): string {
  if (!url) {
    return url;
  }
  try {
    const parsed = new URL(url);
    if (user) {
      const name = (user.realName || user.nickName || user.userName || '').trim();
      const phone = (user.phone || '').trim();
      const userId = user.userId ? String(user.userId) : '';
      setParamIfAbsent(parsed.searchParams, 'name', name);
      setParamIfAbsent(parsed.searchParams, 'phone', phone);
      setParamIfAbsent(parsed.searchParams, 'user_id', userId);
      setParamIfAbsent(parsed.searchParams, 'userName', name);
      setParamIfAbsent(parsed.searchParams, 'pid', userId);
    }
    setParamIfAbsent(parsed.searchParams, 'source', CHAT_VISITOR_SOURCE);
    return parsed.toString();
  } catch {
    return url;
  }
}

export function resolveChatChannelUrl(channel: AppServiceChannel, user?: RuoyiUser | null): string {
  return withChatVisitorParams(channelHttpUrl(channel), user);
}

export function resolveFallbackChatUrl(user?: RuoyiUser | null): string {
  return withChatVisitorParams(FALLBACK_CHAT_URL, user);
}

/** 优先后台链接，没有可用配置时用固定客服地址 */
export function resolvePreferredChatUrl(
  channels: AppServiceChannel[],
  user?: RuoyiUser | null,
): { url: string; title: string; fromAdmin: boolean } {
  const channel = pickOnlineChatChannel(channels);
  const adminUrl = channel ? resolveChatChannelUrl(channel, user) : '';
  if (adminUrl) {
    return {
      url: adminUrl,
      title: channel?.name?.trim() || '在线客服',
      fromAdmin: true,
    };
  }
  return {
    url: resolveFallbackChatUrl(user),
    title: '在线客服',
    fromAdmin: false,
  };
}
