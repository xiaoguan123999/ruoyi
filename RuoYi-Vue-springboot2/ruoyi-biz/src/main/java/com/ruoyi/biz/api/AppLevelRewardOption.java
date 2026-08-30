package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("等级奖励可领币种")
public class AppLevelRewardOption
{
    @ApiModelProperty("币种 CNY / USDT")
    private String currency;
    @ApiModelProperty("该币种金额")
    private BigDecimal amount;

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
