import type { AppServiceChannel, RuoyiUser } from '@/api/types';

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

/** 在后台跳转链接上补登录用户身份，不改 channelId / 域名 */
export function withChatVisitorParams(url: string, user?: RuoyiUser | null): string {
  if (!url || !user) {
    return url;
  }
  try {
    const parsed = new URL(url);
    const name = (user.realName || user.nickName || user.userName || '').trim();
    const phone = (user.phone || '').trim();
    if (name && !parsed.searchParams.get('userName')) {
      parsed.searchParams.set('userName', name);
    }
    if (phone && !parsed.searchParams.get('phone')) {
      parsed.searchParams.set('phone', phone);
    }
    if (user.userId && !parsed.searchParams.get('pid')) {
      parsed.searchParams.set('pid', String(user.userId));
    }
    return parsed.toString();
  } catch {
    return url;
  }
}

export function resolveChatChannelUrl(channel: AppServiceChannel, user?: RuoyiUser | null): string {
  return withChatVisitorParams(channelHttpUrl(channel), user);
}
