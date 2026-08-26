package com.ruoyi.biz.domain;

import java.math.BigDecimal;

public class BizDashboardCount
{
    private long today;
    private long total;

    public BizDashboardCount()
    {
    }

    public BizDashboardCount(Long today, Long total)
    {
        this.today = today == null ? 0L : today.longValue();
        this.total = total == null ? 0L : total.longValue();
    }

    public long getToday() { return today; }
    public void setToday(long today) { this.today = today; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public static long nvl(Long v)
    {
        return v == null ? 0L : v.longValue();
    }

    public static BigDecimal nvl(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }
}
