package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 认购产品
 */
public class BizProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 产品ID */
    private Long productId;

    /** 产品名称 */
    private String productName;

    /** 币种 CNY/USDT */
    private String currency;

    /** 认购价格 */
    private BigDecimal price;

    /** 每日返利 */
    private BigDecimal dailyRebate;

    /** 返利天数 */
    private Integer durationDays;

    /** 是否提现指定产品 */
    private String withdrawRequired;

    /** 状态 */
    private String status;

    /** 排序 */
    private Integer sort;

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

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }

}
