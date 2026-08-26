# ruoyi-expo-app

用 **Expo（React Native）** 做若依的 iOS / Android / H5 壳，替代官方 **uni-app** 移动端。管理端仍用若依 Vue；接口对齐开源 `RuoYi-Vue`（前后端分离）。

| 若依官方 | 本壳 |
| --- | --- |
| 管理端 Vue | 不动 |
| 移动端 uni-app | Expo Router + Tamagui |
| `utils/request.js`（axios + Bearer） | `api/request.ts`（fetch + Bearer） |
| `POST /login`、`GET /captchaImage`、`GET /getInfo` | 同样这几个接口 |
| H5 另打 uni 的 h5 | 同一套代码 `pnpm web` / `pnpm build:web` |

壳只做到：启动页 → 登录（验证码）→ 可选注册 → 两个 Tab。业务页后面对 `request("/system/...")`。

---

## 1. 技术栈

- Expo SDK 55、Expo Router、Tamagui 2 RC、TanStack Query、i18next、Biome
- 鉴权：若依 JWT（`Authorization: Bearer`），不用第三方 Auth 套件
- 包管理：pnpm 10，`nodeLinker: hoisted`（Tamagui / Reanimated 需要）
- 原生：Dev Client，不用 Expo Go；H5 不用 Dev Client

版本：`expo@~55.0.16`、`react-native@0.83.6`、`react@19.2.0`、Tamagui **全部** `2.0.0-rc.38`。

---

## 2. 初始化

```bash
corepack enable && corepack prepare pnpm@10.32.1 --activate
pnpm create expo-app@latest ruoyi-expo-app --template tabs@sdk-55
cd ruoyi-expo-app
# 若生成了 src/app，挪到根目录 app/
```

`package.json` 加 `"packageManager": "pnpm@10.32.1"`。新建 `pnpm-workspace.yaml`：

```yaml
nodeLinker: hoisted
packages:
  - "packages/*"
```

```bash
pnpm exec expo install expo-router expo-secure-store expo-image expo-localization \
  expo-dev-client expo-web-browser expo-build-properties expo-font expo-splash-screen \
  react-dom react-native-web @react-native-async-storage/async-storage \
  react-native-gesture-handler react-native-reanimated react-native-screens \
  react-native-safe-area-context react-native-svg react-native-worklets \
  react-native-keyboard-controller react-native-get-random-values @expo/metro-runtime

pnpm add tamagui@2.0.0-rc.38 @tamagui/core@2.0.0-rc.38 @tamagui/config@2.0.0-rc.38 \
  @tamagui/animations-react-native@2.0.0-rc.38 @tamagui/lucide-icons-2@2.0.0-rc.38 \
  @tamagui/metro-plugin@2.0.0-rc.38 @tamagui/babel-plugin@2.0.0-rc.38 \
  @tanstack/react-query@^5.97.0 i18next react-i18next

pnpm add -D typescript@~5.9.2 @types/react@~19.2.14 @biomejs/biome@^2.4.10
```

scripts：`start` / `ios` / `android` / `web`（`expo start --web`）/ `build:web`（`expo export --platform web --output-dir dist --clear`）。

- `tsconfig`：`paths` `@/*` → `./*`
- `babel`：`@tamagui/babel-plugin` + **最后** `react-native-reanimated/plugin`
- `metro`：`withTamagui(...)`，`config: "./tamagui.config.ts"`
- `app.config.ts`：`web.bundler = "metro"`，`web.output = "single"`（SPA，产物主要为 `_expo`/`assets` + `index.html`）；Android 开发开 `usesCleartextTraffic`
- `eas.json`：`development` / `preview` / `production`，channel 同名
- `.env`：`EXPO_PUBLIC_API_URL=http://192.168.x.x:8080`（不要加 `/dev-api`，那是若依 Vue 的代理前缀）

`tamagui.config.ts` 直接用 `@tamagui/config` 的默认主题即可，壳阶段不必自建 palette。

```
app/
  _layout.tsx   splash.tsx   sign-in.tsx   sign-up.tsx   +html.tsx
  (tabs)/_layout.tsx  (tabs)/index.tsx  (tabs)/profile.tsx
api/request.ts  api/ruoyi-auth.ts  api/types.ts  api/auth-state.ts
utils/storage.ts
tamagui.config.ts
```

`app/` 只放路由。

---

## 3. 对接若依

对齐开源 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 登录接口（与官方 uni-app App 相同）：

| 接口 | Token | 说明 |
| --- | --- | --- |
| `GET /captchaImage` | 否 | `img`、`uuid`、`captchaEnabled`（老字段 `captchaOnOff`） |
| `POST /login` | 否 | `{ username, password, code, uuid }` → 根上 `token` |
| `GET /getInfo` | 是 | 根上 `user` / `roles` / `permissions` |
| `POST /logout` | 是 | |
| `POST /register` | 否 | 默认关，需 `sys.account.registerUser=true` |

响应是 `AjaxResult`：`{ code, msg, token? }`，**token 不在 `data` 里**。HTTP 常为 200，失败看 `code`。请求头：`Authorization: Bearer <token>`。默认 **用户名**登录（`admin`），不是手机号。

若是 **RuoYi-Vue-Plus**：多半是 `/auth/login`、`data.access_token`。只改 `api/ruoyi-auth.ts`。

### `api/types.ts`

```ts
export type AjaxResult<T = unknown> = {
  code: number;
  msg: string;
  token?: string;
  img?: string;
  uuid?: string;
  captchaEnabled?: boolean;
  captchaOnOff?: boolean;
  user?: T;
  roles?: string[];
  permissions?: string[];
  rows?: unknown[];
  total?: number;
};

export type RuoyiUser = {
  userId: number;
  userName: string;
  nickName?: string;
  avatar?: string;
};

export type LoginBody = {
  username: string;
  password: string;
  code?: string;
  uuid?: string;
};
```

### `utils/storage.ts`

原生用 `expo-secure-store` 存 token；Web 用 `localStorage`。key：`RUOYI_ADMIN_TOKEN`。导出 `getToken` / `getTokenSync` / `setToken` / `removeToken`。

### `api/request.ts`

```ts
import { handleUnauthorized } from "@/api/auth-state";
import type { AjaxResult } from "@/api/types";
import { config } from "@/config";
import { getToken } from "@/utils/storage";

export class ApiError extends Error {
  constructor(message: string, public code: number) {
    super(message);
    this.name = "ApiError";
  }
}

export async function request<T>(
  path: string,
  options: { method?: string; body?: unknown; withToken?: boolean } = {},
): Promise<AjaxResult<T>> {
  if (!config.API_URL) throw new ApiError("尚未配置 API 地址", -1);

  const { method = "GET", body, withToken = true } = options;
  const headers = new Headers({ "Content-Type": "application/json;charset=utf-8" });
  if (withToken) {
    const token = await getToken();
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${config.API_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const json = (await response.json()) as AjaxResult<T>;
  const code = Number(json.code);

  if (code === 401) {
    void handleUnauthorized();
    throw new ApiError(json.msg || "登录已过期", 401);
  }
  if (code !== 200) throw new ApiError(json.msg || "请求失败", code);
  return json;
}
```

业务接口：`request("/system/user/list")`，用 `useQuery` 包一层。列表常见 `{ code: 200, rows, total }`，字段在根上。

### `api/ruoyi-auth.ts`

- `fetchCaptcha()` → `GET /captchaImage`，`img` 拼成 `data:image/gif;base64,...`
- `login(body)` → `POST /login`（不带 Token）→ `setToken` → `GET /getInfo`
- `logout()` → `POST /logout`，失败也清本地 token
- 有 token 即视为已登录；`getInfo` 非 401 不要误踢

401 清 token 并跳转 `/sign-in`。登录成功后短时间忽略迟到的 401。

---

## 4. 页面

根布局：Query + i18n + Tamagui + 登录守卫。公开路由：`/splash`、`/sign-in`、`/sign-up`。有 token 进 `/(tabs)`。

登录页：用户名、密码、验证码图（点图刷新；后端关闭验证码则隐藏）。成功后 `router.replace("/(tabs)")`。API 未配时 toast，不要白屏。

注册页同结构；若依默认关注册，失败 toast 后端 `msg`。

Tab：首页（展示当前 API 地址或「未配置」）、我的（退出调用 `logout()`）。

`app/+html.tsx` 仅 Web：viewport `width=device-width, viewport-fit=cover`。桌面可加 `maxWidth: 480`。

---

## 5. 跑起来

```bash
pnpm install
pnpm ios        # 或 android
pnpm web        # H5
```

1. 若依听 `0.0.0.0:8080`，不要只绑 `127.0.0.1`
2. 原生 `.env` 填局域网 IP；H5 本机可用 `http://localhost:8080`
3. 用若依 `admin` 测登录和验证码
4. **H5 有 CORS**，若依放行 `http://localhost:8081`（端口看 Metro）。生产建议 Nginx 同源反代：

```nginx
location /prod-api/ { proxy_pass http://127.0.0.1:8080/; }
location / { try_files $uri /index.html; }
```

生产 `EXPO_PUBLIC_API_URL=https://h5.example.com/prod-api`。

Android 开发可明文 HTTP；正式包关闭明文并走 HTTPS。

---

## 6. 发版（壳跑通后再做）

**H5：** `pnpm build:web` → 上传 `dist/`。没有热更新通道，发版就是覆盖静态文件。

**原生：** `eas login` → `eas init`（本项目自己的 projectId）。第一次 `eas build -p android --profile preview` 生成 keystore。iOS 用 Apple 账号走 `eas credentials -p ios`。channel 写在 `eas.json`，随 build 创建。热更新：`eas update --channel preview`，只含 JS；加原生模块或改 channel 必须重打安装包。

---

## 7. 验收

- [ ] 原生能进启动页；`pnpm web` 能进登录页
- [ ] 验证码能显示、能点刷新（或后端关闭时不显示）
- [ ] `admin` 能登录进 Tab；刷新 H5 仍登录
- [ ] 无 token 进登录；401 踢回登录
- [ ] 注册关闭时 toast 后端原文
- [ ] 未配 API 不白屏
- [ ] `pnpm build:web` 成功；Nginx 刷新 `/sign-in` 不 404

---

## 8. 不要做的事

- 不要拿官方 uni-app 工程当壳再往里塞 RN
- 不要在请求路径上加 `/dev-api`
- 不要按手机号验证码登录来做（开源若依是用户名 + 图形验证码）
- 不要在壳里做菜单权限树；`permissions` 以后做按钮时再读 `getInfo`
