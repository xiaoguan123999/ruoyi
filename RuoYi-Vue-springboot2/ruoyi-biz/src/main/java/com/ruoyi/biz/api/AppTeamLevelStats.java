package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("团队某层汇总")
public class AppTeamLevelStats
{
    @ApiModelProperty("层级 1-7")
    private Integer teamLevel;
    @ApiModelProperty("注册人数")
    private Integer register;
    @ApiModelProperty("激活人数（有认购订单）")
    private Integer active;
    @ApiModelProperty("认购USDT")
    private BigDecimal subscribeUsd;
    @ApiModelProperty("认购USDT")
    private BigDecimal subscribeUsdt;
    @ApiModelProperty("认购CNY")
    private BigDecimal subscribeCny;
    @ApiModelProperty("充值USDT")
    private BigDecimal rechargeUsd;
    @ApiModelProperty("充值USDT")
    private BigDecimal rechargeUsdt;
    @ApiModelProperty("充值CNY")
    private BigDecimal rechargeCny;

    public Integer getTeamLevel() { return teamLevel; }
    public void setTeamLevel(Integer teamLevel) { this.teamLevel = teamLevel; }
    public Integer getRegister() { return register; }
    public void setRegister(Integer register) { this.register = register; }
    public Integer getActive() { return active; }
    public void setActive(Integer active) { this.active = active; }
    public BigDecimal getSubscribeUsd() { return subscribeUsd; }
    public void setSubscribeUsd(BigDecimal subscribeUsd) { this.subscribeUsd = subscribeUsd; }
    public BigDecimal getSubscribeUsdt() { return subscribeUsdt; }
    public void setSubscribeUsdt(BigDecimal subscribeUsdt) { this.subscribeUsdt = subscribeUsdt; }
    public BigDecimal getSubscribeCny() { return subscribeCny; }
    public void setSubscribeCny(BigDecimal subscribeCny) { this.subscribeCny = subscribeCny; }
    public BigDecimal getRechargeUsd() { return rechargeUsd; }
    public void setRechargeUsd(BigDecimal rechargeUsd) { this.rechargeUsd = rechargeUsd; }
    public BigDecimal getRechargeUsdt() { return rechargeUsdt; }
    public void setRechargeUsdt(BigDecimal rechargeUsdt) { this.rechargeUsdt = rechargeUsdt; }
    public BigDecimal getRechargeCny() { return rechargeCny; }
    public void setRechargeCny(BigDecimal rechargeCny) { this.rechargeCny = rechargeCny; }
}
