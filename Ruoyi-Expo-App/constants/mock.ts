import type { ImageSource } from 'expo-image';

import { images } from '@/constants/images';

export type ProductItem = {
  id: string;
  name: string;
  enName: string;
  amount: number;
  amountCny: number;
  daily: number;
  dailyCny: number;
  cycle: string;
  termDays: number;
  tag: string;
  desc: string;
  cover: ImageSource;
  titleTone?: 'blue' | 'purple';
  payoutMethod?: string;
  currencies?: string;
  riskLevel?: string;
};

export type ProductSeries = {
  id: string;
  name: string;
  cover: ImageSource;
  items: ProductItem[];
};

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
    summary: '俄罗斯近24小时遥感卫星观测任务与行业应用动态',
    date: '2026-08-18',
    cover: images.newsCover,
    body: `一、在轨遥感星座整体运行工况平稳\n（一）高分辨率光学卫星完成农情、地质重点区域成像。\n（二）雷达卫星持续开展全天候云雨覆盖区域观测。\n\n二、行业应用动态\n面向应急、农业、交通等场景的数据产品按计划分发，支撑多地业务系统稳定运行。`,
  },
  {
    id: '2',
    title: '商业航天星座组网加速，行业应用场景持续拓展',
    summary: '商业航天星座组网加速，行业应用场景持续拓展',
    date: '2026-08-12',
    cover: images.newsCover,
    body: `商业航天正从单星验证走向规模组网。星帆智联持续推进星座部署与地面终端协同，为行业用户提供稳定连接能力。`,
  },
];

const PRODUCT_META = {
  termDays: 1825,
  payoutMethod: '每日回报（次日发放）',
  currencies: 'USDT / RMB',
  riskLevel: 'R2 中低风险',
} as const;

export const mockProductSeries: ProductSeries[] = [
  {
    id: 'tianqi',
    name: '「星帆·天启计划」',
    cover: images.productIntro,
    items: [
      {
        id: 'dawn-1',
        name: '曙光一号',
        enName: 'EARLY LIGHT • 01',
        amount: 50,
        amountCny: 350,
        daily: 0.72,
        dailyCny: 5,
        cycle: '2年',
        termDays: PRODUCT_META.termDays,
        tag: '商业航天参与计划',
        desc: '启航计划 • 探索商业航天的第一步',
        cover: images.product1,
        payoutMethod: PRODUCT_META.payoutMethod,
        currencies: PRODUCT_META.currencies,
        riskLevel: PRODUCT_META.riskLevel,
      },
      {
        id: 'startrace-2',
        name: '星轨二号',
        enName: 'STARTRACE • 02',
        amount: 200,
        amountCny: 1400,
        daily: 3.6,
        dailyCny: 25,
        cycle: '2年',
        termDays: PRODUCT_META.termDays,
        tag: '商业航天参与计划',
        desc: '进阶计划 • 连接星网生态，共享发展红利',
        cover: images.product2,
        payoutMethod: PRODUCT_META.payoutMethod,
        currencies: PRODUCT_META.currencies,
        riskLevel: PRODUCT_META.riskLevel,
      },
      {
        id: 'skyvault-3',
        name: '天穹三号',
        enName: 'SKYDOME • 03',
        amount: 600,
        amountCny: 4200,
        daily: 11.5,
        dailyCny: 80,
        cycle: '2年',
        termDays: PRODUCT_META.termDays,
        tag: '商业航天参与计划',
        desc: '领航计划 • 共建航天未来，领航星辰大海',
        cover: images.product3,
        titleTone: 'purple',
        payoutMethod: PRODUCT_META.payoutMethod,
        currencies: PRODUCT_META.currencies,
        riskLevel: PRODUCT_META.riskLevel,
      },
    ],
  },
  {
    id: 'yuanzheng',
    name: '「星帆·远征计划」',
    cover: images.productHero,
    items: [
      {
        id: 'orbit-1',
        name: '极轨一号',
        enName: 'POLAR ORBIT • 01',
        amount: 100,
        amountCny: 700,
        daily: 1.6,
        dailyCny: 11,
        cycle: '2年',
        termDays: PRODUCT_META.termDays,
        tag: '商业航天参与计划',
        desc: '拓展计划 • 覆盖极地航迹，延伸星座边界',
        cover: images.product2,
        payoutMethod: PRODUCT_META.payoutMethod,
        currencies: PRODUCT_META.currencies,
        riskLevel: PRODUCT_META.riskLevel,
      },
      {
        id: 'horizon-2',
        name: '地平二号',
        enName: 'HORIZON • 02',
        amount: 360,
        amountCny: 2520,
        daily: 6.8,
        dailyCny: 48,
        cycle: '2年',
        termDays: PRODUCT_META.termDays,
        tag: '商业航天参与计划',
        desc: '远征计划 • 跨域协同组网，共享星座红利',
        cover: images.product3,
        titleTone: 'purple',
        payoutMethod: PRODUCT_META.payoutMethod,
        currencies: PRODUCT_META.currencies,
        riskLevel: PRODUCT_META.riskLevel,
      },
    ],
  },
];

export function getProductSeries(id?: string): ProductSeries | undefined {
  if (!id) {
    return mockProductSeries[0];
  }
  return mockProductSeries.find((s) => s.id === id) ?? mockProductSeries[0];
}

export function getProductItem(id?: string): { series: ProductSeries; item: ProductItem } | undefined {
  if (!id) {
    return undefined;
  }
  for (const series of mockProductSeries) {
    const item = series.items.find((p) => p.id === id);
    if (item) {
      return { series, item };
    }
  }
  return undefined;
}

export const mockProducts: ProductItem[] = mockProductSeries.flatMap((s) => s.items);

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

export type SubscribeRecord = {
  id: string;
  plan: string;
  product: string;
  amount: string;
  activateLabel: string;
  status: '进行中' | '已到期';
  time: string;
};

export const mockRecords: SubscribeRecord[] = [
  {
    id: 's1',
    plan: '「星帆·天启计划」',
    product: '曙光计划',
    amount: '¥350',
    activateLabel: '暂未激活',
    status: '进行中',
    time: '2026-08-19 12：54',
  },
  {
    id: 's2',
    plan: '「星帆·天启计划」',
    product: '曙光计划',
    amount: '¥350',
    activateLabel: '已激活',
    status: '已到期',
    time: '2026-08-19 13：54',
  },
];

export const mockFunds = [
  { id: 'f1', title: '充值', amount: '+1000', time: '2026-08-18 10:21', type: 'in' as const },
  { id: 'f2', title: '认购曙光一号', amount: '-50', time: '2026-08-01 12:21', type: 'out' as const },
  { id: 'f3', title: '每日收益', amount: '+0.72', time: '2026-08-19 00:01', type: 'in' as const },
];
