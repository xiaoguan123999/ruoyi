package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 提现申请
 */
public class BizWithdraw extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 提现ID */
    private Long withdrawId;

    /** 会员ID */
    private Long memberId;

    /** 手机号 */
    private String phone;

    /** 币种 */
    private String currency;

    /** 金额 */
    private BigDecimal amount;

    /** 收款信息 */
    private String accountInfo;

    /** 状态 */
    private String status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核备注 */
    private String auditRemark;

    public Long getWithdrawId()
    {
        return withdrawId;
    }

    public void setWithdrawId(Long withdrawId)
    {
        this.withdrawId = withdrawId;
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

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getAccountInfo()
    {
        return accountInfo;
    }

    public void setAccountInfo(String accountInfo)
    {
        this.accountInfo = accountInfo;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

}
