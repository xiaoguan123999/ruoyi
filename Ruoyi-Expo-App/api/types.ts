export type AjaxResult<T = unknown> = {
  code: number;
  msg: string;
  data?: T;
  token?: string;
  img?: string;
  /** App 验证码明文（/app/auth/captcha） */
  text?: string;
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
  realName?: string;
  idCard?: string;
  avatar?: string;
  phone?: string;
  inviteCode?: string;
  kycStatus?: string;
  levelId?: number;
  levelName?: string;
  usdtAvailable?: number;
  cnyAvailable?: number;
  cnyFrozen?: number;
  teamCount?: number;
  cnyProductIncome?: number;
  usdtProductIncome?: number;
  cnyAssistValue?: number;
  usdtAssistValue?: number;
  /** 兼容旧单值，等同 cnyProductIncome */
  productIncome?: number;
  /** 兼容旧单值，等同 cnyAssistValue */
  assistValue?: number;
  status?: string;
  /** 是否已设置支付密码 */
  hasPayPassword?: boolean;
};

export type LoginBody = {
  username: string;
  password: string;
  code?: string;
  uuid?: string;
};

export type AppLoginBody = {
  phone: string;
  password: string;
  code: string;
  uuid: string;
};

export type AppRegisterBody = {
  phone: string;
  password: string;
  code: string;
  uuid: string;
  inviteCode?: string;
  /** 交易密码 */
  payPassword?: string;
};

export type AppMember = {
  memberId: number;
  phone: string;
  realName?: string;
  idCard?: string;
  inviteCode?: string;
  kycStatus?: string;
  levelId?: number;
  levelName?: string;
  usdtAvailable?: number;
  cnyAvailable?: number;
  cnyFrozen?: number;
  teamCount?: number;
  cnyProductIncome?: number;
  usdtProductIncome?: number;
  cnyAssistValue?: number;
  usdtAssistValue?: number;
  productIncome?: number;
  assistValue?: number;
  status?: string;
  /** 是否已设置支付密码 */
  hasPayPassword?: boolean;
};

export type AppTeamLevelStats = {
  register: number;
  active: number;
  subscribeUsd: number;
  subscribeCny: number;
  rechargeUsd: number;
  rechargeCny: number;
};

export type AppTeamLevelNo = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export type AppTeamSummary = Record<`level${AppTeamLevelNo}`, AppTeamLevelStats>;

export type AppTeamMemberItem = {
  memberId?: number;
  name: string;
  phone: string;
  usd: number;
  cny: number;
};

export type AppTeamMembersByLevel = Record<AppTeamLevelNo, AppTeamMemberItem[]>;

export type AppTeamView = {
  summary: AppTeamSummary;
  members: AppTeamMembersByLevel;
};

export type AppAmountBody = {
  amount?: number;
  currency?: string;
  productId?: number;
  /** 已保存的收款账户ID，提现时可传 */
  accountId?: number;
  accountInfo?: string;
  remark?: string;
  googleCode?: string;
};

/** 收款账户类型 */
export type AppPayAccountType = 'USDT' | 'BANK' | 'ALIPAY';

/** 会员收款账户 */
export type AppPayAccount = {
  accountId: number;
  accountType: AppPayAccountType;
  /** 户名（银行卡/支付宝） */
  accountName?: string;
  /** 卡号 / 支付宝账号 / USDT 地址 */
  accountNo: string;
  bankName?: string;
  /** USDT 网络，如 TRC20 / ERC20 */
  network?: string;
  /** 是否默认 1是 */
  isDefault?: string;
  status?: string;
  phone?: string;
  remark?: string;
};

export type AppPayAccountBody = {
  accountType: AppPayAccountType;
  accountName?: string;
  accountNo: string;
  bankName?: string;
  network?: string;
  isDefault?: string;
  remark?: string;
};

export type AppSubscribeBody = {
  productId: number;
  currency: 'CNY' | 'USDT';
  /** 交易密码 */
  payPassword: string;
};

export type AppProduct = {
  productId: number;
  productName: string;
  nameEn?: string;
  /** 兼容旧字段 */
  price?: number;
  priceCny?: number;
  priceUsdt?: number;
  currency?: string;
  dailyRebate?: number;
  dailyRebateCny?: number;
  dailyRebateUsdt?: number;
  durationDays?: number;
  remark?: string;
  status?: string;
  sort?: number;
  withdrawRequired?: string;
  seriesId?: number;
  categoryId?: number;
  categoryName?: string;
  coverUrl?: string;
};

/** GET /app/product/series */
export type AppProductSeries = {
  seriesId: number;
  seriesName: string;
  coverUrl?: string;
  sort?: number;
};

export type AppWallet = {
  usdtAvailable: number;
  cnyAvailable: number;
  cnyFrozen: number;
  cnyProductIncome: number;
  usdtProductIncome: number;
  cnyAssistValue: number;
  usdtAssistValue: number;
};

export type AppCheckinRecord = {
  checkinId?: number;
  checkinDate: string;
  amount?: number;
};

export type AppCheckinPrizeRule = {
  days: number;
  name: string;
  rate: number;
  enabled: boolean;
};

export type AppCheckinRule = {
  amount: number;
  oncePerDay: boolean;
  prizes: AppCheckinPrizeRule[];
};

/** GET /app/checkin/info */
export type AppCheckinInfo = {
  checkinId?: number;
  checkinDate?: string;
  amount: number;
  currency: string;
  streakDays: number;
  checkedToday: boolean;
  rule: AppCheckinRule;
  prizeDrawn?: boolean;
  prizeWon?: boolean;
  prizeName?: string;
  prizeDays?: number;
};

export type AppOrderRecord = {
  orderId: number;
  productId?: number;
  productName: string;
  planName?: string;
  amount: number;
  currency: string;
  status: string;
  statusLabel: '进行中' | '已到期' | string;
  activateLabel: string;
  createTime: string;
};

export type AppFundRecord = {
  id: string;
  title: string;
  amount: number;
  currency: string;
  status: string;
  createTime: string;
};

/** 充值余额 Tab 用的钱包流水 */
export type AppWalletLogItem = {
  id: string;
  title: string;
  amount: number;
  currency: string;
  createTime: string;
};

export type AppKycBody = {
  realName: string;
  idCard: string;
};

export type AppPasswordBody = {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export type AppLevel = {
  levelId: number;
  levelName: string;
  minRechargeCny: number;
  minRechargeUsdt: number;
  minValidMembers: number;
  /** 团队要求文案，如「一级内」 */
  teamDepth?: string;
  /** 最低团队业绩 */
  minTeamPerfCny?: number;
  minTeamPerfUsdt?: number;
  teamRewardCny?: number;
  teamRewardUsdt?: number;
  sort?: number;
  status?: string;
  remark?: string;
};

export type AppLevelCurrent = {
  levelId?: number;
  levelName?: string;
};

export type AppLevelsView = {
  current: AppLevelCurrent;
  levels: AppLevel[];
  /** 表格上方注释；接口 hint / note 同值 */
  hint?: string;
  /** 规则说明弹窗文案 */
  ruleText?: string;
};

export type AppInviteInfo = {
  inviteCode: string;
  inviteUrl?: string;
  qrCode?: string;
  inviteCount?: number;
  /** 邀请规则说明全文 */
  ruleText?: string;
};

export type AppVideoCarouselItem = {
  id: string;
  /** 标题（可能为空） */
  title?: string;
  /** 视频播放地址（可能为空，当前首页做封面预览即可） */
  videoUrl?: string;
  /** 轮播封面/海报图地址 */
  coverUrl?: string;
  /** 排序，越小越靠前 */
  sort: number;
};

export type AppNotice = {
  id: string;
  title: string;
  createTime: string;
};

export type AppNoticeDetail = AppNotice & {
  content: string;
};

export type AppOverviewItem = {
  id: string;
  itemKey: string;
  title: string;
  displayValue: string;
  statusText: string;
  statusColor: string;
  /** 后台配置的远程图；为空时用本地兜底图 */
  imageUrl?: string;
  imageFallback: number;
  sort: number;
};

export type AppAboutItem = {
  id: string;
  title: string;
  subtitle: string;
  content: string;
  imageUrl?: string;
  sort: number;
};

export type AppGroupChatItem = {
  id: string;
  title: string;
  hint: string;
  /** 二维码图片地址，或可编码进二维码的链接 */
  qrUrl?: string;
  remark?: string;
  sort: number;
};

export type AppNewsItem = {
  id: string;
  title: string;
  summary: string;
  coverUrl?: string;
  publishDate: string;
  sort: number;
};

export type AppNewsDetail = AppNewsItem & {
  content: string;
};
