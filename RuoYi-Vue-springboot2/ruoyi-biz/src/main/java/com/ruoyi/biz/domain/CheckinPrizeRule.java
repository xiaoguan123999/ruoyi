package com.ruoyi.biz.domain;

import java.math.BigDecimal;

public class CheckinPrizeRule
{
    private Integer days;
    private String name;
    /** 中奖概率，百分数，1 表示 1% */
    private BigDecimal rate;
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
