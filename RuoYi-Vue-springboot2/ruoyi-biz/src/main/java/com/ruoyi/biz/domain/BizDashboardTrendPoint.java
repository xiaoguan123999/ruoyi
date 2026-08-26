package com.ruoyi.biz.domain;

import java.math.BigDecimal;

public class BizDashboardTrendPoint
{
    private String dayKey;
    private Long cnt;
    private BigDecimal amount;

    public String getDayKey() { return dayKey; }
    public void setDayKey(String dayKey) { this.dayKey = dayKey; }
    public Long getCnt() { return cnt; }
    public void setCnt(Long cnt) { this.cnt = cnt; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
