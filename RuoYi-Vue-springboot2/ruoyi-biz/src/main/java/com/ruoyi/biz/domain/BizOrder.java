package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 认购订单
 */
public class BizOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 手机号 */
    private String phone;

    /** 产品ID */
    private Long productId;

    /** 产品名称 */
    private String productName;

    /** 支付/返利币种 */
    private String currency;

    /** 认购价格 */
    private BigDecimal price;

    /** 每日返利 */
    private BigDecimal dailyRebate;

    /** 总天数 */
    private Integer durationDays;

    /** 剩余天数 */
    private Integer remainingDays;

    /** 上次返利日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastRebateDate;

    /** 是否提现指定产品 */
    private String withdrawRequired;

    /** 状态 */
    private String status;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
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

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getDailyRebate()
    {
        return dailyRebate;
    }

    public void setDailyRebate(BigDecimal dailyRebate)
    {
        this.dailyRebate = dailyRebate;
    }

    public Integer getDurationDays()
    {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays)
    {
        this.durationDays = durationDays;
    }

    public Integer getRemainingDays()
    {
        return remainingDays;
    }

    public void setRemainingDays(Integer remainingDays)
    {
        this.remainingDays = remainingDays;
    }

    public Date getLastRebateDate()
    {
        return lastRebateDate;
    }

    public void setLastRebateDate(Date lastRebateDate)
    {
        this.lastRebateDate = lastRebateDate;
    }

    public String getWithdrawRequired()
    {
        return withdrawRequired;
    }

    public void setWithdrawRequired(String withdrawRequired)
    {
        this.withdrawRequired = withdrawRequired;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
