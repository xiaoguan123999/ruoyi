/** 页面模拟数据 / API 对接状态（供 MockDataBanner 引用） */
export type MockPageEntry = {
  route: string;
  title: string;
  mode: 'full' | 'partial';
  note: string;
};

export const MOCK_PAGES: MockPageEntry[] = [
  {
    route: '/service',
    title: '客服中心',
    mode: 'full',
    note: '联系客服功能未对接',
  },
];
