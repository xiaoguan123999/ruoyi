package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资金流水
 */
public class BizWalletLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long logId;

    /** 会员ID */
    private Long memberId;

    /** 手机号 */
    private String phone;

    /** 币种 */
    private String currency;

    /** 业务类型 */
    private String bizType;

    /** 业务单号 */
    private Long bizId;

    /** 变动金额 */
    private BigDecimal amount;

    /** 变动前可用 */
    private BigDecimal availableBefore;

    /** 变动后可用 */
    private BigDecimal availableAfter;

    /** 变动前冻结 */
    private BigDecimal frozenBefore;

    /** 变动后冻结 */
    private BigDecimal frozenAfter;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAvailableBefore()
    {
        return availableBefore;
    }

    public void setAvailableBefore(BigDecimal availableBefore)
    {
        this.availableBefore = availableBefore;
    }

    public BigDecimal getAvailableAfter()
    {
        return availableAfter;
    }

    public void setAvailableAfter(BigDecimal availableAfter)
    {
        this.availableAfter = availableAfter;
    }

    public BigDecimal getFrozenBefore()
    {
        return frozenBefore;
    }

    public void setFrozenBefore(BigDecimal frozenBefore)
    {
        this.frozenBefore = frozenBefore;
    }

    public BigDecimal getFrozenAfter()
    {
        return frozenAfter;
    }

    public void setFrozenAfter(BigDecimal frozenAfter)
    {
        this.frozenAfter = frozenAfter;
    }

}
