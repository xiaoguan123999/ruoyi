package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppLevelRewardClaimData;
import com.ruoyi.biz.api.AppLevelRewardClaimItem;
import com.ruoyi.biz.domain.AppLevelRewardClaimBody;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizLevelRewardGrant;
import com.ruoyi.biz.domain.BizLevelRewardPayBody;
import com.ruoyi.biz.domain.BizLevelRewardRule;
import com.ruoyi.biz.domain.BizFxRateLog;

public interface IBizLevelRewardService
{
    BizLevelRewardRule getRule();

    void saveRule(BizLevelRewardRule rule);

    int updateLevelReward(BizLevel level);

    void evaluate(Long memberId);

    BizLevel matchLevel(Long memberId);

    int evaluateAll();

    List<BizLevelRewardGrant> selectGrantList(BizLevelRewardGrant grant);

    List<BizFxRateLog> selectFxRateLogList(BizFxRateLog log);

    void payGrant(Long grantId, String operator, String remark);

    void rejectGrant(Long grantId, String operator, String remark);

    void extraPay(BizLevelRewardPayBody body, String operator);

    void fillTeamDepthLabels(List<BizLevel> levels);

    void fillTeamDepthLabel(BizLevel level);

    void normalizeTeamDepth(BizLevel level);

    void applyThresholdModes(BizLevel level);

    void applyRewardGrantFields(BizLevel level);

    List<AppLevelRewardClaimItem> listClaimable(Long memberId);

    AppLevelRewardClaimData claimReward(Long memberId, AppLevelRewardClaimBody body);
}
