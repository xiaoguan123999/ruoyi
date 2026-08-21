package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("签到规则")
public class CheckinRule
{
    @ApiModelProperty("每日签到金额，币种 CNY")
    private BigDecimal amount;
    @ApiModelProperty("是否每天只能签一次，当前恒为 true")
    private Boolean oncePerDay = true;
    @ApiModelProperty("连续签到抽奖档位")
    private List<CheckinPrizeRule> prizes = new ArrayList<CheckinPrizeRule>();

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Boolean getOncePerDay() { return oncePerDay; }
    public void setOncePerDay(Boolean oncePerDay) { this.oncePerDay = oncePerDay; }
    public List<CheckinPrizeRule> getPrizes() { return prizes; }
    public void setPrizes(List<CheckinPrizeRule> prizes) { this.prizes = prizes; }
}
