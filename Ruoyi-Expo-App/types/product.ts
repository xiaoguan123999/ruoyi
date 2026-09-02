import type { ImageSource } from 'expo-image';

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
  apiId?: number;
  onSaleFlag?: boolean;
  unlockRuleText?: string;
};

export type ProductSeries = {
  id: string;
  name: string;
  cover: ImageSource;
  items: ProductItem[];
};
