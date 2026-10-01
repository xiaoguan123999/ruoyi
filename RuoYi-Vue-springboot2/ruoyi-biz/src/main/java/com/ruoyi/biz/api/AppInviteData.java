package com.ruoyi.biz.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("邀请信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppInviteData
{
    @ApiModelProperty(value = "我的7位邀请码", example = "5839201")
    private String inviteCode;
    @ApiModelProperty(value = "直推人数")
    private Integer inviteCount;
    @ApiModelProperty(value = "邀请奖励整数金额，关闭或为0时返回0，便于旧版App展示")
    private Integer reward;
    @ApiModelProperty("每成功邀请1名实名用户的奖励金额")
    private BigDecimal inviteAmount;
    @ApiModelProperty("邀请奖励币种")
    private String inviteCurrency;
    @ApiModelProperty("实名注册奖励人民币")
    private BigDecimal kycRewardCny;
    @ApiModelProperty("实名注册奖励USDT")
    private BigDecimal kycRewardUsdt;
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
    @ApiModelProperty("注册绑定邀请码后不可改上级")
    private Boolean lockParent;

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public Integer getInviteCount() { return inviteCount; }
    public void setInviteCount(Integer inviteCount) { this.inviteCount = inviteCount; }
    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }
    public BigDecimal getInviteAmount() { return inviteAmount; }
    public void setInviteAmount(BigDecimal inviteAmount) { this.inviteAmount = inviteAmount; }
    public String getInviteCurrency() { return inviteCurrency; }
    public void setInviteCurrency(String inviteCurrency) { this.inviteCurrency = inviteCurrency; }
    public BigDecimal getKycRewardCny() { return kycRewardCny; }
    public void setKycRewardCny(BigDecimal kycRewardCny) { this.kycRewardCny = kycRewardCny; }
    public BigDecimal getKycRewardUsdt() { return kycRewardUsdt; }
    public void setKycRewardUsdt(BigDecimal kycRewardUsdt) { this.kycRewardUsdt = kycRewardUsdt; }
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
    public Boolean getLockParent() { return lockParent; }
    public void setLockParent(Boolean lockParent) { this.lockParent = lockParent; }
}
