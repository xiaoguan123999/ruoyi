import AsyncStorage from '@react-native-async-storage/async-storage';
import { Platform } from 'react-native';

const META_KEY = 'video-cache-meta-v1';
const WEB_CACHE_NAME = 'app-video-cache-v1';
/** 总缓存上限约 300MB */
const MAX_BYTES = 300 * 1024 * 1024;

export type VideoCacheItem = {
  id: string;
  remoteUrl: string;
  /** App：本地 file:// 路径；H5：不存路径，走 Cache Storage */
  localUri?: string;
  size: number;
  lastUsedAt: number;
};

export type ResolvePlayResult = {
  uri: string;
  fromCache: boolean;
  /** H5 blob URL，关闭播放器时需要 revoke */
  revokeUri?: string;
};

type MetaMap = Record<string, VideoCacheItem>;

const inflight = new Map<string, Promise<void>>();
const memoryBlobUrls = new Map<string, string>();

function hashKey(input: string): string {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return `v_${Math.abs(hash).toString(36)}`;
}

function guessExt(url: string): string {
  const clean = url.split('?')[0] ?? url;
  const match = clean.match(/\.([a-zA-Z0-9]{2,5})$/);
  return match?.[1]?.toLowerCase() || 'mp4';
}

async function readMeta(): Promise<MetaMap> {
  try {
    const raw = await AsyncStorage.getItem(META_KEY);
    if (!raw) {
      return {};
    }
    const parsed = JSON.parse(raw) as MetaMap;
    return parsed && typeof parsed === 'object' ? parsed : {};
  } catch {
    return {};
  }
}

async function writeMeta(meta: MetaMap): Promise<void> {
  await AsyncStorage.setItem(META_KEY, JSON.stringify(meta));
}

async function touchMeta(id: string, remoteUrl: string, patch: Partial<VideoCacheItem>): Promise<void> {
  const meta = await readMeta();
  const prev = meta[id];
  meta[id] = {
    id,
    remoteUrl,
    size: patch.size ?? prev?.size ?? 0,
    lastUsedAt: Date.now(),
    localUri: patch.localUri ?? prev?.localUri,
  };
  await writeMeta(meta);
}

async function enforceQuota(): Promise<void> {
  const meta = await readMeta();
  const entries = Object.values(meta).sort((a, b) => a.lastUsedAt - b.lastUsedAt);
  let total = entries.reduce((sum, item) => sum + (item.size || 0), 0);
  if (total <= MAX_BYTES) {
    return;
  }

  for (const item of entries) {
    if (total <= MAX_BYTES) {
      break;
    }
    await removeCacheEntry(item.id, item.remoteUrl, item.localUri);
    total -= item.size || 0;
    delete meta[item.id];
  }
  await writeMeta(meta);
}

async function removeCacheEntry(id: string, remoteUrl: string, localUri?: string): Promise<void> {
  const blob = memoryBlobUrls.get(id);
  if (blob) {
    try {
      URL.revokeObjectURL(blob);
    } catch {
      // ignore
    }
    memoryBlobUrls.delete(id);
  }

  if (Platform.OS === 'web') {
    try {
      const cache = await caches.open(WEB_CACHE_NAME);
      await cache.delete(remoteUrl);
    } catch {
      // ignore
    }
    return;
  }

  if (localUri) {
    try {
      const FileSystem = await import('expo-file-system/legacy');
      await FileSystem.deleteAsync(localUri, { idempotent: true });
    } catch {
      // ignore
    }
  }
}

async function ensureNativeDir(): Promise<string | null> {
  const FileSystem = await import('expo-file-system/legacy');
  const base = FileSystem.cacheDirectory;
  if (!base) {
    return null;
  }
  const dir = `${base}videos/`;
  const info = await FileSystem.getInfoAsync(dir);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  }
  return dir;
}

async function nativeHasFile(uri: string): Promise<boolean> {
  try {
    const FileSystem = await import('expo-file-system/legacy');
    const info = await FileSystem.getInfoAsync(uri);
    return !!info.exists && !info.isDirectory;
  } catch {
    return false;
  }
}

async function webGetCachedBlob(remoteUrl: string): Promise<Blob | null> {
  try {
    const cache = await caches.open(WEB_CACHE_NAME);
    const hit = await cache.match(remoteUrl);
    if (!hit || !hit.ok) {
      return null;
    }
    return await hit.blob();
  } catch {
    return null;
  }
}

async function webPutCache(remoteUrl: string): Promise<number> {
  const cache = await caches.open(WEB_CACHE_NAME);
  const res = await fetch(remoteUrl, { mode: 'cors', credentials: 'omit' });
  if (!res.ok) {
    throw new Error(`download failed: ${res.status}`);
  }
  const clone = res.clone();
  await cache.put(remoteUrl, clone);
  const blob = await res.blob();
  return blob.size || 0;
}

async function nativeDownload(id: string, remoteUrl: string): Promise<{ localUri: string; size: number }> {
  const dir = await ensureNativeDir();
  if (!dir) {
    throw new Error('cacheDirectory unavailable');
  }
  const FileSystem = await import('expo-file-system/legacy');
  const localUri = `${dir}${hashKey(`${id}:${remoteUrl}`)}.${guessExt(remoteUrl)}`;
  const result = await FileSystem.downloadAsync(remoteUrl, localUri);
  const info = await FileSystem.getInfoAsync(result.uri);
  const size =
    info.exists && 'size' in info && typeof info.size === 'number' ? info.size : 0;
  return { localUri: result.uri, size };
}

/**
 * 后台写入缓存（不阻塞播放）。同 id 并发下载会合并。
 */
export async function prefetchVideo(id: string, remoteUrl: string): Promise<void> {
  const url = remoteUrl.trim();
  if (!id || !url) {
    return;
  }

  const existing = inflight.get(id);
  if (existing) {
    return existing;
  }

  const task = (async () => {
    try {
      const meta = await readMeta();
      const cached = meta[id];

      if (Platform.OS === 'web') {
        const blob = await webGetCachedBlob(url);
        if (blob) {
          await touchMeta(id, url, { size: blob.size });
          return;
        }
        const size = await webPutCache(url);
        await touchMeta(id, url, { size });
        await enforceQuota();
        return;
      }

      if (cached?.localUri && (await nativeHasFile(cached.localUri))) {
        await touchMeta(id, url, { localUri: cached.localUri, size: cached.size });
        return;
      }

      const { localUri, size } = await nativeDownload(id, url);
      await touchMeta(id, url, { localUri, size });
      await enforceQuota();
    } catch {
      // 缓存失败不影响播放，静默忽略（常见原因：H5 CORS）
    } finally {
      inflight.delete(id);
    }
  })();

  inflight.set(id, task);
  return task;
}

/** 列表加载后预缓存全部视频（串行，避免打满带宽） */
export async function prefetchVideos(
  items: Array<{ id: string; videoUrl?: string | null }>,
): Promise<void> {
  for (const item of items) {
    const url = item.videoUrl?.trim();
    if (!url) {
      continue;
    }
    await prefetchVideo(item.id, url);
  }
}

/**
 * 优先返回本地缓存地址；未命中则返回远程地址，并触发后台缓存。
 */
export async function resolvePlayUrl(id: string, remoteUrl: string): Promise<ResolvePlayResult> {
  const url = remoteUrl.trim();
  if (!id || !url) {
    return { uri: remoteUrl, fromCache: false };
  }

  if (Platform.OS === 'web') {
    const meta = await readMeta();
    const cachedMeta = meta[id];
    if (cachedMeta && cachedMeta.remoteUrl !== url) {
      await removeCacheEntry(id, cachedMeta.remoteUrl, cachedMeta.localUri);
      const next = await readMeta();
      delete next[id];
      await writeMeta(next);
      memoryBlobUrls.delete(id);
    }

    const memo = memoryBlobUrls.get(id);
    if (memo) {
      await touchMeta(id, url, {});
      return { uri: memo, fromCache: true, revokeUri: memo };
    }

    const blob = await webGetCachedBlob(url);
    if (blob) {
      const objectUrl = URL.createObjectURL(blob);
      memoryBlobUrls.set(id, objectUrl);
      await touchMeta(id, url, { size: blob.size });
      return { uri: objectUrl, fromCache: true, revokeUri: objectUrl };
    }

    void prefetchVideo(id, url);
    return { uri: url, fromCache: false };
  }

  const meta = await readMeta();
  const cached = meta[id];
  if (cached && cached.remoteUrl !== url) {
    await removeCacheEntry(id, cached.remoteUrl, cached.localUri);
    delete meta[id];
    await writeMeta(meta);
  } else if (cached?.localUri && (await nativeHasFile(cached.localUri))) {
    await touchMeta(id, url, { localUri: cached.localUri, size: cached.size });
    return { uri: cached.localUri, fromCache: true };
  }

  void prefetchVideo(id, url);
  return { uri: url, fromCache: false };
}

export function revokePlayUri(uri?: string, id?: string): void {
  if (!uri || !uri.startsWith('blob:')) {
    return;
  }
  try {
    URL.revokeObjectURL(uri);
  } catch {
    // ignore
  }
  if (id && memoryBlobUrls.get(id) === uri) {
    memoryBlobUrls.delete(id);
  }
}
