import { spawnSync } from 'node:child_process';
import { resolve } from 'node:path';

import config from '../app.config';

const profile = process.argv[2] === 'production' ? 'production' : 'preview';
const version = config.version;
const output = resolve(`xfzl-${version}.apk`);

const result = spawnSync(
  'pnpm',
  [
    'exec',
    'eas',
    'build',
    '--platform',
    'android',
    '--profile',
    profile,
    '--local',
    '--non-interactive',
    '--output',
    output,
  ],
  {
    stdio: 'inherit',
    env: {
      ...process.env,
      APP_ENV: profile,
    },
  },
);

if (result.status !== 0) {
  process.exit(result.status ?? 1);
}

console.log(`APK: ${output}`);
console.log(
  profile === 'production'
    ? '下一步：pnpm upload:r2:production'
    : '下一步：pnpm upload:r2:preview',
);
