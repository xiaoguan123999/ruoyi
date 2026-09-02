import { execFileSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { resolve } from 'node:path';

import config from '../app.config';

const BUCKET_NAME = 'xfzl-app';
const PUBLIC_BASE = 'https://download.rgoslz.com';

function run() {
  const appEnv = process.env.APP_ENV === 'production' ? 'production' : 'preview';
  const version = config.version;
  const fileName = `xfzl-${version}.apk`;
  const filePath = resolve(fileName);

  if (!existsSync(filePath)) {
    console.error(`找不到 ${filePath}`);
    console.error(
      appEnv === 'production'
        ? '请先执行：pnpm build:android'
        : '请先执行：pnpm build:android:preview',
    );
    process.exit(1);
  }

  const destination = `${BUCKET_NAME}/${appEnv}/${fileName}`;
  console.log(`Uploading ${fileName} to R2 (${appEnv})...`);

  execFileSync(
    'pnpm',
    [
      'dlx',
      'wrangler',
      'r2',
      'object',
      'put',
      destination,
      '--file',
      filePath,
      '--remote',
      '--cache-control',
      'no-cache, no-store, must-revalidate',
    ],
    { stdio: 'inherit' },
  );

  console.log(`公开地址：${PUBLIC_BASE}/${appEnv}/${fileName}`);
}

run();
