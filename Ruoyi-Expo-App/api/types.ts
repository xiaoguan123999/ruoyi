export type AjaxResult<T = unknown> = {
  code: number;
  msg: string;
  data?: T;
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
};

export type AppTeamLevelStats = {
  register: number;
  active: number;
  subscribeUsd: number;
  subscribeCny: number;
  rechargeUsd: number;
  rechargeCny: number;
};

export type AppTeamSummary = {
  level1: AppTeamLevelStats;
  level2: AppTeamLevelStats;
  level3: AppTeamLevelStats;
};

export type AppTeamMemberItem = {
  memberId?: number;
  name: string;
  phone: string;
  usd: number;
  cny: number;
};

export type AppTeamMembersByLevel = Record<1 | 2 | 3, AppTeamMemberItem[]>;

export type AppTeamView = {
  summary: AppTeamSummary;
  members: AppTeamMembersByLevel;
};

export type AppAmountBody = {
  amount?: number;
  currency?: string;
  productId?: number;
  accountInfo?: string;
  remark?: string;
  googleCode?: string;
};

export type AppSubscribeBody = {
  productId: number;
  currency: 'CNY' | 'USDT';
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
  sort?: number;
  status?: string;
  remark?: string;
};

export type AppInviteInfo = {
  inviteCode: string;
  inviteUrl?: string;
  qrCode?: string;
  inviteCount?: number;
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
