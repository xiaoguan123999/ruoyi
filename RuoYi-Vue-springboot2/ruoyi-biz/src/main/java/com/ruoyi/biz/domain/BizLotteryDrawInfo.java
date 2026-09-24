package com.ruoyi.biz.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * App 当前抽奖活动信息
 */
@ApiModel("大转盘当前活动信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BizLotteryDrawInfo
{
    @ApiModelProperty("活动ID")
    private Long activityId;

    @ApiModelProperty("活动名称")
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("开始时间")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("抽奖间隔小时")
    private Integer intervalHours;

    @ApiModelProperty("App展示抽奖规则（多行文本）")
    private String ruleText;

    @ApiModelProperty("当前累计抽奖次数")
    private Integer drawCount;

    @ApiModelProperty("可用抽奖次数")
    private Integer chanceBalance;

    @ApiModelProperty("是否可抽")
    private Boolean canDraw;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("下次可抽时间，可抽时为空")
    private Date nextDrawTime;

    @ApiModelProperty("奖品扇区列表（不含库存数字）")
    private List<BizLotteryPrizeView> prizes;

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Integer getIntervalHours() { return intervalHours; }
    public void setIntervalHours(Integer intervalHours) { this.intervalHours = intervalHours; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public Integer getDrawCount() { return drawCount; }
    public void setDrawCount(Integer drawCount) { this.drawCount = drawCount; }
    public Integer getChanceBalance() { return chanceBalance; }
    public void setChanceBalance(Integer chanceBalance) { this.chanceBalance = chanceBalance; }
    public Boolean getCanDraw() { return canDraw; }
    public void setCanDraw(Boolean canDraw) { this.canDraw = canDraw; }
    public Date getNextDrawTime() { return nextDrawTime; }
    public void setNextDrawTime(Date nextDrawTime) { this.nextDrawTime = nextDrawTime; }
    public List<BizLotteryPrizeView> getPrizes() { return prizes; }
    public void setPrizes(List<BizLotteryPrizeView> prizes) { this.prizes = prizes; }
}
