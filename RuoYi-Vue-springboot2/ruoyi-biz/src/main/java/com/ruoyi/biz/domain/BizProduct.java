package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("认购产品")
public class BizProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("产品ID")
    private Long productId;

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("英文名，App 卡片副标题")
    private String nameEn;

    @ApiModelProperty("所属系列/分类ID")
    private Long categoryId;

    @ApiModelProperty("系列名称")
    private String categoryName;

    @ApiModelProperty("兼容字段，认购请看 priceCny / priceUsdt")
    private String currency;

    @ApiModelProperty("兼容字段：优先等于人民币价")
    private BigDecimal price;

    @ApiModelProperty("人民币认购价，大于0才支持人民币下单")
    private BigDecimal priceCny;

    @ApiModelProperty("USDT认购价，大于0才支持USDT下单")
    private BigDecimal priceUsdt;

    @ApiModelProperty("兼容字段：优先等于人民币日返")
    private BigDecimal dailyRebate;

    @ApiModelProperty("人民币每日返利")
    private BigDecimal dailyRebateCny;

    @ApiModelProperty("USDT每日返利")
    private BigDecimal dailyRebateUsdt;

    @ApiModelProperty("返利天数")
    private Integer durationDays;

    @ApiModelProperty("是否提现指定产品：1是，认购后才允许提现该币种")
    private String withdrawRequired;

    @ApiModelProperty("上架状态：0上架 1下架")
    private String status;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("产品封面图")
    private String coverUrl;

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

    public String getNameEn()
    {
        return nameEn;
    }

    public void setNameEn(String nameEn)
    {
        this.nameEn = nameEn;
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

    public BigDecimal getPriceCny()
    {
        return priceCny;
    }

    public void setPriceCny(BigDecimal priceCny)
    {
        this.priceCny = priceCny;
    }

    public BigDecimal getPriceUsdt()
    {
        return priceUsdt;
    }

    public void setPriceUsdt(BigDecimal priceUsdt)
    {
        this.priceUsdt = priceUsdt;
    }

    public BigDecimal getDailyRebateCny()
    {
        return dailyRebateCny;
    }

    public void setDailyRebateCny(BigDecimal dailyRebateCny)
    {
        this.dailyRebateCny = dailyRebateCny;
    }

    public BigDecimal getDailyRebateUsdt()
    {
        return dailyRebateUsdt;
    }

    public void setDailyRebateUsdt(BigDecimal dailyRebateUsdt)
    {
        this.dailyRebateUsdt = dailyRebateUsdt;
    }

    @ApiModelProperty("是否支持人民币认购")
    public Boolean getSupportsCny()
    {
        return Boolean.valueOf(hasPrice(priceCny));
    }

    @ApiModelProperty("是否支持USDT认购")
    public Boolean getSupportsUsdt()
    {
        return Boolean.valueOf(hasPrice(priceUsdt));
    }

    public static boolean hasPrice(BigDecimal value)
    {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal priceOf(String currency)
    {
        if ("USDT".equalsIgnoreCase(currency))
        {
            return priceUsdt;
        }
        return priceCny;
    }

    public BigDecimal rebateOf(String currency)
    {
        if ("USDT".equalsIgnoreCase(currency))
        {
            return dailyRebateUsdt;
        }
        return dailyRebateCny;
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

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }
}
