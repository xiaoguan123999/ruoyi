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
{ "phone": "13800000001", "password": "123456", "inviteCode": "10001", "code": "3", "uuid": "验证码uuid" }
```

| 字段 | 必填 | 说明 |
|---|---|---|
| phone | 是 | 手机号，唯一 |
| password | 是 | 密码 |
| inviteCode | 否 | 上级邀请码，等于上级 `memberId` |
| code | 是 | 验证码答案 |
| uuid | 是 | `/app/auth/captcha` 返回的 uuid |

返回：

```json
{
  "code": 200,
  "msg": "操作成功",
  "token": "eyJ...",
  "memberId": 10001,
  "inviteCode": "10001"
}
```

邀请码就是会员数字 ID。

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

返回字段同注册。验证码错误或过期会返回 `code=500`。

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
    "inviteCode": "10001",
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
    "inviteCode": "10001",
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

一天一次，成功入账 CNY 2 元（参数 `biz.checkin.amount` 可改）。

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

---

## 二、建议联调顺序

1. 注册 A（不填邀请码）
2. 注册 B，`inviteCode = A.memberId`
3. 注册 C，`inviteCode = B.memberId`
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
| GET | `/biz/member/team/{memberId}?teamLevel=1` | 某会员的 1/2/3 级或全部下线 |
| GET/POST/PUT | `/biz/product` | 产品列表/新增/修改 |
| GET | `/biz/product/{productId}` | 产品详情 |
| DELETE | `/biz/product/{ids}` | 删除产品 |
| GET | `/biz/order/list` | 认购订单 |
| GET | `/biz/checkin/list` | 签到记录 |
| GET | `/biz/recharge/list` | 充值列表 |
| POST | `/biz/recharge` | 后台代提充值单 `{memberId, currency, amount, remark}` |
| PUT | `/biz/recharge/audit` | 审核 `{id, status, auditRemark}`，`status`：`1` 通过 `2` 拒绝 |
| GET | `/biz/withdraw/list` | 提现列表 |
| PUT | `/biz/withdraw/audit` | 提现审核，body 同上 |
| GET | `/biz/walletLog/list` | 资金流水 |
| GET | `/biz/team/list` | 团队关系（会员列表视角） |
| GET/POST/PUT/DELETE | `/biz/level` | 会员等级配置 |
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
| biz.checkin.amount | 2 | 每日签到金额（CNY） |
| biz.withdraw.minAmount | 105 | 人民币最低提现 |
| biz.withdraw.minAmount.usdt | 105 | USDT 最低提现 |
| biz.team.rate.l1 / l2 / l3 | 9 / 3 / 1 | 充值分佣百分比（同币种） |
| biz.invite.reward | 0 | 邀请奖励，暂未发放 |
| biz.usdt.enabled | true | USDT 开关 |

每日返利任务：定时任务里「产品每日返利」，`dailyRebateTask.execute()`，cron `0 5 0 * * ?`。

初始化脚本：`RuoYi-Vue-springboot2/sql/biz_init.sql`
