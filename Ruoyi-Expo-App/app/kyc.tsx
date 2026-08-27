import { useFocusEffect } from 'expo-router';
import { useCallback, useRef, useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { fetchAppProfile, isKycVerified, maskIdCard } from '@/api/app-auth';
import {
  claimAppKycReward,
  fetchAppKycReward,
  formatKycRewardLabel,
  submitAppKyc,
} from '@/api/app-member';
import { ApiError } from '@/api/request';
import type { AppKycRewardInfo, KycRewardCurrency, RuoyiUser } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { KycRewardModal } from '@/components/ui/KycRewardModal';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { useAuth } from '@/hooks/useAuth';
import { colors } from '@/theme/colors';
import { validateIdCard } from '@/utils/id-card';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

const emptyReward = (): AppKycRewardInfo => ({
  kycRewardCny: 0,
  kycRewardUsdt: 0,
  kycRewardClaimable: false,
  kycRewardClaimed: false,
});

function applyProfileToForm(
  profile: RuoyiUser,
  setName: (value: string) => void,
  setIdNo: (value: string) => void,
) {
  if (!isKycVerified(profile.kycStatus)) {
    return;
  }
  if (profile.realName) {
    setName(profile.realName);
  }
  if (profile.idCard) {
    setIdNo(maskIdCard(profile.idCard));
  }
}

export default function KycScreen() {
  const { user } = useAuth();
  const userRef = useRef(user);
  userRef.current = user;

  const verified = isKycVerified(user?.kycStatus);
  const [name, setName] = useState(user?.realName ?? '');
  const [idNo, setIdNo] = useState(verified && user?.idCard ? maskIdCard(user.idCard) : '');
  const [submitting, setSubmitting] = useState(false);
  const [reward, setReward] = useState<AppKycRewardInfo>(emptyReward());
  const [rewardVisible, setRewardVisible] = useState(false);
  const [rewardSubmitting, setRewardSubmitting] = useState(false);

  const applyRewardState = useCallback(async (profile: RuoyiUser | null | undefined, autoShow: boolean) => {
    if (!isKycVerified(profile?.kycStatus)) {
      setReward(emptyReward());
      setRewardVisible(false);
      return;
    }
    try {
      const info = await fetchAppKycReward();
      setReward(info);
      if (info.kycRewardClaimable && autoShow) {
        setRewardVisible(true);
      } else {
        setRewardVisible(false);
      }
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        // 奖励接口失败不阻断实名页
      }
      setReward(emptyReward());
      setRewardVisible(false);
    }
  }, []);

  const load = useCallback(async () => {
    try {
      const profile = await fetchAppProfile();
      applyProfileToForm(profile, setName, setIdNo);
      await applyRewardState(profile, true);
    } catch {
      await applyRewardState(userRef.current, true);
    }
  }, [applyRewardState]);

  useFocusEffect(
    useCallback(() => {
      let cancelled = false;
      void (async () => {
        try {
          const profile = await fetchAppProfile();
          if (cancelled) {
            return;
          }
          applyProfileToForm(profile, setName, setIdNo);
          await applyRewardState(profile, true);
        } catch {
          if (cancelled) {
            return;
          }
          await applyRewardState(userRef.current, true);
        }
      })();
      return () => {
        cancelled = true;
      };
    }, [applyRewardState]),
  );

  const onSubmit = async () => {
    if (verified) {
      modalWarning('您已完成实名认证');
      return;
    }
    if (!name.trim() || !idNo.trim()) {
      modalWarning('请填写姓名和身份证号码');
      return;
    }
    if (!validateIdCard(idNo)) {
      modalWarning('请输入正确的身份证号码');
      return;
    }
    setSubmitting(true);
    try {
      const message = await submitAppKyc({ realName: name, idCard: idNo });
      const profile = await fetchAppProfile().catch(() => null);
      if (profile) {
        applyProfileToForm(profile, setName, setIdNo);
      }
      modalSuccess(message);
      await applyRewardState(profile ?? userRef.current, true);
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '实名认证失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const onClaimReward = async (currency: KycRewardCurrency) => {
    setRewardSubmitting(true);
    try {
      const result = await claimAppKycReward(currency);
      setReward((prev) => ({
        ...prev,
        kycRewardClaimable: false,
        kycRewardClaimed: true,
        claimedCurrency: result.currency,
        claimedAmount: result.amount,
      }));
      setRewardVisible(false);
      modalSuccess(result.message || '领取成功，已到账');
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '领取失败');
      }
    } finally {
      setRewardSubmitting(false);
    }
  };

  const claimedLabel =
    reward.kycRewardClaimed && reward.claimedCurrency && reward.claimedAmount != null
      ? formatKycRewardLabel(reward.claimedCurrency, reward.claimedAmount)
      : '';

  return (
    <AppBackground>
      <PageHeader title="实名认证" showBack={!rewardVisible} />
      <RefreshableScrollView
        style={{ flex: 1 }}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 28 }}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
        onRefresh={load}
      >
        <GlassCard>
          <Text style={styles.tip}>
            {verified ? '您已完成实名认证' : '请使用真实有效身份证信息认证'}
          </Text>
          <Text style={styles.section}>实名认证信息</Text>
          <TextInput
            value={name}
            onChangeText={setName}
            placeholder="请输入姓名"
            placeholderTextColor={colors.placeholder}
            style={[styles.input, verified && styles.inputDisabled]}
            editable={!verified}
          />
          <TextInput
            value={idNo}
            onChangeText={setIdNo}
            placeholder={verified ? '已认证' : '请输入身份证号码'}
            placeholderTextColor={colors.placeholder}
            style={[styles.input, verified && styles.inputDisabled]}
            editable={!verified}
            autoCapitalize="characters"
          />
          {verified && claimedLabel ? (
            <Text style={styles.rewardTip}>已领取实名奖励：{claimedLabel}</Text>
          ) : null}
          {verified && reward.kycRewardClaimable ? (
            <Text
              style={styles.rewardTipPending}
              onPress={() => setRewardVisible(true)}
            >
              实名奖励待领取，点击选择
            </Text>
          ) : null}
        </GlassCard>
        <View style={{ marginTop: 16 }}>
          <PrimaryButton
            title={verified ? '已实名认证' : '立即实名'}
            onPress={() => void onSubmit()}
            disabled={submitting || verified}
          />
        </View>
      </RefreshableScrollView>

      <KycRewardModal
        visible={rewardVisible}
        submitting={rewardSubmitting}
        rewardCny={reward.kycRewardCny}
        rewardUsdt={reward.kycRewardUsdt}
        onClose={() => setRewardVisible(false)}
        onConfirm={(currency) => void onClaimReward(currency)}
      />
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  tip: { color: colors.text, marginBottom: 12 },
  section: { color: colors.muted, marginBottom: 10 },
  input: {
    backgroundColor: 'rgba(142,175,210,0.35)',
    borderRadius: 10,
    color: colors.text,
    paddingHorizontal: 12,
    paddingVertical: 12,
    marginBottom: 10,
  },
  inputDisabled: {
    opacity: 0.7,
  },
  rewardTip: {
    marginTop: 4,
    color: '#7EB6FF',
    fontSize: 13,
  },
  rewardTipPending: {
    marginTop: 4,
    color: '#F0C36A',
    fontSize: 13,
  },
});
