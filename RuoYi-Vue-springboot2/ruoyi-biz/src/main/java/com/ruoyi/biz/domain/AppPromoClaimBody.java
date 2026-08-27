package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("领取实名注册奖励")
public class AppPromoClaimBody
{
    @ApiModelProperty(value = "领取币种，CNY或USDT，二选一", required = true, example = "CNY")
    @JsonAlias({"type", "choice"})
    private String currency;

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
