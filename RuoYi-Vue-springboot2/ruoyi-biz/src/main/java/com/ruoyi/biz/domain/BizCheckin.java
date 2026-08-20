package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 签到记录
 */
public class BizCheckin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 签到ID */
    private Long checkinId;

    /** 会员ID */
    private Long memberId;

    /** 手机号 */
    private String phone;

    /** 签到日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkinDate;

    /** 奖励金额 */
    private BigDecimal amount;

    /** 币种 */
    private String currency;

    public Long getCheckinId()
    {
        return checkinId;
    }

    public void setCheckinId(Long checkinId)
    {
        this.checkinId = checkinId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public Date getCheckinDate()
    {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate)
    {
        this.checkinDate = checkinDate;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

}
