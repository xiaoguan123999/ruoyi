package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("后台团队总汇总（不限层级，不含本人，不含测试号）")
public class BizTeamOverview
{
    @ApiModelProperty("团队今日注册")
    private Integer registerToday;
    @ApiModelProperty("团队总注册")
    private Integer registerTotal;
    @ApiModelProperty("团队今日实名")
    private Integer kycToday;
    @ApiModelProperty("团队总实名")
    private Integer kycTotal;
    @ApiModelProperty("团队今日激活人数")
    private Integer activeToday;
    @ApiModelProperty("团队总激活人数")
    private Integer activeTotal;
    @ApiModelProperty("团队今日总认购CNY")
    private BigDecimal subscribeTodayCny;
    @ApiModelProperty("团队总认购CNY")
    private BigDecimal subscribeTotalCny;
    @ApiModelProperty("团队今日总认购USDT")
    private BigDecimal subscribeTodayUsdt;
    @ApiModelProperty("团队总认购USDT")
    private BigDecimal subscribeTotalUsdt;
    @ApiModelProperty("团队今日充值CNY")
    private BigDecimal rechargeTodayCny;
    @ApiModelProperty("团队总充值CNY")
    private BigDecimal rechargeTotalCny;
    @ApiModelProperty("团队今日充值USDT")
    private BigDecimal rechargeTodayUsdt;
    @ApiModelProperty("团队总充值USDT")
    private BigDecimal rechargeTotalUsdt;
    @ApiModelProperty("团队今日签到人数")
    private Integer checkinToday;
    @ApiModelProperty("团队昨日签到人数")
    private Integer checkinYesterday;

    public Integer getRegisterToday() { return registerToday; }
    public void setRegisterToday(Integer registerToday) { this.registerToday = registerToday; }
    public Integer getRegisterTotal() { return registerTotal; }
    public void setRegisterTotal(Integer registerTotal) { this.registerTotal = registerTotal; }
    public Integer getKycToday() { return kycToday; }
    public void setKycToday(Integer kycToday) { this.kycToday = kycToday; }
    public Integer getKycTotal() { return kycTotal; }
    public void setKycTotal(Integer kycTotal) { this.kycTotal = kycTotal; }
    public Integer getActiveToday() { return activeToday; }
    public void setActiveToday(Integer activeToday) { this.activeToday = activeToday; }
    public Integer getActiveTotal() { return activeTotal; }
    public void setActiveTotal(Integer activeTotal) { this.activeTotal = activeTotal; }
    public BigDecimal getSubscribeTodayCny() { return subscribeTodayCny; }
    public void setSubscribeTodayCny(BigDecimal subscribeTodayCny) { this.subscribeTodayCny = subscribeTodayCny; }
    public BigDecimal getSubscribeTotalCny() { return subscribeTotalCny; }
    public void setSubscribeTotalCny(BigDecimal subscribeTotalCny) { this.subscribeTotalCny = subscribeTotalCny; }
    public BigDecimal getSubscribeTodayUsdt() { return subscribeTodayUsdt; }
    public void setSubscribeTodayUsdt(BigDecimal subscribeTodayUsdt) { this.subscribeTodayUsdt = subscribeTodayUsdt; }
    public BigDecimal getSubscribeTotalUsdt() { return subscribeTotalUsdt; }
    public void setSubscribeTotalUsdt(BigDecimal subscribeTotalUsdt) { this.subscribeTotalUsdt = subscribeTotalUsdt; }
    public BigDecimal getRechargeTodayCny() { return rechargeTodayCny; }
    public void setRechargeTodayCny(BigDecimal rechargeTodayCny) { this.rechargeTodayCny = rechargeTodayCny; }
    public BigDecimal getRechargeTotalCny() { return rechargeTotalCny; }
    public void setRechargeTotalCny(BigDecimal rechargeTotalCny) { this.rechargeTotalCny = rechargeTotalCny; }
    public BigDecimal getRechargeTodayUsdt() { return rechargeTodayUsdt; }
    public void setRechargeTodayUsdt(BigDecimal rechargeTodayUsdt) { this.rechargeTodayUsdt = rechargeTodayUsdt; }
    public BigDecimal getRechargeTotalUsdt() { return rechargeTotalUsdt; }
    public void setRechargeTotalUsdt(BigDecimal rechargeTotalUsdt) { this.rechargeTotalUsdt = rechargeTotalUsdt; }
    public Integer getCheckinToday() { return checkinToday; }
    public void setCheckinToday(Integer checkinToday) { this.checkinToday = checkinToday; }
    public Integer getCheckinYesterday() { return checkinYesterday; }
    public void setCheckinYesterday(Integer checkinYesterday) { this.checkinYesterday = checkinYesterday; }
}
