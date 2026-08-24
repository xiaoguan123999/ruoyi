package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("领取实名注册奖励")
public class AppPromoClaimBody
{
    @ApiModelProperty(value = "领取币种，CNY或USDT，二选一", required = true, example = "CNY")
    private String currency;

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
