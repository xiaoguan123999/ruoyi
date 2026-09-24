package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会员抽奖资格概况
 */
public class BizLotteryMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 活动ID */
    private Long activityId;

    /** 会员ID */
    private Long memberId;

    /** 累计抽奖次数 */
    private Integer drawCount;

    /** 最近抽奖时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastDrawTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Integer getDrawCount()
    {
        return drawCount;
    }

    public void setDrawCount(Integer drawCount)
    {
        this.drawCount = drawCount;
    }

    public Date getLastDrawTime()
    {
        return lastDrawTime;
    }

    public void setLastDrawTime(Date lastDrawTime)
    {
        this.lastDrawTime = lastDrawTime;
    }
}
