/** 会员等级页 UI 占位数据（接口未返回时使用） */
export type LevelUiRow = {
  levelName: string;
  teamDepth: string;
  minRechargeCny: number;
  minRechargeUsdt: number;
  teamRewardCny: number;
  teamRewardUsdt: number;
};

export const LEVEL_UI_ROWS: LevelUiRow[] = [
  {
    levelName: '启航',
    teamDepth: '一级内',
    minRechargeCny: 28000,
    minRechargeUsdt: 4000,
    teamRewardCny: 1100,
    teamRewardUsdt: 158,
  },
  {
    levelName: '探索',
    teamDepth: '二级内',
    minRechargeCny: 87000,
    minRechargeUsdt: 12429,
    teamRewardCny: 3500,
    teamRewardUsdt: 500,
  },
  {
    levelName: '开拓',
    teamDepth: '三级内',
    minRechargeCny: 420000,
    minRechargeUsdt: 60000,
    teamRewardCny: 17000,
    teamRewardUsdt: 2429,
  },
  {
    levelName: '星耀',
    teamDepth: '四级内',
    minRechargeCny: 1120000,
    minRechargeUsdt: 160000,
    teamRewardCny: 50000,
    teamRewardUsdt: 7143,
  },
  {
    levelName: '领航',
    teamDepth: '五级内',
    minRechargeCny: 3500000,
    minRechargeUsdt: 500000,
    teamRewardCny: 140000,
    teamRewardUsdt: 20000,
  },
  {
    levelName: '星域',
    teamDepth: '六级内',
    minRechargeCny: 8400000,
    minRechargeUsdt: 1200000,
    teamRewardCny: 336000,
    teamRewardUsdt: 48000,
  },
  {
    levelName: '星链',
    teamDepth: '七级内',
    minRechargeCny: 20000000,
    minRechargeUsdt: 2857143,
    teamRewardCny: 800000,
    teamRewardUsdt: 114286,
  },
];

export const LEVEL_UI_STATUS = {
  levelName: '星域',
  teamRechargeCny: 10000000,
  teamRechargeUsdt: 10000000,
};
