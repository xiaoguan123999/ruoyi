import * as FileSystem from 'expo-file-system/legacy';

import { getToken } from '@/utils/storage';
import { resolvePdfFetchUrl } from '@/utils/pdf-url';

const PDF_MAGIC_B64 = 'JVBE';
const inflight = new Map<string, Promise<string | null>>();

function hashKey(input: string): string {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return `p_${Math.abs(hash).toString(36)}`;
}

async function cacheDir(): Promise<string | null> {
  const base = FileSystem.cacheDirectory;
  if (!base) {
    return null;
  }
  const dir = `${base}pdf-cache/`;
  const info = await FileSystem.getInfoAsync(dir);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  }
  return dir;
}

async function isCompletePdf(uri: string): Promise<boolean> {
  try {
    const info = await FileSystem.getInfoAsync(uri);
    if (!info.exists || info.isDirectory) {
      return false;
    }
    if ('size' in info && typeof info.size === 'number' && info.size < 8) {
      return false;
    }
    const head = await FileSystem.readAsStringAsync(uri, {
      encoding: FileSystem.EncodingType.Base64,
      length: 8,
      position: 0,
    });
    return head.startsWith(PDF_MAGIC_B64);
  } catch {
    return false;
  }
}

function cachePath(dir: string, url: string) {
  return `${dir}${hashKey(url)}.pdf`;
}

/** 仅返回已下完且文件头合法的本地 PDF，未完成的临时文件不算命中 */
export async function getCachedPdfPath(remoteUrl: string): Promise<string | null> {
  const url = resolvePdfFetchUrl(remoteUrl);
  const dir = await cacheDir();
  if (!dir) {
    return null;
  }
  const path = cachePath(dir, url);
  if (!(await isCompletePdf(path))) {
    return null;
  }
  return path;
}

export async function readPdfAsBase64(path: string): Promise<string> {
  return FileSystem.readAsStringAsync(path, { encoding: FileSystem.EncodingType.Base64 });
}

export async function getPdfAuthHeaders(): Promise<Record<string, string>> {
  const token = await getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** 后台写入完整文件后再改名，预览不需要等它结束 */
export async function prefetchPdf(remoteUrl: string): Promise<string | null> {
  const url = resolvePdfFetchUrl(remoteUrl);
  const existing = inflight.get(url);
  if (existing) {
    return existing;
  }

  const task = (async () => {
    const hit = await getCachedPdfPath(url);
    if (hit) {
      return hit;
    }
    const dir = await cacheDir();
    if (!dir) {
      return null;
    }
    const dest = cachePath(dir, url);
    const tmp = `${dest}.tmp`;
    const headers = await getPdfAuthHeaders();
    try {
      await FileSystem.deleteAsync(tmp, { idempotent: true });
      const result = await FileSystem.downloadAsync(
        url,
        tmp,
        Object.keys(headers).length ? { headers } : undefined,
      );
      if (result.status !== 200 || !(await isCompletePdf(result.uri))) {
        await FileSystem.deleteAsync(tmp, { idempotent: true });
        return null;
      }
      await FileSystem.deleteAsync(dest, { idempotent: true });
      await FileSystem.moveAsync({ from: result.uri, to: dest });
      return dest;
    } catch {
      await FileSystem.deleteAsync(tmp, { idempotent: true });
      return null;
    }
  })();

  inflight.set(url, task);
  try {
    return await task;
  } finally {
    inflight.delete(url);
  }
}
