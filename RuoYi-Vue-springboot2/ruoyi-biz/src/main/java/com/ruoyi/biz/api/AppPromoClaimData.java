package com.ruoyi.biz.api;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("实名注册奖励领取结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPromoClaimData
{
    @ApiModelProperty("入账币种")
    private String currency;
    @ApiModelProperty("入账金额")
    private BigDecimal amount;

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
