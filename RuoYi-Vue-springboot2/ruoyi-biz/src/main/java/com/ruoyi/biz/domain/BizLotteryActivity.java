package com.ruoyi.biz.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大转盘抽奖活动
 */
public class BizLotteryActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    private String title;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 抽奖频控间隔(小时) */
    private Integer intervalHours;

    /** App展示抽奖规则（多行） */
    private String ruleText;

    /** 0正常 1停用 */
    private String status;

    /** 奖品列表（详情） */
    private transient List<BizLotteryPrize> prizes;

    /** 必中策略列表（详情） */
    private transient List<BizLotteryWinStrategy> strategies;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Integer getIntervalHours()
    {
        return intervalHours;
    }

    public void setIntervalHours(Integer intervalHours)
    {
        this.intervalHours = intervalHours;
    }

    public String getRuleText()
    {
        return ruleText;
    }

    public void setRuleText(String ruleText)
    {
        this.ruleText = ruleText;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<BizLotteryPrize> getPrizes()
    {
        return prizes;
    }

    public void setPrizes(List<BizLotteryPrize> prizes)
    {
        this.prizes = prizes;
    }

    public List<BizLotteryWinStrategy> getStrategies()
    {
        return strategies;
    }

    public void setStrategies(List<BizLotteryWinStrategy> strategies)
    {
        this.strategies = strategies;
    }
}
