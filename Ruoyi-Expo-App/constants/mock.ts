import { images } from '@/constants/images';

export const mockUser = {
  userId: 1,
  userName: '13300008888',
  nickName: '张三',
  avatar: '',
  phone: '133****8888',
  verified: true,
  balanceCny: 1000,
  balanceUsdt: 1000,
  productIncome: 1000,
  assistValue: 1000,
  inviteCode: '000111',
};

export const mockNews = [
  {
    id: '1',
    title: '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
    summary: '近24小时内，在轨遥感星座整体运行工况平稳，高分辨率光学卫星完成农情、地质重点区域成像。',
    date: '2026-08-18',
    cover: images.productHero,
    body: `一、在轨遥感星座整体运行工况平稳\n（一）高分辨率光学卫星完成农情、地质重点区域成像。\n（二）雷达卫星持续开展全天候云雨覆盖区域观测。\n\n二、行业应用动态\n面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。`,
  },
  {
    id: '2',
    title: '商业航天星座组网加速，行业应用场景持续拓展',
    summary: '新一批卫星完成在轨测试，通信与遥感融合应用进入规模化验证阶段。',
    date: '2026-08-12',
    cover: images.product2,
    body: `商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。`,
  },
];

export const mockProducts = [
  {
    id: 'dawn-1',
    name: '曙光一号',
    enName: 'EARLY LIGHT • 01',
    amount: 50,
    daily: 0.72,
    cycle: '2年',
    tag: '商业航天参与计划',
    desc: '启航计划 • 探索商业航天的第一步',
    cover: images.product1,
  },
  {
    id: 'startrace-2',
    name: '星轨二号',
    enName: 'STARTRACE • 01',
    amount: 200,
    daily: 3.6,
    cycle: '2年',
    tag: '商业航天参与计划',
    desc: '进阶计划 • 连接星网生态，共享发展红利',
    cover: images.product2,
  },
  {
    id: 'skyvault-3',
    name: '天穹三号',
    enName: 'SKY VAULT • 03',
    amount: 800,
    daily: 16,
    cycle: '3年',
    tag: '商业航天参与计划',
    desc: '旗舰计划 • 深度参与星座运营',
    cover: images.product3,
  },
];

export const mockTeamSummary = {
  level1: { register: 100, active: 100, subscribeUsd: 100, subscribeCny: 100, rechargeUsd: 100, rechargeCny: 100 },
  level2: { register: 100, active: 100, subscribeUsd: 100, subscribeCny: 100, rechargeUsd: 100, rechargeCny: 100 },
  level3: { register: 100, active: 100, subscribeUsd: 100, subscribeCny: 100, rechargeUsd: 100, rechargeCny: 100 },
};

export const mockTeamMembers: Record<1 | 2 | 3, { name: string; phone: string; usd: number; cny: number }[]> = {
  1: [
    { name: '张三', phone: '157****4242', usd: 0, cny: 1000 },
    { name: '李四', phone: '198****2523', usd: 10000, cny: 1000000 },
  ],
  2: [{ name: '王五', phone: '139****1100', usd: 200, cny: 1400 }],
  3: [{ name: '赵六', phone: '186****9088', usd: 50, cny: 360 }],
};

export const mockRecords = [
  { id: 's1', name: '曙光一号', amount: 50, time: '2026-08-01 12:21', status: '收益中' },
  { id: 's2', name: '星轨二号', amount: 200, time: '2026-07-18 09:08', status: '收益中' },
];

export const mockFunds = [
  { id: 'f1', title: '充值', amount: '+1000', time: '2026-08-18 10:21', type: 'in' as const },
  { id: 'f2', title: '认购曙光一号', amount: '-50', time: '2026-08-01 12:21', type: 'out' as const },
  { id: 'f3', title: '每日收益', amount: '+0.72', time: '2026-08-19 00:01', type: 'in' as const },
];
