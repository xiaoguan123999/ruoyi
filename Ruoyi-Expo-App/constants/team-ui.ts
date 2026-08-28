/** 我的团队页：层级文案与展示类型（数据走 API） */
export type TeamUiLevelRow = {
  register: number;
  active: number;
  rechargeCny: number;
  rechargeUsd: number;
  subscribeCny: number;
  subscribeUsd: number;
};

export const TEAM_LEVEL_LABELS = ['一', '二', '三', '四', '五', '六', '七'] as const;

export const TEAM_TAB_LABELS = ['一级', '二级', '三级', '四级', '五级', '六级', '七级'] as const;

export type TeamLevelNo = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export const TEAM_LEVELS: TeamLevelNo[] = [1, 2, 3, 4, 5, 6, 7];
