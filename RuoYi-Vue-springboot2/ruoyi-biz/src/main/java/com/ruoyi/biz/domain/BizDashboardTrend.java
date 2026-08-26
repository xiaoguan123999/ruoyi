package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BizDashboardTrend
{
    private List<String> dates = new ArrayList<String>();
    private List<Long> register = new ArrayList<Long>();
    private List<Long> orderCount = new ArrayList<Long>();
    private List<Long> orderUsers = new ArrayList<Long>();
    private List<BigDecimal> rechargeCny = new ArrayList<BigDecimal>();
    private List<BigDecimal> withdrawCny = new ArrayList<BigDecimal>();

    public List<String> getDates() { return dates; }
    public void setDates(List<String> dates) { this.dates = dates; }
    public List<Long> getRegister() { return register; }
    public void setRegister(List<Long> register) { this.register = register; }
    public List<Long> getOrderCount() { return orderCount; }
    public void setOrderCount(List<Long> orderCount) { this.orderCount = orderCount; }
    public List<Long> getOrderUsers() { return orderUsers; }
    public void setOrderUsers(List<Long> orderUsers) { this.orderUsers = orderUsers; }
    public List<BigDecimal> getRechargeCny() { return rechargeCny; }
    public void setRechargeCny(List<BigDecimal> rechargeCny) { this.rechargeCny = rechargeCny; }
    public List<BigDecimal> getWithdrawCny() { return withdrawCny; }
    public void setWithdrawCny(List<BigDecimal> withdrawCny) { this.withdrawCny = withdrawCny; }
}
