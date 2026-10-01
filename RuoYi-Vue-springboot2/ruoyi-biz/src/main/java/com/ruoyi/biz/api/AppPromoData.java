package com.ruoyi.biz.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("注册推广规则")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPromoData
{
    @ApiModelProperty("总开关")
    private Boolean enabled;
    @ApiModelProperty("实名注册奖励开关")
    private Boolean kycSelfEnabled;
    @ApiModelProperty("实名注册奖励人民币")
    private BigDecimal kycRewardCny;
    @ApiModelProperty("实名注册奖励USDT")
    private BigDecimal kycRewardUsdt;
    @ApiModelProperty("实名推广奖励开关")
    private Boolean inviteEnabled;
    @ApiModelProperty("邀请奖励金额")
    private BigDecimal inviteAmount;
    @ApiModelProperty("邀请奖励币种")
    private String inviteCurrency;
    @ApiModelProperty("注册后不可改上级")
    private Boolean lockParent;
    @ApiModelProperty("认购团队返佣开关")
    private Boolean teamEnabled;
    @ApiModelProperty("认购一级返佣百分比")
    private BigDecimal teamRateL1;
    @ApiModelProperty("认购二级返佣百分比")
    private BigDecimal teamRateL2;
    @ApiModelProperty("认购三级返佣百分比")
    private BigDecimal teamRateL3;
    @ApiModelProperty("充值团队返佣开关")
    private Boolean rechargeTeamEnabled;
    @ApiModelProperty("后台人工充值是否返佣")
    private Boolean rechargeTeamManualEnabled;
    @ApiModelProperty("充值一级返佣百分比")
    private BigDecimal rechargeTeamRateL1;
    @ApiModelProperty("充值二级返佣百分比")
    private BigDecimal rechargeTeamRateL2;
    @ApiModelProperty("充值三级返佣百分比")
    private BigDecimal rechargeTeamRateL3;
    @ApiModelProperty("规则说明全文")
    private String ruleText;
    @ApiModelProperty("当前会员实名状态，未登录不返回")
    private String kycStatus;
    @ApiModelProperty("是否已领实名注册奖励")
    private Boolean kycRewardClaimed;
    @ApiModelProperty("当前是否可领实名注册奖励")
    private Boolean kycRewardClaimable;
    @ApiModelProperty("已领币种")
    private String claimedCurrency;
    @ApiModelProperty("已领金额")
    private BigDecimal claimedAmount;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getKycSelfEnabled() { return kycSelfEnabled; }
    public void setKycSelfEnabled(Boolean kycSelfEnabled) { this.kycSelfEnabled = kycSelfEnabled; }
    public BigDecimal getKycRewardCny() { return kycRewardCny; }
    public void setKycRewardCny(BigDecimal kycRewardCny) { this.kycRewardCny = kycRewardCny; }
    public BigDecimal getKycRewardUsdt() { return kycRewardUsdt; }
    public void setKycRewardUsdt(BigDecimal kycRewardUsdt) { this.kycRewardUsdt = kycRewardUsdt; }
    public Boolean getInviteEnabled() { return inviteEnabled; }
    public void setInviteEnabled(Boolean inviteEnabled) { this.inviteEnabled = inviteEnabled; }
    public BigDecimal getInviteAmount() { return inviteAmount; }
    public void setInviteAmount(BigDecimal inviteAmount) { this.inviteAmount = inviteAmount; }
    public String getInviteCurrency() { return inviteCurrency; }
    public void setInviteCurrency(String inviteCurrency) { this.inviteCurrency = inviteCurrency; }
    public Boolean getLockParent() { return lockParent; }
    public void setLockParent(Boolean lockParent) { this.lockParent = lockParent; }
    public Boolean getTeamEnabled() { return teamEnabled; }
    public void setTeamEnabled(Boolean teamEnabled) { this.teamEnabled = teamEnabled; }
    public BigDecimal getTeamRateL1() { return teamRateL1; }
    public void setTeamRateL1(BigDecimal teamRateL1) { this.teamRateL1 = teamRateL1; }
    public BigDecimal getTeamRateL2() { return teamRateL2; }
    public void setTeamRateL2(BigDecimal teamRateL2) { this.teamRateL2 = teamRateL2; }
    public BigDecimal getTeamRateL3() { return teamRateL3; }
    public void setTeamRateL3(BigDecimal teamRateL3) { this.teamRateL3 = teamRateL3; }
    public Boolean getRechargeTeamEnabled() { return rechargeTeamEnabled; }
    public void setRechargeTeamEnabled(Boolean rechargeTeamEnabled) { this.rechargeTeamEnabled = rechargeTeamEnabled; }
    public Boolean getRechargeTeamManualEnabled() { return rechargeTeamManualEnabled; }
    public void setRechargeTeamManualEnabled(Boolean rechargeTeamManualEnabled) { this.rechargeTeamManualEnabled = rechargeTeamManualEnabled; }
    public BigDecimal getRechargeTeamRateL1() { return rechargeTeamRateL1; }
    public void setRechargeTeamRateL1(BigDecimal rechargeTeamRateL1) { this.rechargeTeamRateL1 = rechargeTeamRateL1; }
    public BigDecimal getRechargeTeamRateL2() { return rechargeTeamRateL2; }
    public void setRechargeTeamRateL2(BigDecimal rechargeTeamRateL2) { this.rechargeTeamRateL2 = rechargeTeamRateL2; }
    public BigDecimal getRechargeTeamRateL3() { return rechargeTeamRateL3; }
    public void setRechargeTeamRateL3(BigDecimal rechargeTeamRateL3) { this.rechargeTeamRateL3 = rechargeTeamRateL3; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public Boolean getKycRewardClaimed() { return kycRewardClaimed; }
    public void setKycRewardClaimed(Boolean kycRewardClaimed) { this.kycRewardClaimed = kycRewardClaimed; }
    public Boolean getKycRewardClaimable() { return kycRewardClaimable; }
    public void setKycRewardClaimable(Boolean kycRewardClaimable) { this.kycRewardClaimable = kycRewardClaimable; }
    public String getClaimedCurrency() { return claimedCurrency; }
    public void setClaimedCurrency(String claimedCurrency) { this.claimedCurrency = claimedCurrency; }
    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(BigDecimal claimedAmount) { this.claimedAmount = claimedAmount; }
}
