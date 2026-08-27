# 认购返利业务接口文档（第一版）

Base URL：`http://localhost:8080`  
Content-Type：`application/json`

统一响应：

```json
{ "code": 200, "msg": "操作成功", "data": {} }
```

| 字段 | 说明 |
|---|---|
| code | `200` 成功；`401` 未登录/token 失效；`500` 失败，看 `msg` |
| msg | 提示文案。失败原因、校验错误都看这里 |
| data | 业务数据。空成功（如提交实名）没有这个字段 |

分页接口（签到记录、订单、充值、提现）返回 `rows` + `total`，不走 `data`。

Swagger：`http://localhost:8080/swagger-ui/index.html`，分组 `App-*`。每个接口的 Schema 里都有字段中文说明。

---

## 一、App 接口（给前端对接）

注册、登录不需要 token。公告、运行概览、关于我们、官方群聊、新闻、视频轮播、**客服中心**、**注册推广规则** 的 GET 也不需要 token。其余 `/app/**` 必须带：

```
Authorization: Bearer <token>
```

token 来自注册或登录返回。

### 1. 注册

`POST /app/auth/register`

先调 `GET /app/auth/captcha` 拿到 `text` 和 uuid，再提交：

```json
{ "phone": "13800000001", "password": "123456", "inviteCode": "5839201", "code": "1234", "uuid": "验证码uuid" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| phone | 是 | 手机号，唯一 |
| password | 是 | 登录密码 |
| payPassword | 建议填 | 支付/交易密码，4-20 位。注册页「交易密码」应传到这里 |
| inviteCode | 否 | 上级邀请码，7 位数字。注册时绑定后不可改上级，请核对后再提交 |
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
  "text": "1234",
  "captchaEnabled": true
}
```

4 位数字，App 直接展示在输入框右侧，点右侧刷新。`text` 就是用户要填的内容。验证码 2 分钟有效，一次性使用。App **登录和注册都必须**带验证码。

### 3. 登录

`POST /app/auth/login`

```json
{ "phone": "13800000001", "password": "123456", "code": "1234", "uuid": "上一步返回的uuid" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| phone | 是 | 手机号 |
| password | 是 | 密码 |
| code | 是 | 验证码答案 |
| uuid | 是 | `/app/auth/captcha` 返回的 uuid |
| googleCode | 否 | 忽略，App 登录不校验谷歌验证 |

返回字段同注册，另有 `gaBound`。App 会员登录、提现都不需要谷歌验证。

### 3.1 谷歌验证器

需登录。App 用返回的 `otpauthUrl` 生成二维码，用 Google Authenticator / 微软 Authenticator 扫码。

`GET /app/google/status`

```json
{ "bound": false, "enabled": true, "requireWithdraw": false, "issuer": "App" }
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
    "cnyProductIncome": 0,
    "cnyAssistValue": 0,
    "usdtAvailable": 0,
    "usdtFrozen": 0,
    "usdtProductIncome": 0,
    "usdtAssistValue": 0,
    "teamCount": 0,
    "hasPayPassword": false
  }
}
```

`kycStatus`：`0` 未实名，`1` 已实名  
`status`：`0` 正常，`1` 停用  

资产卡字段：`available` 可用余额，`productIncome` 该币种累计产品日返，`assistValue` 推广收益累计（签到 `CHECKIN`、实名奖励 `KYC_REWARD`、邀请 `INVITE`、团队分佣 `COMMISSION`、等级奖励 `LEVEL_REWARD`）。App 仍用该字段展示，无需改前端。

### 6. 实名

`POST /app/kyc`

请求：

```json
{ "realName": "张三", "idCard": "110101199001011237" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| realName | 是 | 2–20 个中文，可含间隔号 `·` |
| idCard | 是 | 18 位身份证，校验码正确；不能与其他会员重复 |

返回（没有 data）：

```json
{ "code": 200, "msg": "操作成功" }
```

| 字段 | 说明 |
|---|---|
| code | 200 成功 |
| msg | 失败原因：姓名格式不对、身份证号不正确、该身份证号已被使用、已实名不能重复提交 |

不接三方核验。格式和唯一性通过即 `kycStatus=1`，没有审核中。后台改会员资料**允许**身份证重复。

实名成功后：
- 若该用户注册时填了邀请码，系统按后台配置给邀请人发放**推广奖励**（默认 2 元，每人只因该被邀请人发一次）。
- 本人的**实名注册奖励**不自动入账，需再调领取接口，人民币或 USDT 任选其一，每人一次。

### 6.1 修改密码

`POST /app/password`（`PUT` 也可以）

### 5.1 支付 / 交易密码

认购必须带支付密码。未设置时 `POST /app/orders` 返回「请先设置支付密码」。

`POST /app/payPassword`（别名 `POST /app/tradePassword`，`PUT` 也可以）

未设置过：

```json
{ "payPassword": "123456" }
```

已设置过，改密：

```json
{ "oldPassword": "123456", "newPassword": "654321", "confirmPassword": "654321" }
```

`payPassword` / `newPassword` / `tradePassword` 都认。资料里 `hasPayPassword` 表示是否已设置，哈希不会返回。

需登录。App「密码设置」页：原密码、新密码、确认新密码。

```json
{ "oldPassword": "admin123", "newPassword": "654321", "confirmPassword": "654321" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| oldPassword | 是 | 原密码 |
| newPassword | 是 | 新密码，5–20 位 |
| confirmPassword | 是 | 须与 newPassword 一致 |

返回：

```json
{ "code": 200, "msg": "操作成功" }
```

| 字段 | 说明 |
|---|---|
| code | 200 成功 |
| msg | 失败原因：原密码错误、两次密码不一致、新密码不能与原密码相同、长度不符 |

改密后当前 token 仍有效，不必强制重新登录。

### 7. 邀请信息

`GET /app/invite`（需登录）

```json
{
  "data": {
    "inviteCode": "5839201",
    "inviteCount": 2,
    "reward": 2,
    "inviteAmount": 2,
    "inviteCurrency": "CNY",
    "kycRewardCny": 14,
    "kycRewardUsdt": 2,
    "teamRateL1": 9,
    "teamRateL2": 3,
    "teamRateL3": 1,
    "lockParent": true,
    "ruleText": "用户注册与推广奖励规则：\n一、实名注册奖励\n..."
  }
}
```

| 字段 | 说明 |
|---|---|
| inviteCode | 我的 7 位邀请码，给别人注册用 |
| inviteCount | 直推人数 |
| reward | 邀请奖励整数金额；规则关闭或金额为 0 时返回 `0`（兼容旧字段） |
| inviteAmount / inviteCurrency | 每成功邀请 1 名实名用户发给邀请人的金额和币种 |
| kycRewardCny / kycRewardUsdt | 实名后本人可领的两种金额，任选其一 |
| teamRateL1 / L2 / L3 | 充值三级返佣百分比 |
| lockParent | 注册绑定后不可改上级 |
| ruleText | 后台配置的规则全文，可直接展示 |

### 7.1 注册推广规则与实名奖励领取

`GET /app/promo`（别名 `GET /app/registerReward`，**可不登录**）

未登录只返回规则。已登录额外返回能否领取实名奖励。

```json
{
  "data": {
    "enabled": true,
    "kycSelfEnabled": true,
    "kycRewardCny": 14,
    "kycRewardUsdt": 2,
    "inviteEnabled": true,
    "inviteAmount": 2,
    "inviteCurrency": "CNY",
    "lockParent": true,
    "teamEnabled": true,
    "teamRateL1": 9,
    "teamRateL2": 3,
    "teamRateL3": 1,
    "ruleText": "...",
    "kycStatus": "1",
    "kycRewardClaimed": false,
    "kycRewardClaimable": true
  }
}
```

| 字段 | 说明 |
|---|---|
| enabled | 总开关。关闭后实名自领和邀请奖励都不发 |
| kycSelfEnabled | 实名注册奖励开关 |
| kycRewardCny / kycRewardUsdt | 实名后可选的两种金额 |
| inviteEnabled | 邀请奖励开关 |
| teamEnabled | 充值三级返佣开关 |
| kycStatus | 已登录才有：`0` 未实名 `1` 已实名 |
| kycRewardClaimed | 是否已领过实名注册奖励 |
| kycRewardClaimable | 当前是否可领（已实名且未领且开关打开） |
| claimedCurrency / claimedAmount | 已领时返回当时选择的币种和金额 |

弹窗金额与能否领取：

`GET /app/kyc/reward`（别名 `GET /app/kycReward`，也可继续用 `GET /app/promo`）

已登录看 `kycRewardClaimable`。弹窗左侧用 `kycRewardCny`，右侧用 `kycRewardUsdt`。

领取实名注册奖励（需登录，实名后调用，选一种币种入账）：

`POST /app/kyc/reward`（别名 `POST /app/kycReward` 、 `POST /app/promo/kycReward` 、 `POST /app/registerReward`）

```json
{ "currency": "CNY" }
```

`currency` 只能是 `CNY` 或 `USDT`，每人只能成功一次。

```json
{ "code": 200, "msg": "领取成功，已到账", "data": { "currency": "CNY", "amount": 14 } }
```

失败 `msg`：请先完成实名认证、已领取实名注册奖励、只能选择人民币或USDT、实名注册奖励暂未开放、USDT暂未开放。

邀请奖励在被邀请人 `POST /app/kyc` 成功后自动入账邀请人钱包，无需再调接口。历史已实名用户不补发邀请奖励，但仍可自领实名注册奖励（尚未领过时）。

### 8. 团队

`GET /app/team`（需登录）

返回 1～7 级下线名单和汇总。激活人数 = 已实名。成员金额是该人**已通过的充值**；认购金额是订单金额。

```json
{
  "data": {
    "summary": {
      "level1": {
        "register": 2,
        "active": 1,
        "subscribeUsd": 100,
        "subscribeUsdt": 100,
        "subscribeCny": 700,
        "rechargeUsd": 50,
        "rechargeUsdt": 50,
        "rechargeCny": 200
      },
      "level2": {},
      "level3": {},
      "level4": {},
      "level5": {},
      "level6": {},
      "level7": {}
    },
    "members": {
      "1": [
        {
          "memberId": 10002,
          "name": "李四",
          "realName": "李四",
          "phone": "13800000002",
          "kycStatus": "1",
          "usd": 50,
          "usdt": 50,
          "rechargeUsdt": 50,
          "cny": 200,
          "rechargeCny": 200,
          "subscribeUsdt": 100,
          "subscribeCny": 700,
          "teamLevel": 1
        }
      ],
      "2": []
    },
    "level1": [],
    "level2": [],
    "level3": [],
    "level4": [],
    "level5": [],
    "level6": [],
    "level7": []
  }
}
```

`members["1"]`～`members["7"]`、`level1`～`level7` 内容相同，方便 App 兼容。`usd`/`usdt`/`rechargeUsdt` 同义，`cny`/`rechargeCny` 同义。

### 9. 会员等级 / 成长激励金

`GET /app/levels`

返回**全部**等级配置（含停用），方便 App 画七档表。升级匹配仍只用 `status=0` 的档。

文案在后台 **业务管理 → 等级奖励** 改：

- `ruleText`：右上角「规则说明」点开后的正文
- `hint` / `note`：表格上方灰色「注」（两个字段内容相同）

```json
{
  "data": {
    "current": { "memberId": 10001, "levelName": "V0" },
    "ruleText": "启航、探索、开拓、星耀、领航、星域：达成条件后系统自动发放1次成长激励金。...",
    "hint": "注：成员个人累计认购金额达到 ¥10,000 或 1,429 USDT 后，方可计入团队等级考核。请遵循平台规则，严禁作弊行为，一经发现将取消奖励资格。",
    "note": "注：成员个人累计认购金额达到 ¥10,000 或 1,429 USDT 后，方可计入团队等级考核。请遵循平台规则，严禁作弊行为，一经发现将取消奖励资格。",
    "levels": [
      {
        "levelId": 1,
        "levelName": "启航",
        "minValidMembers": 1,
        "minRechargeCny": 0,
        "minRechargeUsdt": 0,
        "minTeamPerfCny": 0,
        "minTeamPerfUsdt": 0,
        "rewardEnabled": "1",
        "rewardCycle": "ONCE",
        "rewardMode": "AUTO",
        "rewardCny": 100,
        "rewardUsdt": 10
      }
    ]
  }
}
```

团队奖励只使用 `rewardCny` / `rewardUsdt`（表 `biz_level.reward_cny` / `reward_usdt`），不再提供 `teamRewardCny` 别名。

等级匹配（全部启用中的等级取 sort 最高的一档）：有效成员人数、本人累计充值 CNY/USDT、团队业绩 CNY/USDT。某项阈值填 0 表示不限制。

有效成员、团队业绩口径、是否含本人，都在后台「等级奖励」里配。

成长激励金发放：

- `ONCE` + `AUTO`：达标自动入账 1 次（启航/探索/开拓/星耀/领航/星域，前6级）
- `PERMANENT` + `MANUAL` + `UNLIMITED`：仅第7级星链。达标后生成待发放，客服在「等级奖励发放」确认入账；之后可「额外发放」
- 团队业绩同时有人民币和 USDT 时，按全局「混合业绩发放币种」发放（默认 USDT）

预置的启航～星链默认是**停用**，配好人数/业绩/金额后再改成正常，避免未配金额就升级。

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

### 11. 产品系列（分类）

App 产品是两层：**Tab 渲染系列卡片 → 点进去查该系列下的产品**。后端表是产品分类 `biz_product_category`，App 接口用系列这个名字。

`GET /app/product/series`

需登录。返回上架系列，给 Tab 用。

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "seriesId": 1,
      "categoryId": 1,
      "seriesName": "「星帆·天启计划」",
      "categoryName": "「星帆·天启计划」",
      "coverUrl": "",
      "sort": 1
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| seriesId / categoryId | 同一个值，点进去查产品时带这个 |
| seriesName / categoryName | 卡片标题 |
| coverUrl | 封面，空则 App 用本地默认图 |

`GET /app/product/series/{seriesId}` 系列详情（页头标题、封面）。不存在或已隐藏返回 500。

### 11.1 产品列表

`GET /app/products`  
`GET /app/products?seriesId=1`（`categoryId` 也可以）

不带参数返回全部上架产品；带 `seriesId` 只返回该系列下产品。

```json
{
  "data": [
    {
      "productId": 1,
      "productName": "提现指定产品",
      "nameEn": "",
      "seriesId": 1,
      "categoryId": 1,
      "seriesName": "「星帆·天启计划」",
      "categoryName": "「星帆·天启计划」",
      "priceCny": 100,
      "dailyRebateCny": 5,
      "priceUsdt": 14.3,
      "dailyRebateUsdt": 0.72,
      "supportsCny": true,
      "supportsUsdt": true,
      "durationDays": 30,
      "withdrawRequired": "1",
      "coverUrl": "",
      "status": "0"
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| priceCny / dailyRebateCny | 人民币认购价和日返，大于 0 才支持人民币下单 |
| priceUsdt / dailyRebateUsdt | USDT 认购价和日返，大于 0 才支持 USDT 下单 |
| supportsCny / supportsUsdt | 是否支持该币种按钮 |
| price / dailyRebate / currency | 兼容旧字段，优先等于人民币价 |

`withdrawRequired = 1` 表示认购该币种指定产品后，才允许提现对应币种（看订单当时选的币）。

### 12. 认购产品

`POST /app/orders`

```json
{ "productId": 1, "currency": "CNY", "payPassword": "123456" }
```

不传或传错 `payPassword` 不会扣款、不会下单。

| 字段 | 必填 | 说明 |
|---|---|---|
| productId | 是 | 产品 ID |
| payPassword | 是 | 支付/交易密码。别名 `tradePassword` / `payPwd` |
| currency | 建议填 | `CNY` 或 `USDT`。不传：有人民币价走人民币，否则走 USDT |
| amount | 否 | **忽略**，以产品配置的对应币种价格为准 |

`CNY` 扣人民币钱包、日返人民币；`USDT` 扣 USDT 钱包、日返 USDT。产品没配该币种价格会 500。每天 00:05 按订单币种打 `dailyRebate`，打满 `durationDays` 天结束。

`GET /app/orders?pageNum=1&pageSize=10` 我的订单。  
订单 `status`：`0` 持仓中，`1` 已完成。订单带 `currency` 字段，以及所属产品系列：

```json
{
  "rows": [
    {
      "orderId": 3,
      "orderNo": "...",
      "productId": 1,
      "productName": "天启一号",
      "seriesId": 1,
      "categoryId": 1,
      "seriesName": "「星帆·天启计划」",
      "categoryName": "「星帆·天启计划」",
      "seriesCoverUrl": "http://host/profile/xxx.png",
      "currency": "CNY",
      "price": 100,
      "dailyRebate": 5,
      "durationDays": 30,
      "remainingDays": 29,
      "status": "0"
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| seriesId / categoryId | 产品所属系列，和产品列表同一套 |
| seriesName / categoryName | 系列名称 |
| seriesCoverUrl | 系列封面，空字符串表示没有图 |

产品改了所属系列后，历史订单按**当前产品挂的系列**返回。

### 13. 钱包

`GET /app/wallet`

给个人中心资产卡用：余额、产品收益分 CNY / USDT 两行。充值、提现仍走原接口。

```json
{
  "data": {
    "cnyAvailable": 202,
    "cnyFrozen": 0,
    "cnyProductIncome": 12.5,
    "cnyAssistValue": 0,
    "usdtAvailable": 0,
    "usdtFrozen": 0,
    "usdtProductIncome": 0,
    "usdtAssistValue": 0,
    "cny": { "currency": "CNY", "available": 202, "frozen": 0, "productIncome": 12.5, "assistValue": 0 },
    "usdt": { "currency": "USDT", "available": 0, "frozen": 0, "productIncome": 0, "assistValue": 0 },
    "wallets": [
      { "currency": "CNY", "available": 202, "frozen": 0, "productIncome": 12.5, "assistValue": 0 },
      { "currency": "USDT", "available": 0, "frozen": 0, "productIncome": 0, "assistValue": 0 }
    ]
  }
}
```

| 字段 | 说明 |
|---|---|
| available | 可用余额 |
| frozen | 冻结金额（提现审核中） |
| productIncome | 该币种累计产品日返（持仓订单每天打入的 `dailyRebate` 合计） |
| assistValue | 推广收益累计（签到、实名奖励、邀请、分佣、等级奖励）。JSON 字段名不变，App 推广收益列直接展示该值 |

CNY / USDT 独立账户，不能互转。充值、认购、返利、提现按币种单独结算：用人民币买产品就返人民币、提人民币；用 USDT 买产品就返 U、提 U。签到奖励仍走 CNY。

### 13.1 资金明细 / 充值余额交易

`GET /app/walletLog?pageNum=1&pageSize=10`

给资金明细「充值余额」Tab 用，返回**可用余额变动**流水（认购扣款、充值入账、签到、日返、推广奖金、提现冻结、提现退回等）。只记可用余额变化，提现打款成功只扣冻结的那条不出现。

别名：`GET /app/wallet/logs` 、 `GET /app/funds`。可选 `currency=CNY|USDT`、`bizType`（别名 `type`）。

按类型筛选示例：

- 全部：`GET /app/walletLog?pageNum=1&pageSize=10`
- 充值记录：`GET /app/walletLog?bizType=RECHARGE`
- 提现记录：`GET /app/walletLog?bizType=WITHDRAW`（含 `WITHDRAW_FREEZE` / `WITHDRAW_SUCCESS` / `WITHDRAW_REJECT`）
- 推广收益：`GET /app/walletLog?bizType=PROMO`（签到、实名奖励、邀请、分佣、等级奖励）
- 产品收益：`GET /app/walletLog?bizType=PRODUCT`（产品日返 `REBATE`）
- 多个类型：`GET /app/walletLog?bizType=RECHARGE,SUBSCRIBE`

`bizType=WITHDRAW` 是分组查询；`bizType=PROMO` / `ASSIST` / `推广收益` 查推广收益（签到、实名奖励、邀请、分佣、等级奖励）；`bizType=PRODUCT` / `产品收益` 查产品日返。精确值如 `SUBSCRIBE`、`CHECKIN` 仍按原值过滤。

```json
{
  "total": 4,
  "rows": [
    {
      "logId": 12,
      "id": 12,
      "title": "曙光一号",
      "name": "曙光一号",
      "bizType": "SUBSCRIBE",
      "bizTypeLabel": "认购",
      "typeLabel": "认购",
      "amount": -350,
      "currency": "CNY",
      "direction": "OUT",
      "date": "2026-08-19",
      "createTime": "2026-08-19 12:00:00",
      "remark": "认购产品:曙光一号"
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| title / name | 列表左侧标题。认购用产品名，充值=充值，分佣=推广奖金，日返=系统 |
| amount | 收入为正、支出为负 |
| direction | `IN` / `OUT` |
| date | `yyyy-MM-dd`，可直接展示 |

`bizType` 筛选：`RECHARGE` 充值记录，`WITHDRAW` 提现记录（分组）。精确值：`SUBSCRIBE` 认购、`RECHARGE` 充值、`COMMISSION` 推广奖金、`INVITE` 推广奖励、`KYC_REWARD` 实名注册奖励、`CHECKIN` 签到、`REBATE` 产品日返、`LEVEL_REWARD` 等级奖励、`ADJUST` 后台调账、`WITHDRAW_FREEZE` 提现、`WITHDRAW_SUCCESS` 提现成功、`WITHDRAW_REJECT` 提现退回。

### 13.2 充值/提现申请记录（含全部状态）

`GET /app/fundRecords?pageNum=1&pageSize=10`

查的是**申请单**，不是余额流水。待审、通过/已打款、拒绝都会返回。

别名：`GET /app/fund/records` 、 `GET /app/bills`。

| 参数 | 说明 |
|---|---|
| type / bizType | `RECHARGE` 充值申请，`WITHDRAW` 提现申请。不传则两类都返回 |
| currency | 可选 `CNY` / `USDT` |
| status | 可选。不传=全部。`0` 待处理，`1` 成功，`2` 拒绝 |

```
GET /app/fundRecords?bizType=RECHARGE
GET /app/fundRecords?bizType=WITHDRAW
GET /app/fundRecords?bizType=RECHARGE&status=0
```

```json
{
  "total": 2,
  "rows": [
    {
      "id": 12,
      "bizType": "RECHARGE",
      "bizTypeLabel": "充值",
      "title": "充值待审",
      "amount": 300,
      "currency": "CNY",
      "status": "0",
      "statusLabel": "待审",
      "createTime": "2026-08-26 21:00:00"
    }
  ]
}
```

充值 `statusLabel`：待审 / 已通过 / 已拒绝。提现：待打款 / 已打款 / 已拒绝。

---

### 14.1 收款账户（钱包管理）

给 App「钱包管理 / 添加 USDT、银行卡、支付宝」用，**不是**余额钱包。需登录。别名：`/app/wallet/accounts`。

`GET /app/payAccounts?type=USDT` 我的账户。`type` 可选 `USDT` / `BANK` / `ALIPAY`。

```json
{
  "data": [
    {
      "accountId": 1,
      "accountType": "USDT",
      "accountName": "张三",
      "accountNo": "Txxxx",
      "bankName": "",
      "network": "TRC20",
      "isDefault": "1",
      "status": "0"
    }
  ]
}
```

`POST /app/payAccounts` 新增或保存（带 `accountId` 则改）：

```json
{ "accountType": "BANK", "accountName": "张三", "accountNo": "6222...", "bankName": "中国银行", "isDefault": "1" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| accountType | 是 | `USDT` / `BANK` / `ALIPAY` |
| accountNo | 是 | USDT 地址 / 卡号 / 支付宝账号 |
| accountName | 否 | 户名 |
| bankName | 银行卡必填 | 开户行 |
| network | USDT 可空 | 默认 `TRC20`，也可 `ERC20` |
| isDefault | 否 | `1` 默认。该类型第一张自动默认 |
| accountId | 改时 | 修改已有账户 |

每类最多 5 个。`PUT /app/payAccounts/{id}` 修改，`DELETE /app/payAccounts/{id}` 删除。


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

### 15. 提现申请（后台审核打款）

`POST /app/withdraw`

会员提交后**不会立刻到账**。对应币种可用余额先冻结，等后台在「业务管理 → 提现审核」确认已线下打款后，才扣掉冻结；拒绝则解冻退回。

App 提现不需要绑定或填写谷歌验证码，`googleCode` 可省略。

```json
{ "currency": "CNY", "amount": 105, "accountInfo": "支付宝账号", "remark": "支付宝" }
```

```json
{ "currency": "USDT", "amount": 105, "accountId": 12, "remark": "USDT" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| currency | 是 | `CNY` 走支付宝，`USDT` 走链上地址 |
| amount | 是 | 不低于最低提现 |
| accountId | 否 | 已保存的收款账户 ID，有则自动填 `accountInfo` |
| accountInfo | accountId 为空时必填 | CNY 填支付宝/银行卡，USDT 填钱包地址 |
| remark | 否 | 可写收款方式说明 |
| googleCode | 否 | 忽略，App 提现不校验谷歌验证 |

规则：
- 人民币最低/最高提现：后台「提现审核」页配置，最高 0 表示不限
- USDT 最低/最高提现：同上
- 产品收益提现：必须已认购该币种且 `withdrawRequired=1` 的产品
- 推广收益提现：App 会把备注写成「推广收益-…」。未购买过任何产品时拒绝，提示请先购买产品后再提现推广收益
- 申请时冻结对应币种余额

`GET /app/withdraw?pageNum=1&pageSize=10` 我的提现单。

| status | statusLabel |
|---|---|
| 0 | 待打款 |
| 1 | 已打款 |
| 2 | 已拒绝 |

另返回 `payMethod`（`ALIPAY` / `USDT`）、`payMethodLabel`、`accountInfo`、`payProofUrl`（打款凭证，未打款为空）。

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

### 17.1 首页视频轮播

后台 **业务管理 → 视频轮播**。App 首页顶部轮播用这条接口，不需要登录。

`GET /app/video`（兼容别名 `GET /app/carousel`）

```json
{
  "code": 200,
  "data": [
    {
      "carouselId": 1,
      "title": "首页主视觉",
      "videoUrl": "http://host/profile/upload/xxx.mp4",
      "coverUrl": "http://host/profile/upload/xxx.png",
      "sort": 1
    }
  ]
}
```

| 字段 | 说明 |
|---|---|
| videoUrl | 视频完整 URL，mp4 |
| coverUrl | 封面，空字符串表示没有封面 |
| sort | 越小越靠前 |

`data` 为空数组时，App 继续用本地默认图。初始化脚本：`sql/biz_carousel_patch.sql`

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

### 20.1 客服中心

后台 **业务管理 → 客服中心**。给 App「客服中心」和登录页「联系客服」用，和官方群聊不是同一套。

**不需要登录。**

`GET /app/service`（兼容别名 `GET /app/customer-service`）

```json
{
  "data": {
    "title": "客服中心",
    "workTime": "09:00 - 21:00",
    "hint": "通道拥堵可联系在线客服",
    "channels": [
      {
        "channelId": 1,
        "name": "微信客服",
        "type": "WECHAT",
        "value": "",
        "qrUrl": "https://example.com/qr.png",
        "linkUrl": "",
        "sort": 1
      }
    ]
  }
}
```

| 字段 | 说明 |
|---|---|
| title / workTime / hint | 文案，后台可改 |
| type | `PHONE` / `WECHAT` / `TELEGRAM` / `QQ` / `LINK` / `QR` |
| value | 手机号、微信号、账号 |
| qrUrl | 二维码完整 URL，可空 |
| linkUrl | 点击跳转，可空 |

隐藏的渠道不返回。初始化脚本：`sql/biz_app_support_patch.sql`

### 21. 新闻资讯

后台在 **业务管理 → 新闻资讯** 新增。给 App 底部「新闻」Tab 用，和首页「公告」不是同一套。

不需要登录。

`GET /app/news` 列表（不含正文）：

```json
{
  "code": 200,
  "data": [
    {
      "newsId": 1,
      "title": "俄罗斯近24小时遥感卫星观测任务与行业应用动态",
      "summary": "俄罗斯近24小时遥感卫星观测任务与行业应用动态",
      "coverUrl": "",
      "publishDate": "2026-08-18",
      "sort": 1
    }
  ]
}
```

`GET /app/news/{newsId}` 详情，多返回 `content`（已去掉 HTML，可直接展示）。隐藏或不存在时失败。

| 字段 | 说明 |
|---|---|
| title | 标题 |
| summary | 列表摘要 |
| coverUrl | 封面完整 URL，空字符串表示用 App 本地默认图 |
| publishDate | `yyyy-MM-dd` |
| content | 仅详情有，纯文本 |

初始化脚本：`sql/biz_news_patch.sql`


---

## 二、建议联调顺序

1. 注册 A（不填邀请码）
2. 注册 B，`inviteCode = A` 的 7 位邀请码
3. 注册 C，`inviteCode = B` 的 7 位邀请码
4. C 实名 → B 到账 2 元推广奖励
5. C 调 `POST /app/promo/kycReward` `{ "currency": "CNY" }` → C 到账 14 元（或选 USDT 到账 2）
6. C 申请充值 300
7. 后台「充值审核」通过 → B 到账 27，A 到账 9
8. C 认购产品 1（扣 100）
9. C 签到（+2）
10. C 申请提现 105 → 后台「提现审核」通过
11. 未认购指定产品时提现会被拒绝

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
| GET | `/biz/member/team/{memberId}?teamLevel=1` | 某会员的 1～7 级或全部下线 |
| GET/POST/PUT | `/system/notice` | 通知公告（系统管理菜单，类型选「公告」会展示到 App） |
| GET/POST/PUT | `/biz/productCategory` | 产品分类（App 系列）列表/新增/修改 |
| GET | `/biz/productCategory/options` | 分类下拉 |
| GET/POST/PUT | `/biz/product` | 产品列表/新增/修改，产品挂 `categoryId` |
| GET | `/biz/product/{productId}` | 产品详情 |
| DELETE | `/biz/product/{ids}` | 删除产品 |
| GET | `/biz/order/list` | 认购订单 |
| GET | `/biz/checkin/list` | 签到记录 |
| GET/PUT | `/biz/checkin/rule` | 签到规则（金额、连续天数、奖品、概率） |
| GET/PUT | `/biz/promo/rule` | 注册推广规则（实名奖励、邀请奖励、三级返佣、规则说明） |
| GET | `/biz/promo/grant/list` | 实名自领 / 推广奖励发放记录 |
| GET | `/biz/checkin/prize/list` | 签到中奖记录 |
| GET | `/biz/recharge/list` | 充值列表 |
| POST | `/biz/recharge` | 后台代提充值单 `{memberId, currency, amount, remark}` |
| PUT | `/biz/recharge/audit` | 审核 `{id, status, auditRemark}`，`status`：`1` 通过 `2` 拒绝 |
| GET | `/biz/withdraw/list` | 提现列表 |
| PUT | `/biz/withdraw/audit` | 提现打款 `{id, status, auditRemark, payProofUrl}`。`status`：`1` 确认已打款（扣冻结）`2` 拒绝（解冻）。拒绝必须填 `auditRemark`；`payProofUrl` 为打款截图，建议确认打款时上传 |
| GET | `/biz/walletLog/list` | 资金流水 |
| GET | `/biz/team/list` | 团队关系（会员列表视角） |
| GET/POST/PUT/DELETE | `/biz/level` | 会员等级配置 |
| GET/PUT | `/biz/levelReward/rule` | 成长激励金全局规则 |
| GET | `/biz/levelReward/level/list` | 各等级奖励配置列表 |
| PUT | `/biz/levelReward/level` | 保存某等级条件与奖励 |
| POST | `/biz/levelReward/evaluate` | 立即核算全部会员 |
| GET | `/biz/levelReward/grant/list` | 发放记录 |
| PUT | `/biz/levelReward/grant/pay/{grantId}` | 确认发放入账 |
| PUT | `/biz/levelReward/grant/reject/{grantId}` | 拒绝发放 |
| POST | `/biz/levelReward/grant/extraPay` | 永久档额外发放 `{memberId, levelId, remark}` |
| GET/POST/PUT/DELETE | `/biz/overview` | App 运行概览（手改展示数字） |
| GET/POST/PUT/DELETE | `/biz/about` | App 关于我们（手改展示内容） |
| GET/POST/PUT/DELETE | `/biz/group` | App 官方群聊（上传二维码） |
| GET/POST/PUT/DELETE | `/biz/news` | App 新闻资讯 |
| GET/POST/PUT/DELETE | `/biz/carousel` | App 首页视频轮播 |
| GET | `/biz/payAccount/list` | 会员收款账户，查询：phone、accountType、status |
| GET/POST/PUT/DELETE | `/biz/payAccount` | 代会员新增/改/删 USDT、银行卡、支付宝账户 |
| GET/PUT | `/biz/service/config` | 客服中心标题、工作时间、提示文案 |
| GET/POST/PUT/DELETE | `/biz/service` | 客服渠道（二维码/微信号/电话等） |
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
| LEVEL_REWARD | 等级奖励（成长激励金） |
| KYC_REWARD | 实名注册奖励（用户自领） |
| INVITE | 实名推广奖励（邀请人） |

---

## 四、运营配置（到对应功能页，不要改参数设置里的 biz.*）

参数设置只保留系统项。业务键仍存在 `sys_config`，但后台列表会隐藏 `biz.*`。

| 功能页 | 可改内容 |
|---|---|
| 资金中心 → 提现审核 | 人民币/USDT 最低、最高提现（最高 0=不限），USDT 充提开关 |
| 会员中心 → 会员管理 | App 谷歌验证开关、验证器名称 |
| 会员中心 → 注册推广规则 | 实名自领、邀请奖励、三级返佣比例与规则文案 |
| 会员中心 → 实名认证奖励 | 实名自领金额（与注册推广同步） |
| 会员中心 → 等级奖励 | 成长激励金开关、业绩口径、各等级金额 |
| 产品交易 → 签到规则 | 每日签到金额、连续签到抽奖两档 |
| 运营内容 → 客服中心 | 标题、工作时间、提示文案 |
| 系统管理 → 参数设置 | 皮肤、验证码、初始密码、后台谷歌验证等 sys.* |

每日返利任务：定时任务里「产品每日返利」，`dailyRebateTask.execute()`，cron `0 5 0 * * ?`。  
成长激励金任务：定时任务里「等级奖励核算」，`levelRewardTask.execute()`，cron `0 15 0 * * ?`。也可在后台「等级奖励」点立即核算。

初始化脚本：`RuoYi-Vue-springboot2/sql/biz_init.sql`  
注册推广规则补丁：`RuoYi-Vue-springboot2/sql/biz_promo_rule_patch.sql`

