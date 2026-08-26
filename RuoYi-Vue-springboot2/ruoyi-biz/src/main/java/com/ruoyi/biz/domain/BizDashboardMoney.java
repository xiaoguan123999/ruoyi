package com.ruoyi.biz.domain;

import java.math.BigDecimal;

public class BizDashboardMoney
{
    private BigDecimal todayCny = BigDecimal.ZERO;
    private BigDecimal todayUsdt = BigDecimal.ZERO;
    private BigDecimal totalCny = BigDecimal.ZERO;
    private BigDecimal totalUsdt = BigDecimal.ZERO;
    private long todayCount;
    private long totalCount;

    public BizDashboardMoney()
    {
    }

    public BizDashboardMoney(BigDecimal todayCny, BigDecimal todayUsdt, BigDecimal totalCny, BigDecimal totalUsdt,
            Long todayCount, Long totalCount)
    {
        this.todayCny = BizDashboardCount.nvl(todayCny);
        this.todayUsdt = BizDashboardCount.nvl(todayUsdt);
        this.totalCny = BizDashboardCount.nvl(totalCny);
        this.totalUsdt = BizDashboardCount.nvl(totalUsdt);
        this.todayCount = BizDashboardCount.nvl(todayCount);
        this.totalCount = BizDashboardCount.nvl(totalCount);
    }

    public BigDecimal getTodayCny() { return todayCny; }
    public void setTodayCny(BigDecimal todayCny) { this.todayCny = todayCny; }
    public BigDecimal getTodayUsdt() { return todayUsdt; }
    public void setTodayUsdt(BigDecimal todayUsdt) { this.todayUsdt = todayUsdt; }
    public BigDecimal getTotalCny() { return totalCny; }
    public void setTotalCny(BigDecimal totalCny) { this.totalCny = totalCny; }
    public BigDecimal getTotalUsdt() { return totalUsdt; }
    public void setTotalUsdt(BigDecimal totalUsdt) { this.totalUsdt = totalUsdt; }
    public long getTodayCount() { return todayCount; }
    public void setTodayCount(long todayCount) { this.todayCount = todayCount; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
}
