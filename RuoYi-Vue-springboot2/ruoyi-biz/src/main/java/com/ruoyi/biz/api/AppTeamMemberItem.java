package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("团队成员")
public class AppTeamMemberItem
{
    @ApiModelProperty("会员ID")
    private Long memberId;
    @ApiModelProperty("姓名")
    private String name;
    @ApiModelProperty("真实姓名")
    private String realName;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("实名 0/1")
    private String kycStatus;
    @ApiModelProperty("充值USDT")
    private BigDecimal usd;
    @ApiModelProperty("充值USDT")
    private BigDecimal usdt;
    @ApiModelProperty("充值USDT")
    private BigDecimal rechargeUsdt;
    @ApiModelProperty("充值CNY")
    private BigDecimal cny;
    @ApiModelProperty("充值CNY")
    private BigDecimal rechargeCny;
    @ApiModelProperty("认购USDT")
    private BigDecimal subscribeUsdt;
    @ApiModelProperty("认购CNY")
    private BigDecimal subscribeCny;
    @ApiModelProperty("团队层级 1-7")
    private Integer teamLevel;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
    public BigDecimal getUsd() { return usd; }
    public void setUsd(BigDecimal usd) { this.usd = usd; }
    public BigDecimal getUsdt() { return usdt; }
    public void setUsdt(BigDecimal usdt) { this.usdt = usdt; }
    public BigDecimal getRechargeUsdt() { return rechargeUsdt; }
    public void setRechargeUsdt(BigDecimal rechargeUsdt) { this.rechargeUsdt = rechargeUsdt; }
    public BigDecimal getCny() { return cny; }
    public void setCny(BigDecimal cny) { this.cny = cny; }
    public BigDecimal getRechargeCny() { return rechargeCny; }
    public void setRechargeCny(BigDecimal rechargeCny) { this.rechargeCny = rechargeCny; }
    public BigDecimal getSubscribeUsdt() { return subscribeUsdt; }
    public void setSubscribeUsdt(BigDecimal subscribeUsdt) { this.subscribeUsdt = subscribeUsdt; }
    public BigDecimal getSubscribeCny() { return subscribeCny; }
    public void setSubscribeCny(BigDecimal subscribeCny) { this.subscribeCny = subscribeCny; }
    public Integer getTeamLevel() { return teamLevel; }
    public void setTeamLevel(Integer teamLevel) { this.teamLevel = teamLevel; }
}
