# 认购返利业务接口文档（第一版）

Base URL：`http://localhost:8080`  
Content-Type：`application/json`

统一响应：

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

- `code = 200` 成功
- `code = 401` 未登录 / token 失效
- `code = 500` 业务失败，看 `msg`
- 列表接口额外返回 `rows`、`total`

---

## 一、App 接口（给前端对接）

注册、登录不需要 token。其余 `/app/**` 必须带：

```
Authorization: Bearer <token>
```

token 来自注册或登录返回。

### 1. 注册

`POST /app/auth/register`

先调 `GET /app/auth/captcha` 拿到图和 uuid，再提交：

```json
{ "phone": "13800000001", "password": "123456", "inviteCode": "5839201", "code": "3", "uuid": "验证码uuid" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| phone | 是 | 手机号，唯一 |
| password | 是 | 密码 |
| inviteCode | 否 | 上级邀请码，7 位数字，注册时系统生成且不重复 |
| code | 是 | 验证码答案 |
| uuid | 是 | `/app/auth/captcha` 返回的 uuid |

返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "token": "eyJ...",
  "memberId": 10001,
  "inviteCode": "5839201"
}
```

邀请码是 7 位随机数字（1000000–9999999），全表唯一，不等于会员 ID。已有会员可执行 sql/biz_invite_code_patch.sql。

### 2. 登录验证码

`GET /app/auth/captcha`

返回：

```json
{
  "code": 200,
  "uuid": "校验用的uuid",
  "img": "base64图片（PNG，不含data:image前缀）",
  "imgType": "png",
  "captchaEnabled": true
}
```

当前是数学验证码，图片为深蓝底、浅色数字，和 App 登录/注册页一致。显示时用 `data:image/png;base64,` 拼在 `img` 前面。验证码 2 分钟有效，一次性使用。App **登录和注册都必须**带验证码。

### 3. 登录

`POST /app/auth/login`

```json
{ "phone": "13800000001", "password": "123456", "code": "3", "uuid": "上一步返回的uuid" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| phone | 是 | 手机号 |
| password | 是 | 密码 |
| code | 是 | 验证码答案 |
| uuid | 是 | `/app/auth/captcha` 返回的 uuid |
| googleCode | 已绑定则必填 | 谷歌验证器 6 位数字 |

返回字段同注册，另有 `gaBound`（是否已绑定谷歌验证）。已绑定但未传或传错 `googleCode` 会失败。

### 3.1 谷歌验证器

需登录。App 用返回的 `otpauthUrl` 生成二维码，用 Google Authenticator / 微软 Authenticator 扫码。

`GET /app/google/status`

```json
{ "bound": false, "enabled": true, "requireWithdraw": true, "issuer": "App" }
```

`GET /app/google/bind` 开始绑定，返回 `secret`、`otpauthUrl`（10 分钟内有效，需再确认）。

`POST /app/google/bind`

```json
{ "googleCode": "123456" }
```

`POST /app/google/unbind` body 同上，解绑也要当前验证码。

资料接口 `GET /app/profile` 增加 `gaStatus`：`0` 未绑定，`1` 已绑定。密钥不会返回。

### 4. 退出登录

`POST /app/auth/logout`

Header 带 `Authorization: Bearer <token>`。服务端会删除 Redis 里的登录态，之后这个 token 不能再用。没带 token 也返回成功，方便前端本地清缓存。

### 5. 我的资料

`GET /app/profile`

返回会员信息，含钱包汇总字段：

```json
{
  "data": {
    "memberId": 10001,
    "phone": "13800000001",
    "inviteCode": "5839201",
    "parentId": null,
    "realName": "",
    "idCard": "",
    "kycStatus": "0",
    "levelId": 1,
    "levelName": "V0",
    "status": "0",
    "cnyAvailable": 0,
    "cnyFrozen": 0,
    "usdtAvailable": 0,
    "teamCount": 0
  }
}
```

`kycStatus`：`0` 未实名，`1` 已实名  
`status`：`0` 正常，`1` 停用

### 6. 实名

`POST /app/kyc`

```json
{ "realName": "张三", "idCard": "110101199001011234" }
```

第一版不接三方核验，提交即视为已实名。后台也可改。

### 7. 邀请信息

`GET /app/invite`

```json
{
  "data": {
    "inviteCode": "5839201",
    "inviteCount": 2,
    "reward": 0
  }
}
```

当前邀请没有奖励，`reward` 固定 0。

### 8. 团队

`GET /app/team`

```json
{
  "data": {
    "level1": [ { "memberId": 10002, "phone": "13800000002", "realName": "李四" } ],
    "level2": [],
    "level3": []
  }
}
```

一级 / 二级 / 三级下线列表。

### 9. 会员等级

`GET /app/levels`

```json
{
  "data": {
    "current": { "memberId": 10001, "levelName": "V0" },
    "levels": [
      { "levelId": 1, "levelName": "V0", "minValidMembers": 0, "minRechargeCny": 0 },
      { "levelId": 2, "levelName": "V1", "minValidMembers": 3, "minRechargeCny": 1000 }
    ]
  }
}
```

有效会员定义（可改）：已实名 + 至少一笔认购。等级按「团队有效人数 + 本人累计 CNY 充值」匹配。

### 10. 签到

`POST /app/checkin`

每个账户每天只能签到一次（库表唯一约束）。成功入账 CNY（默认 2 元，后台「签到规则」可改）。

连续签到刚好达到配置天数时抽奖一次：默认满 180 天有机会获得华为手机（默认概率 1%），满 365 天有机会获得华硕 ROG 笔记本电脑（默认概率 0.5%）。天数、奖品名、概率、开关均后台可配。

返回示例：

```json
{
  "data": {
    "checkinId": 1,
    "checkinDate": "2026-08-20",
    "amount": 2,
    "currency": "CNY",
    "streakDays": 180,
    "checkedToday": true,
    "prizeDrawn": true,
    "prizeWon": false,
    "prizeName": "华为手机",
    "prizeDays": 180,
    "rule": {
      "amount": 2,
      "oncePerDay": true,
      "prizes": [
        { "days": 180, "name": "华为手机", "rate": 1, "enabled": true },
        { "days": 365, "name": "华硕ROG笔记本电脑", "rate": 0.5, "enabled": true }
      ]
    }
  }
}
```

`GET /app/checkin/info` 今日是否已签、当前连续天数、现行规则（不签到）。

`GET /app/checkin/list?pageNum=1&pageSize=10` 签到记录。

### 11. 产品列表

`GET /app/products`

```json
{
  "data": [
    {
      "productId": 1,
      "productName": "提现指定产品",
      "currency": "CNY",
      "price": 100,
      "dailyRebate": 5,
      "durationDays": 30,
      "withdrawRequired": "1",
      "status": "0"
    },
    {
      "productId": 2,
      "productName": "USDT提现指定产品",
      "currency": "USDT",
      "price": 100,
      "dailyRebate": 5,
      "durationDays": 30,
      "withdrawRequired": "1",
      "status": "0"
    }
  ]
}
```

`currency` 为产品结算币种。人民币产品用 CNY 钱包购买并返 CNY；USDT 产品用 USDT 钱包购买并返 USDT。  
`withdrawRequired = 1` 表示认购该币种指定产品后，才允许提现对应币种。

### 12. 认购产品

`POST /app/orders`

```json
{ "productId": 1 }
```

从产品对应币种的可用余额扣 `price`（CNY 产品扣人民币钱包，USDT 产品扣 USDT 钱包）。每天 00:05 按**同一币种**给持仓订单打 `dailyRebate`，打满 `durationDays` 天结束。

`GET /app/orders?pageNum=1&pageSize=10` 我的订单。  
订单 `status`：`0` 持仓中，`1` 已完成。订单带 `currency` 字段。

### 13. 钱包

`GET /app/wallet`

```json
{
  "data": [
    { "currency": "CNY", "available": 202, "frozen": 0 },
    { "currency": "USDT", "available": 0, "frozen": 0 }
  ]
}
```

CNY / USDT 独立账户，不能互转。充值、认购、返利、提现按币种单独结算：用人民币买产品就返人民币、提人民币；用 USDT 买产品就返 U、提 U。签到奖励仍走 CNY。

### 14. 充值申请

`POST /app/recharge`

```json
{ "currency": "CNY", "amount": 300, "remark": "银行卡转账" }
```

```json
{ "currency": "USDT", "amount": 300, "remark": "链上转账哈希/地址" }
```

只提交申请，**后台审核通过才入账到对应币种钱包**。两种币种可同时使用，互不影响。

审核通过后给上级分佣（**同一币种**）：一级 9%、二级 3%、三级 1%。

`GET /app/recharge?pageNum=1&pageSize=10` 我的充值单。  
状态：`0` 待审，`1` 通过，`2` 拒绝。

### 15. 提现申请

`POST /app/withdraw`

已绑定谷歌验证，或后台开启「提现必须谷歌验证」时，body 需带 `googleCode`。未绑定且强制开启时会提示先绑定。

```json
{ "currency": "CNY", "amount": 105, "accountInfo": "银行卡/收款信息占位", "remark": "" }
```

```json
{ "currency": "USDT", "amount": 105, "accountInfo": "USDT收款地址", "remark": "" }
```

规则：
- 人民币最低提现 `biz.withdraw.minAmount`（默认 105）
- USDT 最低提现 `biz.withdraw.minAmount.usdt`（默认 105）
- 提现某币种前，必须已认购该币种且 `withdrawRequired=1` 的产品
- 申请时冻结对应币种余额，后台通过后扣冻结，拒绝则解冻

`GET /app/withdraw?pageNum=1&pageSize=10` 我的提现单。

### 16. 上传图片（Cloudflare R2）

`POST /app/upload`  multipart，字段名 `file`。需要会员 token。

后台通用上传仍是 `POST /common/upload`（若依后台 token）。配好 R2 后文件进桶 `xfzl`，目录：

- 后台通用：`upload/yyyy/MM/dd/`
- 后台头像：`avatar/yyyy/MM/dd/`
- App：`app/yyyy/MM/dd/`

未配置 `R2_PUBLIC_URL` 时，返回的 `fileName` 形如 `/common/r2/upload/...`，浏览器走后端代理读文件。开启 R2 公开访问 / r2.dev 后，把公开域名写进环境变量 `R2_PUBLIC_URL`，就会直接返回 `https://pub-xxxx.r2.dev/...`。

密钥不要写进 yml，用环境变量（或服务器上的 `.r2.env`，`start.sh` 会自动加载）。本地 `.r2.env` 里的 `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` 也可以：

```
R2_ACCESS_KEY=...          # 或 AWS_ACCESS_KEY_ID
R2_SECRET_KEY=...          # 或 AWS_SECRET_ACCESS_KEY
R2_PUBLIC_URL=https://pub-xxxx.r2.dev
```

没配密钥时自动退回本地磁盘 `ruoyi.profile`。

### 17. 公告

后台在 **系统管理 → 通知公告** 新增。类型选「公告」、状态选「正常」，才会出现在 App 首页公告条。类型「通知」只给后台右上角铃铛用。

不需要登录。

`GET /app/notices`

返回最新 20 条：

```json
{
  "code": 200,
  "data": [
    { "noticeId": 1, "noticeTitle": "这是一条公告", "createTime": "2026-08-21 12:00:00" }
  ]
}
```

`GET /app/notices/{noticeId}` 详情，`noticeContent` 已去掉 HTML，可直接展示。关闭或类型不是「公告」时返回失败。
### 18. 运行概览

后台在 **业务管理 → 运行概览** 手改数字。没有真实统计，只给 App 首页展示。

不需要登录。

`GET /app/overview`

```json
{
  "code": 200,
  "data": [
    {
      "itemKey": "satellite",
      "title": "在轨卫星",
      "displayValue": "320 颗",
      "statusText": "正常运行",
      "statusColor": "#3DDC84",
      "imageUrl": "",
      "sort": 1
    },
    {
      "itemKey": "coverage",
      "title": "覆盖国家/地区",
      "displayValue": "150+",
      "statusText": "正常运行",
      "statusColor": "#4DA3FF",
      "imageUrl": "",
      "sort": 2
    },
    {
      "itemKey": "terminal",
      "title": "在线终端",
      "displayValue": "1256000+",
      "statusText": "稳定连接",
      "statusColor": "#4DA3FF",
      "imageUrl": "",
      "sort": 3
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| itemKey | 卡片标识。预置 `satellite` / `coverage` / `terminal`，App 用它匹配本地 3D 图 |
| title | 小标题 |
| displayValue | 大数字，已含单位，直接展示 |
| statusText | 状态文案 |
| statusColor | 状态点颜色 |
| imageUrl | 可选配图 URL，空字符串表示用 App 本地图 |

停用的卡片不会返回。初始化脚本：`sql/biz_overview_patch.sql`

### 19. 关于我们

后台在 **业务管理 → 关于我们** 新增/修改。可多段内容，按 `sort` 排序后给 App 展示。没有真实业务逻辑。

不需要登录。

`GET /app/about`

```json
{
  "code": 200,
  "data": [
    {
      "aboutId": 1,
      "title": "星帆智联",
      "subtitle": "连接星空 · 智联未来",
      "content": "星帆智联聚焦商业航天与卫星互联网应用，以科技连接万物，让星辰触手可及。",
      "imageUrl": "",
      "sort": 1
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| title | 大标题 |
| subtitle | 副标题，可空 |
| content | 正文，已去掉 HTML，可直接展示 |
| imageUrl | 可选配图 URL |

隐藏的内容不会返回。初始化脚本：`sql/biz_about_patch.sql`

### 20. 官方群聊

后台在 **业务管理 → 官方群聊** 上传群二维码。没有真实进群逻辑，App 只展示图片。

不需要登录。

`GET /app/group-chat`

```json
{
  "code": 200,
  "data": [
    {
      "groupId": 1,
      "title": "官方群聊",
      "hint": "扫码进群",
      "qrUrl": "https://example.com/qr.png",
      "remark": "",
      "sort": 1
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| title | 标题 |
| hint | 二维码下方文案，默认「扫码进群」 |
| qrUrl | 二维码图片完整 URL，直接用 Image 展示 |
| remark | 补充说明，可空 |

可返回多条（微信群、QQ群等），按 `sort` 排序。隐藏的不返回。初始化脚本：`sql/biz_group_chat_patch.sql`


---

## 二、建议联调顺序

1. 注册 A（不填邀请码）
2. 注册 B，`inviteCode = A` 的 7 位邀请码
3. 注册 C，`inviteCode = B` 的 7 位邀请码
4. C 实名
5. C 申请充值 300
6. 后台「充值审核」通过 → B 到账 27，A 到账 9
7. C 认购产品 1（扣 100）
8. C 签到（+2）
9. C 申请提现 105 → 后台「提现审核」通过
10. 未认购指定产品时提现会被拒绝

---

## 三、管理后台接口（若依 token）

后台登录仍走若依原接口 `/login`，Header 同样 `Authorization: Bearer <adminToken>`。  
页面在「业务管理」菜单，一般不用直接调这些接口。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/biz/member/list` | 会员列表，查询：phone、inviteCode、kycStatus、status、memberId |
| GET | `/biz/member/{memberId}` | 会员详情 |
| POST | `/biz/member` | 后台新增**顶级会员** `{phone, password}`，无上级，返回 `memberId`/`inviteCode` |
| PUT | `/biz/member` | 改实名、身份证、状态、密码（密码留空不改） |
| PUT | `/biz/member/{memberId}/google/reset` | 后台解绑该会员谷歌验证 |
| GET | `/system/user/profile/google` | 当前后台账号谷歌验证状态 |
| GET | `/system/user/profile/google/bind` | 开始绑定，返回 secret、otpauthUrl |
| POST | `/system/user/profile/google/bind` | 确认绑定 `{googleCode}` |
| POST | `/system/user/profile/google/unbind` | 解绑 `{googleCode}` |
| PUT | `/system/user/{userId}/google/reset` | 管理员给后台账号解绑谷歌验证 |
| POST | `/login` | 后台登录，已绑定则 body 需带 `googleCode` |
| GET | `/biz/member/team/{memberId}?teamLevel=1` | 某会员的 1/2/3 级或全部下线 |
| GET/POST/PUT | `/system/notice` | 通知公告（系统管理菜单，类型选「公告」会展示到 App） |
| GET/POST/PUT | `/biz/product` | 产品列表/新增/修改 |
| GET | `/biz/product/{productId}` | 产品详情 |
| DELETE | `/biz/product/{ids}` | 删除产品 |
| GET | `/biz/order/list` | 认购订单 |
| GET | `/biz/checkin/list` | 签到记录 |
| GET/PUT | `/biz/checkin/rule` | 签到规则（金额、连续天数、奖品、概率） |
| GET | `/biz/checkin/prize/list` | 签到中奖记录 |
| GET | `/biz/recharge/list` | 充值列表 |
| POST | `/biz/recharge` | 后台代提充值单 `{memberId, currency, amount, remark}` |
| PUT | `/biz/recharge/audit` | 审核 `{id, status, auditRemark}`，`status`：`1` 通过 `2` 拒绝 |
| GET | `/biz/withdraw/list` | 提现列表 |
| PUT | `/biz/withdraw/audit` | 提现审核，body 同上 |
| GET | `/biz/walletLog/list` | 资金流水 |
| GET | `/biz/team/list` | 团队关系（会员列表视角） |
| GET/POST/PUT/DELETE | `/biz/level` | 会员等级配置 |
| GET/POST/PUT/DELETE | `/biz/overview` | App 运行概览（手改展示数字） |
| GET/POST/PUT/DELETE | `/biz/about` | App 关于我们（手改展示内容） |
| GET/POST/PUT/DELETE | `/biz/group` | App 官方群聊（上传二维码） |
| GET | `/biz/commission/list` | 分佣记录 |

列表查询通用分页：`pageNum`、`pageSize`。

流水 `bizType`：

| 值 | 含义 |
|---|---|
| CHECKIN | 签到 |
| SUBSCRIBE | 认购扣款 |
| REBATE | 产品日返 |
| RECHARGE | 充值入账 |
| WITHDRAW_FREEZE | 提现冻结 |
| WITHDRAW_SUCCESS | 提现成功 |
| WITHDRAW_REJECT | 提现拒绝解冻 |
| COMMISSION | 团队分佣 |

---

## 四、可改参数（系统管理 → 参数设置）

| 键 | 当前值 | 说明 |
|---|---|---|
| biz.checkin.amount | 2 | 每日签到金额（CNY），建议走后台「签到规则」 |
| biz.checkin.prize1.days / name / rate / enabled | 180 / 华为手机 / 1 / true | 连续签到第一档抽奖 |
| biz.checkin.prize2.days / name / rate / enabled | 365 / 华硕ROG笔记本电脑 / 0.5 / true | 连续签到第二档抽奖 |
| biz.withdraw.minAmount | 105 | 人民币最低提现 |
| biz.withdraw.minAmount.usdt | 105 | USDT 最低提现 |
| biz.team.rate.l1 / l2 / l3 | 9 / 3 / 1 | 充值分佣百分比（同币种） |
| biz.invite.reward | 0 | 邀请奖励，暂未发放 |
| biz.usdt.enabled | true | USDT 开关 |
| biz.google.enabled | true | 谷歌验证总开关 |
| biz.google.requireWithdraw | true | 提现必须先绑定谷歌验证 |
| biz.google.issuer | App | 验证器里显示的名称 |

每日返利任务：定时任务里「产品每日返利」，`dailyRebateTask.execute()`，cron `0 5 0 * * ?`。

初始化脚本：`RuoYi-Vue-springboot2/sql/biz_init.sql`
