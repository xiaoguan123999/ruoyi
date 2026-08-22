import { StyleSheet, Text, View } from 'react-native';

import { formatBalance } from '@/api/app-auth';
import { colors } from '@/theme/colors';

type Props = {
  cny?: number | null;
  usdt?: number | null;
  /** 整块在父容器中居中（我的页余额列） */
  centered?: boolean;
};

export function DualBalance({ cny, usdt, centered = false }: Props) {
  return (
    <View style={[styles.list, centered && styles.listCentered]}>
      <BalanceLine icon="¥" value={formatBalance(cny)} />
      <BalanceLine icon="USDT" value={formatBalance(usdt)} />
    </View>
  );
}

function BalanceLine({ icon, value }: { icon: string; value: string }) {
  return (
    <View style={styles.row}>
      <Text style={styles.icon} numberOfLines={1}>
        {icon}
      </Text>
      <Text style={styles.value} numberOfLines={1}>
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  list: {
    gap: 4,
  },
  listCentered: {
    alignItems: 'center',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 26,
  },
  icon: {
    width: 44,
    color: colors.text,
    fontSize: 15,
    fontWeight: '700',
    textAlign: 'center',
    marginRight: 6,
  },
  value: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '800',
    lineHeight: 26,
    minWidth: 20,
  },
});
