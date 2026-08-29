import { Image } from 'expo-image';
import { useFocusEffect, useLocalSearchParams, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import { ActivityIndicator, Modal, Pressable, StyleSheet, Text, View } from 'react-native';

import {
  deleteAppPayAccount,
  fetchAppPayAccounts,
  formatPayAccountLabel,
} from '@/api/app-pay-account';
import { ApiError } from '@/api/request';
import type { AppPayAccount, AppPayAccountType } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { PageHeader } from '@/components/ui/PageHeader';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { images } from '@/constants/images';
import { colors } from '@/theme/colors';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

type WalletTab = 'usdt' | 'bank' | 'alipay';

const TABS: { key: WalletTab; label: string; type: AppPayAccountType }[] = [
  { key: 'usdt', label: 'USDT钱包', type: 'USDT' },
  { key: 'bank', label: '银行卡', type: 'BANK' },
  { key: 'alipay', label: '支付宝', type: 'ALIPAY' },
];

const TAB_META: Record<
  WalletTab,
  {
    addLabel: string;
    hint: string | null;
    limitMsg: string;
    addRoute: '/wallet/add-usdt' | '/wallet/add-bank' | '/wallet/add-alipay';
    type: AppPayAccountType;
  }
> = {
  usdt: {
    addLabel: '添加虚拟币账户',
    hint: '*最多添加1个虚拟账户',
    limitMsg: '最多添加1个虚拟账户',
    addRoute: '/wallet/add-usdt',
    type: 'USDT',
  },
  bank: {
    addLabel: '添加银行卡',
    hint: '*最多添加1个银行账户',
    limitMsg: '最多添加1个银行账户',
    addRoute: '/wallet/add-bank',
    type: 'BANK',
  },
  alipay: {
    addLabel: '添加支付宝',
    hint: null,
    limitMsg: '最多添加1个支付宝账户',
    addRoute: '/wallet/add-alipay',
    type: 'ALIPAY',
  },
};

const CARD_STYLE: Record<
  AppPayAccountType,
  { bg: string; icon: number; title: (a: AppPayAccount) => string }
> = {
  USDT: {
    bg: '#3D9B8F',
    icon: images.payUsdt,
    title: () => 'USDT',
  },
  BANK: {
    bg: '#E07068',
    icon: images.payCard,
    title: () => '银行储蓄卡',
  },
  ALIPAY: {
    bg: '#4A9BE0',
    icon: images.payAlipay,
    title: (a) => a.accountName || '支付宝',
  },
};

function parseTab(raw?: string | string[]): WalletTab {
  const value = Array.isArray(raw) ? raw[0] : raw;
  if (value === 'bank' || value === 'alipay' || value === 'usdt') {
    return value;
  }
  return 'usdt';
}

/** 与设计稿一致的脱敏：**** **** **** 尾号 / 手机号中间打码 */
function maskAccountNo(account: AppPayAccount): string {
  const no = (account.accountNo || '').replace(/\s/g, '');
  if (!no) return '—';

  if (account.accountType === 'USDT') {
    const tail = no.slice(-4);
    return `**** **** **** ${tail}`;
  }

  if (account.accountType === 'BANK') {
    const tail = no.slice(-4);
    return `**** **** **** ${tail}`;
  }

  // 支付宝：手机号 133****8888；邮箱 / 其它账号中间打码
  if (/^\d{11}$/.test(no)) {
    return `${no.slice(0, 3)}****${no.slice(-4)}`;
  }
  if (no.includes('@') && no.length > 6) {
    const [name, domain] = no.split('@');
    return `${name.slice(0, 2)}****@${domain}`;
  }
  if (no.length <= 4) return no;
  return `${no.slice(0, 3)}****${no.slice(-4)}`;
}

export default function WalletManageScreen() {
  const router = useRouter();
  const { tab } = useLocalSearchParams<{ tab?: string }>();
  const [activeTab, setActiveTab] = useState<WalletTab>(() => parseTab(tab));
  const [loading, setLoading] = useState(true);
  const [accounts, setAccounts] = useState<AppPayAccount[]>([]);
  const [pendingDelete, setPendingDelete] = useState<AppPayAccount | null>(null);
  const [deleting, setDeleting] = useState(false);
  const meta = TAB_META[activeTab];

  const load = useCallback(async () => {
    try {
      const list = await fetchAppPayAccounts(TAB_META[activeTab].type);
      setAccounts(list);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '获取收款账户失败');
      }
      setAccounts([]);
    } finally {
      setLoading(false);
    }
  }, [activeTab]);

  useFocusEffect(
    useCallback(() => {
      setLoading(true);
      void load();
    }, [load]),
  );

  const onAdd = () => {
    if (accounts.length >= 1) {
      modalWarning(meta.limitMsg);
      return;
    }
    router.push(meta.addRoute);
  };

  const onDelete = (account: AppPayAccount) => {
    if (deleting) {
      return;
    }
    setPendingDelete(account);
  };

  const closeDeleteModal = () => {
    if (!deleting) {
      setPendingDelete(null);
    }
  };

  const confirmDelete = async () => {
    if (!pendingDelete || deleting) {
      return;
    }
    setDeleting(true);
    try {
      const msg = await deleteAppPayAccount(pendingDelete.accountId);
      setPendingDelete(null);
      modalSuccess(msg);
      await load();
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '删除失败');
      }
    } finally {
      setDeleting(false);
    }
  };

  return (
    <AppBackground>
      <PageHeader title="钱包管理" />
      <View style={styles.tabs}>
        {TABS.map((item) => {
          const active = item.key === activeTab;
          return (
            <Pressable
              key={item.key}
              onPress={() => {
                setActiveTab(item.key);
                setLoading(true);
              }}
              style={styles.tabItem}
            >
              <Text style={[styles.tabText, active && styles.tabTextActive]}>{item.label}</Text>
              {active ? <View style={styles.tabLine} /> : <View style={styles.tabLinePlaceholder} />}
            </Pressable>
          );
        })}
      </View>

      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={styles.content}
        onRefresh={load}
      >
        {loading ? (
          <View style={styles.loading}>
            <ActivityIndicator color={colors.accent} />
          </View>
        ) : (
          accounts.slice(0, 1).map((item) => {
            const skin = CARD_STYLE[item.accountType];
            return (
              <View key={item.accountId} style={[styles.card, { backgroundColor: skin.bg }]}>
                <View style={styles.cardHead}>
                  <Image source={skin.icon} style={styles.cardIcon} contentFit="contain" />
                  <Text style={styles.cardTitle} numberOfLines={1}>
                    {skin.title(item)}
                  </Text>
                  <Pressable disabled={deleting} onPress={() => onDelete(item)} hitSlop={10}>
                    <Text style={styles.deleteText}>删除</Text>
                  </Pressable>
                </View>
                <Text style={styles.cardNo}>{maskAccountNo(item)}</Text>
              </View>
            );
          })
        )}

        <Pressable onPress={onAdd} style={styles.addBtn}>
          <Text style={styles.addPlus}>+</Text>
          <Text style={styles.addText}>{meta.addLabel}</Text>
        </Pressable>
        {meta.hint ? <Text style={styles.hint}>{meta.hint}</Text> : null}
      </RefreshableScrollView>

      <Modal
        visible={pendingDelete != null}
        transparent
        animationType="fade"
        onRequestClose={closeDeleteModal}
      >
        <Pressable style={styles.modalMask} onPress={closeDeleteModal}>
          <Pressable style={styles.modalCard} onPress={() => {}}>
            <Text style={styles.modalTitle}>删除账户</Text>
            <Text style={styles.modalText}>
              {pendingDelete
                ? `确认删除「${formatPayAccountLabel(pendingDelete)}」？`
                : ''}
            </Text>
            <View style={styles.modalActions}>
              <Pressable
                disabled={deleting}
                onPress={closeDeleteModal}
                style={[styles.modalBtn, styles.modalBtnGhost]}
              >
                <Text style={styles.modalBtnGhostText}>取消</Text>
              </Pressable>
              <Pressable
                disabled={deleting}
                onPress={() => {
                  void confirmDelete();
                }}
                style={[styles.modalBtn, styles.modalBtnDanger]}
              >
                <Text style={styles.modalBtnDangerText}>{deleting ? '删除中…' : '删除'}</Text>
              </Pressable>
            </View>
          </Pressable>
        </Pressable>
      </Modal>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tabs: {
    flexDirection: 'row',
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 10,
  },
  tabText: {
    color: 'rgba(220, 230, 245, 0.55)',
    fontSize: 15,
  },
  tabTextActive: {
    color: colors.text,
    fontWeight: '700',
  },
  tabLine: {
    marginTop: 8,
    width: 36,
    height: 3,
    borderRadius: 2,
    backgroundColor: '#E85A5A',
  },
  tabLinePlaceholder: {
    marginTop: 8,
    height: 3,
  },
  content: {
    paddingHorizontal: 18,
    paddingTop: 14,
    paddingBottom: 32,
  },
  loading: {
    paddingVertical: 28,
    alignItems: 'center',
  },
  card: {
    borderRadius: 14,
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 20,
    marginBottom: 16,
    minHeight: 108,
  },
  cardHead: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  cardIcon: {
    width: 28,
    height: 28,
    borderRadius: 14,
  },
  cardTitle: {
    flex: 1,
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
  deleteText: {
    color: 'rgba(255, 255, 255, 0.95)',
    fontSize: 14,
    fontWeight: '500',
  },
  cardNo: {
    marginTop: 22,
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: '600',
    letterSpacing: 0.5,
  },
  addBtn: {
    height: 52,
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth * 2,
    borderColor: 'rgba(180, 205, 235, 0.45)',
    backgroundColor: 'rgba(6, 16, 36, 0.55)',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  addPlus: {
    color: 'rgba(230, 238, 250, 0.95)',
    fontSize: 22,
    fontWeight: '400',
    marginTop: -1,
  },
  addText: {
    color: 'rgba(230, 238, 250, 0.95)',
    fontSize: 15,
    fontWeight: '500',
  },
  hint: {
    marginTop: 14,
    textAlign: 'center',
    color: 'rgba(175, 195, 225, 0.72)',
    fontSize: 12,
  },
  modalMask: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  modalCard: {
    borderRadius: 14,
    backgroundColor: '#0E172A',
    borderWidth: 1,
    borderColor: 'rgba(98, 150, 220, 0.28)',
    paddingHorizontal: 18,
    paddingTop: 18,
    paddingBottom: 16,
  },
  modalTitle: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
    textAlign: 'center',
  },
  modalText: {
    marginTop: 12,
    color: 'rgba(220, 232, 255, 0.88)',
    fontSize: 14,
    lineHeight: 22,
    textAlign: 'center',
  },
  modalActions: {
    marginTop: 20,
    flexDirection: 'row',
    gap: 10,
  },
  modalBtn: {
    flex: 1,
    height: 42,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalBtnGhost: {
    borderWidth: 1,
    borderColor: 'rgba(180, 205, 235, 0.35)',
  },
  modalBtnGhostText: {
    color: 'rgba(230, 238, 250, 0.92)',
    fontSize: 15,
    fontWeight: '600',
  },
  modalBtnDanger: {
    backgroundColor: '#E85A5A',
  },
  modalBtnDangerText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
});
