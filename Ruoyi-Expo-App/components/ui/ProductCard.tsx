import { ProductCardClassic } from '@/components/ui/product-cards/ProductCardClassic';
import { ProductCardCompact } from '@/components/ui/product-cards/ProductCardCompact';
import { ProductCardHero } from '@/components/ui/product-cards/ProductCardHero';
import { ProductCardNumbered } from '@/components/ui/product-cards/ProductCardNumbered';
import { ProductCardRow } from '@/components/ui/product-cards/ProductCardRow';
import { ProductCardSplit } from '@/components/ui/product-cards/ProductCardSplit';
import type { ProductItem } from '@/types/product';

export type { ProductItem };

type Props = {
  item: ProductItem;
  onPress?: () => void;
  /** SPLIT 等双币直购：按币种回调 */
  onSubscribe?: (currency: 'CNY' | 'USDT') => void;
};

/** 按 layoutType 分发；未知类型降级 CLASSIC */
export function ProductCard({ item, onPress, onSubscribe }: Props) {
  const layout = String(item.layoutType || 'CLASSIC').toUpperCase();
  switch (layout) {
    case 'HERO':
      return <ProductCardHero item={item} onPress={onPress} />;
    case 'SPLIT':
      return <ProductCardSplit item={item} onPress={onPress} onSubscribe={onSubscribe} />;
    case 'NUMBERED':
      return <ProductCardNumbered item={item} onPress={onPress} />;
    case 'ROW':
      return <ProductCardRow item={item} onPress={onPress} />;
    case 'COMPACT':
      return <ProductCardCompact item={item} onPress={onPress} />;
    case 'BANNER':
    case 'PRICE_FOCUS':
    case 'MEDIA_LEFT':
    case 'CLASSIC':
    default:
      return <ProductCardClassic item={item} onPress={onPress} />;
  }
}
