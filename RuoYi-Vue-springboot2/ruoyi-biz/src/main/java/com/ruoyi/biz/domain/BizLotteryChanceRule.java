package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 抽奖获次规则
 */
public class BizLotteryChanceRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private Long activityId;
    private String ruleName;
    /** 需累计签到天数，0=不限制 */
    private Integer checkinDays;

    /** 连续签到天数，0=不限制 */
    private Integer streakDays;
    /** 需直推实名人数，0=不限制 */
    private Integer inviteKycCount;
    /** 每档达标发放的抽奖次数 */
    private Integer grantAmount;
    /** 1仅发放一次；0按条件倍数可重复发放 */
    private String onceOnly;
    /** 重复模式下最多发放档次数：0=不限制（按实际倍数全发）；仅一次时固定为1 */
    private Integer maxRepeat;
    private String status;
    private Integer sort;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getCheckinDays() { return checkinDays; }
    public void setCheckinDays(Integer checkinDays) { this.checkinDays = checkinDays; }

    public Integer getStreakDays()
    {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays)
    {
        this.streakDays = streakDays;
    }
    public Integer getInviteKycCount() { return inviteKycCount; }
    public void setInviteKycCount(Integer inviteKycCount) { this.inviteKycCount = inviteKycCount; }
    public Integer getGrantAmount() { return grantAmount; }
    public void setGrantAmount(Integer grantAmount) { this.grantAmount = grantAmount; }
    public String getOnceOnly() { return onceOnly; }
    public void setOnceOnly(String onceOnly) { this.onceOnly = onceOnly; }
    public Integer getMaxRepeat() { return maxRepeat; }
    public void setMaxRepeat(Integer maxRepeat) { this.maxRepeat = maxRepeat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
