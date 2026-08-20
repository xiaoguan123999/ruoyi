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
  inviteCode?: string;
  kycStatus?: string;
  levelId?: number;
  levelName?: string;
  usdtAvailable?: number;
  cnyAvailable?: number;
  cnyFrozen?: number;
  teamCount?: number;
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
  amount: number;
  currency?: string;
  productId?: number;
  accountInfo?: string;
  remark?: string;
};

export type AppProduct = {
  productId: number;
  productName: string;
  price: number;
  currency?: string;
  dailyRebate?: number;
  durationDays?: number;
  remark?: string;
  status?: string;
  sort?: number;
  withdrawRequired?: string;
};

export type AppWallet = {
  usdtAvailable: number;
  cnyAvailable: number;
  cnyFrozen: number;
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
