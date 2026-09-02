import { execSync } from "node:child_process";
import { writeFileSync } from "node:fs";
import { resolve } from "node:path";

const OUTPUT_PATH = resolve("config/bundled-ota-updates.json");

function readJsonValue(text: string, start: number): { value: unknown; end: number } | null {
  const open = text[start];
  if (open !== "{" && open !== "[") {
    return null;
  }

  let depth = 0;
  let inString = false;
  let escape = false;

  for (let index = start; index < text.length; index += 1) {
    const char = text[index];

    if (inString) {
      if (escape) {
        escape = false;
        continue;
      }

      if (char === "\\") {
        escape = true;
        continue;
      }

      if (char === '"') {
        inString = false;
      }

      continue;
    }

    if (char === '"') {
      inString = true;
      continue;
    }

    if (char === "{" || char === "[") {
      depth += 1;
      continue;
    }

    if (char === "}" || char === "]") {
      depth -= 1;

      if (depth === 0) {
        try {
          return {
            value: JSON.parse(text.slice(start, index + 1)),
            end: index + 1,
          };
        } catch {
          return null;
        }
      }
    }
  }

  return null;
}

function extractJson(stdout: string): unknown {
  const values: unknown[] = [];

  for (let index = 0; index < stdout.length; index += 1) {
    const char = stdout[index];
    if (char !== "{" && char !== "[") {
      continue;
    }

    const parsed = readJsonValue(stdout, index);
    if (!parsed) {
      continue;
    }

    values.push(parsed.value);
    index = parsed.end - 1;
  }

  if (values.length === 0) {
    throw new Error("eas update 没有输出 JSON，无法写入 bundled OTA id");
  }

  return values.length === 1 ? values[0] : values;
}

function isUpdateRecord(record: Record<string, unknown>) {
  return (
    typeof record.id === "string" &&
    (typeof record.platform === "string" ||
      typeof record.group === "string" ||
      typeof record.runtimeVersion === "string" ||
      typeof record.createdAt === "string")
  );
}

function collectUpdateIds(value: unknown, ids: Set<string>, groups: Set<string>) {
  if (Array.isArray(value)) {
    for (const item of value) {
      collectUpdateIds(item, ids, groups);
    }
    return;
  }

  if (!value || typeof value !== "object") {
    return;
  }

  const record = value as Record<string, unknown>;

  if (isUpdateRecord(record) && typeof record.id === "string" && record.id.trim()) {
    ids.add(record.id.trim());

    if (typeof record.group === "string" && record.group.trim()) {
      groups.add(record.group.trim());
    }
  }

  for (const nested of Object.values(record)) {
    if (nested && typeof nested === "object") {
      collectUpdateIds(nested, ids, groups);
    }
  }
}

function writeBundledFromPayload(payload: unknown, sourceLabel: string) {
  const ids = new Set<string>();
  const groups = new Set<string>();
  collectUpdateIds(payload, ids, groups);

  if (ids.size === 0) {
    console.error(`${sourceLabel} JSON:\n`, payload);
    throw new Error("解析不到 update id，无法写入 bundled OTA id");
  }

  const bundled = {
    updatedAt: new Date().toISOString(),
    group: [...groups][0] ?? null,
    ids: [...ids],
  };

  writeFileSync(OUTPUT_PATH, `${JSON.stringify(bundled, null, 2)}\n`);
  console.log(`已写入 ${OUTPUT_PATH}`);
  console.log(JSON.stringify(bundled, null, 2));
  console.log("接下来打 APK 并上传覆盖下载地址，不要再发一次 eas update。");
}

function runEasJson(command: string) {
  return execSync(command, {
    encoding: "utf8",
    stdio: ["inherit", "pipe", "inherit"],
    env: {
      ...process.env,
      APP_ENV: "production",
    },
  });
}

function syncLatestPublishedUpdate() {
  const stdout = runEasJson(
    "eas update:list --branch production --limit 1 --json --non-interactive",
  );
  writeBundledFromPayload(extractJson(stdout), "eas update:list");
}

function run() {
  const syncLatest = process.argv.includes("--sync-latest");

  if (syncLatest) {
    syncLatestPublishedUpdate();
    return;
  }

  const stdout = runEasJson(
    "eas update --channel production --environment production --auto --non-interactive --json",
  );

  try {
    writeBundledFromPayload(extractJson(stdout), "eas update");
  } catch (error) {
    console.warn("解析 eas update JSON 失败，改为读取 production 通道最新一条，不再重新发布。");
    console.warn(error);
    syncLatestPublishedUpdate();
  }
}

run();
