package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品返利记录
 */
public class BizRebateLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 返利ID */
    private Long rebateId;

    /** 订单ID */
    private Long orderId;

    /** 会员ID */
    private Long memberId;

    /** 币种 */
    private String currency;

    /** 返利金额 */
    private BigDecimal amount;

    /** 返利日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date rebateDate;

    public Long getRebateId()
    {
        return rebateId;
    }

    public void setRebateId(Long rebateId)
    {
        this.rebateId = rebateId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
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

    public Date getRebateDate()
    {
        return rebateDate;
    }

    public void setRebateDate(Date rebateDate)
    {
        this.rebateDate = rebateDate;
    }

}
