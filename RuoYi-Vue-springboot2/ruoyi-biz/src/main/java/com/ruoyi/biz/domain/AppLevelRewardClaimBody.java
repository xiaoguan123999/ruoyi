package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("领取等级奖励")
public class AppLevelRewardClaimBody
{
    @ApiModelProperty(value = "等级ID", required = true)
    private Long levelId;

    @ApiModelProperty(value = "领取币种 CNY 或 USDT", required = true, example = "CNY")
    private String currency;

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
