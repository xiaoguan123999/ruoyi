import { spawn } from 'node:child_process';
import { createServer } from 'node:net';

const BASE_PORT = 9527;

function isPortFree(port) {
  return new Promise((resolve) => {
    const server = createServer();
    server.once('error', () => resolve(false));
    server.once('listening', () => {
      server.close(() => resolve(true));
    });
    server.listen(port, '0.0.0.0');
  });
}

async function findFreePort(start) {
  let port = start;
  while (!(await isPortFree(port))) {
    console.log(`[dev] ${port} 已被占用，改用 ${port + 1}`);
    port += 1;
  }
  return port;
}

const port = await findFreePort(BASE_PORT);
console.log(`[dev] 使用端口 ${port}`);

const child = spawn('expo', [...process.argv.slice(2), '--port', String(port)], {
  stdio: 'inherit',
  env: process.env,
  shell: process.platform === 'win32',
});

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});
