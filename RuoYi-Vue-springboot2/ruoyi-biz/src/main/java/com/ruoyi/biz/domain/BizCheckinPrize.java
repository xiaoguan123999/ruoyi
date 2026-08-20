package com.ruoyi.biz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizCheckinPrize extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long prizeLogId;
    private Long memberId;
    private String phone;
    private Long checkinId;
    private Integer streakDays;
    private String prizeName;
    /** 0未中 1已中 */
    private String won;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getPrizeLogId() { return prizeLogId; }
    public void setPrizeLogId(Long prizeLogId) { this.prizeLogId = prizeLogId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getCheckinId() { return checkinId; }
    public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
    public String getWon() { return won; }
    public void setWon(String won) { this.won = won; }
    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
