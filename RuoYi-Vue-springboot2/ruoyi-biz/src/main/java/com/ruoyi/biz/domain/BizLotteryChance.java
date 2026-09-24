package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 会员抽奖次数余额
 */
public class BizLotteryChance
{
    private Long id;
    private Long activityId;
    private Long memberId;
    private Integer balance;
    private Integer totalGranted;
    private Integer totalConsumed;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 查询展示 */
    private String phone;
    private String realName;
    private String activityTitle;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Integer getBalance() { return balance; }
    public void setBalance(Integer balance) { this.balance = balance; }
    public Integer getTotalGranted() { return totalGranted; }
    public void setTotalGranted(Integer totalGranted) { this.totalGranted = totalGranted; }
    public Integer getTotalConsumed() { return totalConsumed; }
    public void setTotalConsumed(Integer totalConsumed) { this.totalConsumed = totalConsumed; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
}
