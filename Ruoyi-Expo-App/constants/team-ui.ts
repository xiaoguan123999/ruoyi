/** 我的团队页 UI 占位数据（接口未返回时使用） */
export type TeamUiLevelRow = {
  register: number;
  active: number;
  rechargeCny: number;
  rechargeUsd: number;
  subscribeCny: number;
  subscribeUsd: number;
};

export type TeamUiMember = {
  name: string;
  phone: string;
  usd: number;
  cny: number;
};

export const TEAM_LEVEL_LABELS = ['一', '二', '三', '四', '五', '六', '七'] as const;

export const TEAM_TAB_LABELS = ['一级', '二级', '三级', '四级', '五级', '六级', '七级'] as const;

export const TEAM_UI_SUMMARY = {
  register: 1000,
  active: 1000,
  recharge: 10000000,
  subscribe: 10000000,
};

const LEVEL_ROW: TeamUiLevelRow = {
  register: 100,
  active: 100,
  rechargeCny: 10000,
  rechargeUsd: 1000,
  subscribeCny: 10000,
  subscribeUsd: 1000,
};

export const TEAM_UI_LEVEL_ROWS: TeamUiLevelRow[] = Array.from({ length: 7 }, () => ({
  ...LEVEL_ROW,
}));

export const TEAM_UI_MEMBERS: Record<number, TeamUiMember[]> = {
  1: [
    { name: '张三', phone: '157****4242', usd: 0, cny: 1000 },
    { name: '李四', phone: '157****4242', usd: 10000, cny: 1000000 },
  ],
};

export type TeamLevelNo = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export const TEAM_LEVELS: TeamLevelNo[] = [1, 2, 3, 4, 5, 6, 7];
