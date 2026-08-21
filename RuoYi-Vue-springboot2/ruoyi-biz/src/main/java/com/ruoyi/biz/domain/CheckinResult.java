package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("签到结果/状态")
public class CheckinResult
{
    @ApiModelProperty("签到记录ID，未签到时可能为空")
    private Long checkinId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("签到日期")
    private Date checkinDate;
    @ApiModelProperty("本次入账金额")
    private BigDecimal amount;
    @ApiModelProperty("入账币种，当前为 CNY")
    private String currency;
    @ApiModelProperty("连续签到天数")
    private Integer streakDays;
    @ApiModelProperty("今天是否已签到")
    private Boolean checkedToday;
    @ApiModelProperty("签到规则")
    private CheckinRule rule;
    @ApiModelProperty("本次是否触发抽奖")
    private Boolean prizeDrawn;
    @ApiModelProperty("本次是否中奖")
    private Boolean prizeWon;
    @ApiModelProperty("中奖奖品名，未中奖为空")
    private String prizeName;
    @ApiModelProperty("触发抽奖对应的连续天数")
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
