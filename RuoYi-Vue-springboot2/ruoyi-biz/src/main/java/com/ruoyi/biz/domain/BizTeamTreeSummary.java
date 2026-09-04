package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员结构图团队汇总")
public class BizTeamTreeSummary
{
    @ApiModelProperty("下级总人数")
    private Integer teamCount;

    @ApiModelProperty("直推人数")
    private Integer directCount;

    @ApiModelProperty("直推中有认购的人数")
    private Integer directActive;

    @ApiModelProperty("下级实名人数")
    private Integer teamKyc;

    @ApiModelProperty("下级认购 CNY")
    private BigDecimal subscribeCny;

    @ApiModelProperty("下级认购 USDT")
    private BigDecimal subscribeUsdt;

    @ApiModelProperty("下级充值 CNY")
    private BigDecimal rechargeCny;

    @ApiModelProperty("下级充值 USDT")
    private BigDecimal rechargeUsdt;

    @ApiModelProperty("下级提现 CNY")
    private BigDecimal withdrawCny;

    @ApiModelProperty("下级提现 USDT")
    private BigDecimal withdrawUsdt;

    public Integer getTeamCount() { return teamCount; }
    public void setTeamCount(Integer teamCount) { this.teamCount = teamCount; }
    public Integer getDirectCount() { return directCount; }
    public void setDirectCount(Integer directCount) { this.directCount = directCount; }
    public Integer getDirectActive() { return directActive; }
    public void setDirectActive(Integer directActive) { this.directActive = directActive; }
    public Integer getTeamKyc() { return teamKyc; }
    public void setTeamKyc(Integer teamKyc) { this.teamKyc = teamKyc; }
    public BigDecimal getSubscribeCny() { return subscribeCny; }
    public void setSubscribeCny(BigDecimal subscribeCny) { this.subscribeCny = subscribeCny; }
    public BigDecimal getSubscribeUsdt() { return subscribeUsdt; }
    public void setSubscribeUsdt(BigDecimal subscribeUsdt) { this.subscribeUsdt = subscribeUsdt; }
    public BigDecimal getRechargeCny() { return rechargeCny; }
    public void setRechargeCny(BigDecimal rechargeCny) { this.rechargeCny = rechargeCny; }
    public BigDecimal getRechargeUsdt() { return rechargeUsdt; }
    public void setRechargeUsdt(BigDecimal rechargeUsdt) { this.rechargeUsdt = rechargeUsdt; }
    public BigDecimal getWithdrawCny() { return withdrawCny; }
    public void setWithdrawCny(BigDecimal withdrawCny) { this.withdrawCny = withdrawCny; }
    public BigDecimal getWithdrawUsdt() { return withdrawUsdt; }
    public void setWithdrawUsdt(BigDecimal withdrawUsdt) { this.withdrawUsdt = withdrawUsdt; }
}
