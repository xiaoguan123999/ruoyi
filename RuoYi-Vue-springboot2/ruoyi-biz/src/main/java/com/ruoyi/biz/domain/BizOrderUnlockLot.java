package com.ruoyi.biz.domain;

import java.util.Date;

public class BizOrderUnlockLot
{
    private Long lotId;
    private Long orderId;
    private Integer shareNo;
    private Integer qty;
    private Date activateTime;
    private Date incomeStartTime;
    private Integer remainingDays;
    private Date lastRebateDate;

    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getShareNo() { return shareNo; }
    public void setShareNo(Integer shareNo) { this.shareNo = shareNo; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public Date getActivateTime() { return activateTime; }
    public void setActivateTime(Date activateTime) { this.activateTime = activateTime; }
    public Date getIncomeStartTime() { return incomeStartTime; }
    public void setIncomeStartTime(Date incomeStartTime) { this.incomeStartTime = incomeStartTime; }
    public Integer getRemainingDays() { return remainingDays; }
    public void setRemainingDays(Integer remainingDays) { this.remainingDays = remainingDays; }
    public Date getLastRebateDate() { return lastRebateDate; }
    public void setLastRebateDate(Date lastRebateDate) { this.lastRebateDate = lastRebateDate; }
}
