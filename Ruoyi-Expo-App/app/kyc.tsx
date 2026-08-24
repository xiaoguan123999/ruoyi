import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { fetchAppProfile, isKycVerified, maskIdCard } from '@/api/app-auth';
import { submitAppKyc } from '@/api/app-member';
import { ApiError } from '@/api/request';
import type { RuoyiUser } from '@/api/types';
import { AppBackground } from '@/components/ui/AppBackground';
import { GlassCard } from '@/components/ui/GlassCard';
import { PageHeader } from '@/components/ui/PageHeader';
import { PrimaryButton } from '@/components/ui/PrimaryButton';
import { RefreshableScrollView } from '@/components/ui/RefreshableScrollView';
import { useAuth } from '@/hooks/useAuth';
import { colors } from '@/theme/colors';
import { validateIdCard } from '@/utils/id-card';
import { modalError, modalSuccess, modalWarning } from '@/utils/toast';

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
  const verified = isKycVerified(user?.kycStatus);
  const [name, setName] = useState(user?.realName ?? '');
  const [idNo, setIdNo] = useState(verified && user?.idCard ? maskIdCard(user.idCard) : '');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    try {
      const profile = await fetchAppProfile();
      applyProfileToForm(profile, setName, setIdNo);
    } catch {
      // 401 由全局处理；其它错误保留当前输入
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void load();
    }, [load]),
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
    } catch (error) {
      if (!(error instanceof ApiError) || error.code !== 401) {
        modalError(error instanceof ApiError ? error.message : '实名认证失败');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppBackground>
      <PageHeader title="实名认证" />
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
        </GlassCard>
        <View style={{ marginTop: 16 }}>
          <PrimaryButton
            title={verified ? '已实名认证' : '立即实名'}
            onPress={() => void onSubmit()}
            disabled={submitting || verified}
          />
        </View>
      </RefreshableScrollView>
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
});
