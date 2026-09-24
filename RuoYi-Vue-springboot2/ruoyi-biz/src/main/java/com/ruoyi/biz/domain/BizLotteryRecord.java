package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大转盘抽奖流水
 */
public class BizLotteryRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long recordId;

    /** 活动ID */
    private Long activityId;

    /** 会员ID */
    private Long memberId;

    /** 本次为第几次 */
    private Integer drawCount;

    /** 奖品ID */
    private Long prizeId;

    /** 奖品名称快照 */
    private String prizeName;

    /** 奖品类型快照 */
    private Integer prizeType;

    /** PUBLIC公海 EXCLUSIVE专属 */
    private String poolType;

    /** 是否步进必中 0否 1是 */
    private String isStrategy;

    /** 是否库存熔断 0否 1是 */
    private String isMeltdown;

    /** 0待处理 1已入账 2待领取 3已关闭 */
    private String grantStatus;

    /** 幂等业务号 */
    private String bizNo;

    /** 会员手机号（联表查询） */
    private transient String phone;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
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

    public Long getPrizeId()
    {
        return prizeId;
    }

    public void setPrizeId(Long prizeId)
    {
        this.prizeId = prizeId;
    }

    public String getPrizeName()
    {
        return prizeName;
    }

    public void setPrizeName(String prizeName)
    {
        this.prizeName = prizeName;
    }

    public Integer getPrizeType()
    {
        return prizeType;
    }

    public void setPrizeType(Integer prizeType)
    {
        this.prizeType = prizeType;
    }

    public String getPoolType()
    {
        return poolType;
    }

    public void setPoolType(String poolType)
    {
        this.poolType = poolType;
    }

    public String getIsStrategy()
    {
        return isStrategy;
    }

    public void setIsStrategy(String isStrategy)
    {
        this.isStrategy = isStrategy;
    }

    public String getIsMeltdown()
    {
        return isMeltdown;
    }

    public void setIsMeltdown(String isMeltdown)
    {
        this.isMeltdown = isMeltdown;
    }

    public String getGrantStatus()
    {
        return grantStatus;
    }

    public void setGrantStatus(String grantStatus)
    {
        this.grantStatus = grantStatus;
    }

    public String getBizNo()
    {
        return bizNo;
    }

    public void setBizNo(String bizNo)
    {
        this.bizNo = bizNo;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }
}
