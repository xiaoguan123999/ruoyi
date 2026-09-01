# 星帆智联应用商店

纯前端页面，数据在 `src/data.ts`。桌面三列卡片，手机单列，支持中英文和深色模式。

每个应用的跳转地址写在 `links` 里：

- `web`：网页版
- `android`：Android 下载（apk 或应用市场）
- `ios`：App Store / 企业签

填了哪个字段，对应按钮才会出现。点按钮会新开标签页。

```bash
npm install
npm run dev
```
