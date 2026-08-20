package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class CheckinResult
{
    private Long checkinId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkinDate;
    private BigDecimal amount;
    private String currency;
    private Integer streakDays;
    private Boolean checkedToday;
    private CheckinRule rule;
    /** 本次是否触发抽奖 */
    private Boolean prizeDrawn;
    private Boolean prizeWon;
    private String prizeName;
    private Integer prizeDays;

    public Long getCheckinId() { return checkinId; }
    public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }
    public Date getCheckinDate() { return checkinDate; }
    public void setCheckinDate(Date checkinDate) { this.checkinDate = checkinDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
    public Boolean getCheckedToday() { return checkedToday; }
    public void setCheckedToday(Boolean checkedToday) { this.checkedToday = checkedToday; }
    public CheckinRule getRule() { return rule; }
    public void setRule(CheckinRule rule) { this.rule = rule; }
    public Boolean getPrizeDrawn() { return prizeDrawn; }
    public void setPrizeDrawn(Boolean prizeDrawn) { this.prizeDrawn = prizeDrawn; }
    public Boolean getPrizeWon() { return prizeWon; }
    public void setPrizeWon(Boolean prizeWon) { this.prizeWon = prizeWon; }
    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
    public Integer getPrizeDays() { return prizeDays; }
    public void setPrizeDays(Integer prizeDays) { this.prizeDays = prizeDays; }
}
