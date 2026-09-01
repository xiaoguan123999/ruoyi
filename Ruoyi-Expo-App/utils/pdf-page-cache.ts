import * as FileSystem from 'expo-file-system/legacy';
import PdfPageImage from 'expo-pdf-page-image';

import { pdfCacheId } from '@/utils/pdf-cache';

export type CachedPdfPage = {
  uri: string;
  width: number;
  height: number;
};

type Meta = {
  count: number;
  scale: number;
  pages: { width: number; height: number }[];
};

const memory = new Map<string, CachedPdfPage[]>();
const inflight = new Map<string, Promise<CachedPdfPage[] | null>>();

function toFileUri(path: string) {
  return path.startsWith('file:') ? path : `file://${path}`;
}

async function pagesDir(remoteUrl: string): Promise<string | null> {
  const base = FileSystem.cacheDirectory;
  if (!base) {
    return null;
  }
  const dir = `${base}pdf-page-cache/${pdfCacheId(remoteUrl)}/`;
  const info = await FileSystem.getInfoAsync(dir);
  if (!info.exists) {
    await FileSystem.makeDirectoryAsync(dir, { intermediates: true });
  }
  return dir;
}

function pagePath(dir: string, index: number) {
  return `${dir}page-${String(index).padStart(3, '0')}.png`;
}

/** 已渲成图的页面，第二次打开直接出清晰图 */
export async function getCachedPdfPages(remoteUrl: string): Promise<CachedPdfPage[] | null> {
  const id = pdfCacheId(remoteUrl);
  const hit = memory.get(id);
  if (hit?.length) {
    return hit;
  }

  const dir = await pagesDir(remoteUrl);
  if (!dir) {
    return null;
  }
  try {
    const metaUri = `${dir}meta.json`;
    const metaInfo = await FileSystem.getInfoAsync(metaUri);
    if (!metaInfo.exists) {
      return null;
    }
    const meta = JSON.parse(await FileSystem.readAsStringAsync(metaUri)) as Meta;
    if (!meta.count || meta.pages.length !== meta.count) {
      return null;
    }
    const pages: CachedPdfPage[] = [];
    for (let i = 0; i < meta.count; i += 1) {
      const path = pagePath(dir, i);
      const info = await FileSystem.getInfoAsync(path);
      if (!info.exists) {
        return null;
      }
      pages.push({
        uri: toFileUri(path),
        width: meta.pages[i].width,
        height: meta.pages[i].height,
      });
    }
    memory.set(id, pages);
    return pages;
  } catch {
    return null;
  }
}

async function generatePages(fileUri: string, scale: number) {
  try {
    return await PdfPageImage.generateAllPages(toFileUri(fileUri), scale);
  } catch {
    if (scale <= 1.05) {
      return [];
    }
    try {
      return await PdfPageImage.generateAllPages(toFileUri(fileUri), 1);
    } catch {
      return [];
    }
  }
}

/** 把本地 PDF 渲成图片并落盘，供下次直接展示 */
export async function rasterizePdfPages(
  fileUri: string,
  remoteUrl: string,
  scale: number,
): Promise<CachedPdfPage[] | null> {
  const existing = await getCachedPdfPages(remoteUrl);
  if (existing?.length) {
    return existing;
  }

  const id = pdfCacheId(remoteUrl);
  const running = inflight.get(id);
  if (running) {
    return running;
  }

  const task = (async () => {
    const dir = await pagesDir(remoteUrl);
    if (!dir) {
      return null;
    }
    const renderScale = Math.min(2, Math.max(1, scale));
    const generated = await generatePages(fileUri, renderScale);
    if (!generated.length) {
      return null;
    }

    const pages: CachedPdfPage[] = [];
    const metaPages: { width: number; height: number }[] = [];
    for (let i = 0; i < generated.length; i += 1) {
      const src = generated[i];
      const dest = pagePath(dir, i);
      await FileSystem.deleteAsync(dest, { idempotent: true });
      await FileSystem.copyAsync({ from: src.uri, to: dest });
      pages.push({
        uri: toFileUri(dest),
        width: src.width,
        height: src.height,
      });
      metaPages.push({ width: src.width, height: src.height });
    }
    await FileSystem.writeAsStringAsync(
      `${dir}meta.json`,
      JSON.stringify({
        count: pages.length,
        scale: renderScale,
        pages: metaPages,
      } satisfies Meta),
    );
    try {
      await PdfPageImage.cleanupPages(generated.map((item) => item.uri));
    } catch {
      // 系统缓存目录清理由系统处理
    }
    memory.set(id, pages);
    return pages;
  })();

  inflight.set(id, task);
  try {
    return await task;
  } finally {
    inflight.delete(id);
  }
}
