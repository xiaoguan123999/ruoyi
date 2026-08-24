package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppInviteData;
import com.ruoyi.biz.api.AppPromoClaimData;
import com.ruoyi.biz.api.AppPromoData;
import com.ruoyi.biz.domain.AppPromoClaimBody;
import com.ruoyi.biz.domain.BizPromoGrant;
import com.ruoyi.biz.domain.BizPromoRule;

public interface IBizPromoService
{
    BizPromoRule getRule();

    void saveRule(BizPromoRule rule);

    AppPromoData getAppPromo(Long memberId);

    void fillInvite(AppInviteData data);

    void grantInviteOnKyc(Long memberId);

    AppPromoClaimData claimKycReward(Long memberId, AppPromoClaimBody body);

    List<BizPromoGrant> selectGrantList(BizPromoGrant grant);
}
