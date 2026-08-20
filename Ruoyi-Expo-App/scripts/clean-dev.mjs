import { existsSync, rmSync } from 'node:fs';
import { resolve } from 'node:path';

const cacheDirs = ['.expo', 'node_modules/.cache', '.metro-health-check'];
for (const dir of cacheDirs) {
  const full = resolve(process.cwd(), dir);
  if (existsSync(full)) {
    rmSync(full, { recursive: true, force: true });
    console.log(`[clean] 已删除 ${dir}`);
  }
}

console.log('[clean] 开发缓存已清理');
