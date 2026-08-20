package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckinRule
{
    /** 每日签到金额 CNY */
    private BigDecimal amount;
    /** 每个账户每天只能签到一次 */
    private Boolean oncePerDay = true;
    private List<CheckinPrizeRule> prizes = new ArrayList<CheckinPrizeRule>();

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Boolean getOncePerDay() { return oncePerDay; }
    public void setOncePerDay(Boolean oncePerDay) { this.oncePerDay = oncePerDay; }
    public List<CheckinPrizeRule> getPrizes() { return prizes; }
    public void setPrizes(List<CheckinPrizeRule> prizes) { this.prizes = prizes; }
}
