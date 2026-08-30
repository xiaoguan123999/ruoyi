package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 认购订单
 */
@ApiModel("认购订单")
public class BizOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @ApiModelProperty("订单ID")
    private Long orderId;

    /** 订单号 */
    @ApiModelProperty("订单号")
    private String orderNo;

    /** 会员ID */
    @ApiModelProperty("会员ID")
    private Long memberId;

    /** 手机号 */
    @ApiModelProperty("手机号")
    private String phone;

    /** 产品ID */
    @ApiModelProperty("产品ID")
    private Long productId;

    /** 产品名称 */
    @ApiModelProperty("产品名称")
    private String productName;

    /** 产品所属系列ID */
    @ApiModelProperty("系列ID，同 categoryId")
    private Long categoryId;

    /** 产品所属系列名称 */
    @ApiModelProperty("系列名称")
    private String categoryName;

    /** 系列封面 */
    @ApiModelProperty("系列封面图")
    private String seriesCoverUrl;

    /** 支付/返利币种 */
    @ApiModelProperty("支付/返利币种")
    private String currency;

    /** 认购价格 */
    @ApiModelProperty("认购价格")
    private BigDecimal price;

    /** 认购份数 */
    @ApiModelProperty("认购份数")
    private Integer quantity;

    /** 每日返利 */
    @ApiModelProperty("每日返利")
    private BigDecimal dailyRebate;

    /** 总天数 */
    @ApiModelProperty("总天数")
    private Integer durationDays;

    /** 剩余天数 */
    @ApiModelProperty("剩余天数")
    private Integer remainingDays;

    /** 上次返利日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastRebateDate;

    /** 是否提现指定产品 */
    @ApiModelProperty("是否提现指定产品")
    private String withdrawRequired;

    @ApiModelProperty("一拖二：下级几份同档激活上级1份，0关闭")
    private Integer unlockDirectQty;

    @ApiModelProperty("激活后再等待多少小时才开始日返，快照")
    private Integer unlockDelayHours;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("开始返利时间。一拖二未达标为 null")
    private Date incomeStartTime;

    @ApiModelProperty("激活状态：0未激活 1至少有一份已激活")
    private String activateStatus;

    @ApiModelProperty("是否至少有一份已到返利时间")
    private Boolean incomeReady;

    @ApiModelProperty("本单已激活份数")
    private Integer activatedQty;

    @ApiModelProperty("本单已开始返利份数")
    private Integer incomeReadyQty;

    @ApiModelProperty("已激活份数每天实际返利，币种同 currency")
    private BigDecimal incomeDailyRebate;

    @ApiModelProperty("直属下级已认购同档产品份数")
    private Integer unlockDirectHave;

    /** 状态 */
    @ApiModelProperty("订单状态：0持仓中 1已完成")
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

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    @ApiModelProperty("系列ID，同 categoryId")
    public Long getSeriesId()
    {
        return categoryId;
    }

    public void setSeriesId(Long seriesId)
    {
        this.categoryId = seriesId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    @ApiModelProperty("系列名称，同 categoryName")
    public String getSeriesName()
    {
        return categoryName;
    }

    public void setSeriesName(String seriesName)
    {
        this.categoryName = seriesName;
    }

    public String getSeriesCoverUrl()
    {
        return seriesCoverUrl;
    }

    public void setSeriesCoverUrl(String seriesCoverUrl)
    {
        this.seriesCoverUrl = seriesCoverUrl;
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

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public int quantityOrOne()
    {
        if (quantity == null || quantity.intValue() < 1)
        {
            return 1;
        }
        return quantity.intValue();
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

    public Integer getUnlockDirectQty() { return unlockDirectQty; }
    public void setUnlockDirectQty(Integer unlockDirectQty) { this.unlockDirectQty = unlockDirectQty; }
    public Integer getUnlockDelayHours() { return unlockDelayHours; }
    public void setUnlockDelayHours(Integer unlockDelayHours) { this.unlockDelayHours = unlockDelayHours; }
    public Date getIncomeStartTime() { return incomeStartTime; }
    public void setIncomeStartTime(Date incomeStartTime) { this.incomeStartTime = incomeStartTime; }
    public String getActivateStatus() { return activateStatus; }
    public void setActivateStatus(String activateStatus) { this.activateStatus = activateStatus; }
    public Boolean getIncomeReady() { return incomeReady; }
    public void setIncomeReady(Boolean incomeReady) { this.incomeReady = incomeReady; }
    public Integer getActivatedQty() { return activatedQty; }
    public void setActivatedQty(Integer activatedQty) { this.activatedQty = activatedQty; }
    public Integer getIncomeReadyQty() { return incomeReadyQty; }
    public void setIncomeReadyQty(Integer incomeReadyQty) { this.incomeReadyQty = incomeReadyQty; }
    public BigDecimal getIncomeDailyRebate() { return incomeDailyRebate; }
    public void setIncomeDailyRebate(BigDecimal incomeDailyRebate) { this.incomeDailyRebate = incomeDailyRebate; }
    public Integer getUnlockDirectHave() { return unlockDirectHave; }
    public void setUnlockDirectHave(Integer unlockDirectHave) { this.unlockDirectHave = unlockDirectHave; }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
