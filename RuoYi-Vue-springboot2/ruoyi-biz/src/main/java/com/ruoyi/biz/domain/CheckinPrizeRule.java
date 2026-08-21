package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("签到抽奖档位")
public class CheckinPrizeRule
{
    @ApiModelProperty("连续签到天数，例如 180")
    private Integer days;
    @ApiModelProperty("奖品名称")
    private String name;
    @ApiModelProperty("中奖概率百分数，1 表示 1%")
    private BigDecimal rate;
    @ApiModelProperty("是否开启该档")
    private Boolean enabled;

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
