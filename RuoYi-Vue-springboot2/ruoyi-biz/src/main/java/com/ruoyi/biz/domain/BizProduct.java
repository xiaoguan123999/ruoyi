package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.List;
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

    @ApiModelProperty("每人限购份数，0表示不限制")
    private Integer buyLimit;

    @ApiModelProperty("直属下级需认购同档产品多少份才能激活上级1份，0表示关闭一拖二")
    private Integer unlockDirectQty;

    @ApiModelProperty("激活后再等待多少小时才开始日返，0表示激活后即可日返")
    private Integer unlockDelayHours;

    @ApiModelProperty("激活条件文案，后台填写，App 原样展示")
    private String unlockRuleText;

    @ApiModelProperty("收益发放方式，App 展示")
    private String payoutMethod;

    @ApiModelProperty("风险等级，App 展示")
    private String riskLevel;

    @ApiModelProperty("是否开售：1开售 0未开售。未开售时 App 不可进详情、不可认购")
    private String onSale;

    @ApiModelProperty("跳过二级页：1列表直购 0进认购页（与业务模式无关）")
    private String skipDetail;

    @ApiModelProperty("业务模式：REBATE日返 / ASSIST助力退本")
    private String bizMode;

    @ApiModelProperty("助力发放模式：MATCH跟认购币 / CNY固定送CNY / USDT固定送USDT / BOTH双币都送")
    private String assistGrantMode;

    @ApiModelProperty("助力值(CNY侧发放额度，与人民币1:1计量)")
    private java.math.BigDecimal assistValueCny;

    @ApiModelProperty("助力值(USDT侧发放额度)")
    private java.math.BigDecimal assistValueUsdt;

    @ApiModelProperty("本金返还天数（ASSIST）")
    private Integer principalReturnDays;

    @ApiModelProperty("日返入账 CREDIT进钱包 / ACCUMULATE订单累计")
    private String incomeMode;

    @ApiModelProperty("累计周期天数，如60；0表示不用")
    private Integer accumulateCycleDays;

    @ApiModelProperty("对档产品ID，结算累计前须持有")
    private Long relatedProductId;

    @ApiModelProperty("对档产品名称（列表展示）")
    private String relatedProductName;

    @ApiModelProperty("上架状态：0上架 1下架")
    private String status;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("产品封面图")
    private String coverUrl;

    @ApiModelProperty("卡片模板ID")
    private Long templateId;

    @ApiModelProperty("模板名称（列表展示）")
    private String templateName;

    @ApiModelProperty("模板编码 / App layoutType")
    private String templateCode;

    @ApiModelProperty("App 布局类型，同 templateCode")
    private String layoutType;

    @ApiModelProperty("主题色：blue/purple/gold/cyan/silver")
    private String theme;

    @ApiModelProperty("角标覆盖，空则用系列名")
    private String badgeText;

    @ApiModelProperty("卡片序号")
    private String cardNo;

    @ApiModelProperty("主按钮文案覆盖")
    private String ctaText;

    @ApiModelProperty("模板默认按钮文案")
    private String templateDefaultCta;

    @ApiModelProperty("模板是否有主金额区")
    private String hasMainAmount;

    @ApiModelProperty("主金额展示文案（后端组装）")
    private String mainAmountDisplay;

    @ApiModelProperty("指标槽配置（后台保存/详情）")
    private List<BizProductCardMetric> metrics;

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

    public Integer getBuyLimit()
    {
        return buyLimit;
    }

    public void setBuyLimit(Integer buyLimit)
    {
        this.buyLimit = buyLimit;
    }

    public Integer getUnlockDirectQty()
    {
        return unlockDirectQty;
    }

    public void setUnlockDirectQty(Integer unlockDirectQty)
    {
        this.unlockDirectQty = unlockDirectQty;
    }

    public Integer getUnlockDelayHours()
    {
        return unlockDelayHours;
    }

    public void setUnlockDelayHours(Integer unlockDelayHours)
    {
        this.unlockDelayHours = unlockDelayHours;
    }

    public String getUnlockRuleText()
    {
        return unlockRuleText;
    }

    public void setUnlockRuleText(String unlockRuleText)
    {
        this.unlockRuleText = unlockRuleText;
    }

    public String getPayoutMethod()
    {
        return payoutMethod;
    }

    public void setPayoutMethod(String payoutMethod)
    {
        this.payoutMethod = payoutMethod;
    }

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getOnSale()
    {
        return onSale;
    }

    public void setOnSale(String onSale)
    {
        this.onSale = onSale;
    }

    public String getSkipDetail()
    {
        return skipDetail;
    }

    public void setSkipDetail(String skipDetail)
    {
        this.skipDetail = skipDetail;
    }

    public Boolean getSkipDetailFlag()
    {
        return Boolean.valueOf(skipDetailEnabled());
    }

    public boolean skipDetailEnabled()
    {
        return "1".equals(skipDetail);
    }

    public String getBizMode()
    {
        return bizMode;
    }

    public void setBizMode(String bizMode)
    {
        this.bizMode = bizMode;
    }

    public boolean assistMode()
    {
        return "ASSIST".equalsIgnoreCase(bizMode);
    }

    /** 是否配置了可发放的助力值（日返产品可选） */
    public boolean assistGrantConfigured()
    {
        return positive(assistValueCny) || positive(assistValueUsdt);
    }

    public String getAssistGrantMode()
    {
        return assistGrantMode;
    }

    public void setAssistGrantMode(String assistGrantMode)
    {
        this.assistGrantMode = assistGrantMode;
    }

    /** 规范化发放模式，空则 CNY（当前星航默认：助力按人民币计量） */
    public String resolveAssistGrantMode()
    {
        if (assistGrantMode == null || assistGrantMode.trim().isEmpty())
        {
            return "CNY";
        }
        String m = assistGrantMode.trim().toUpperCase();
        if ("MATCH".equals(m) || "CNY".equals(m) || "USDT".equals(m) || "BOTH".equals(m))
        {
            return m;
        }
        return "CNY";
    }

    public java.math.BigDecimal getAssistValueCny()
    {
        return assistValueCny;
    }

    public void setAssistValueCny(java.math.BigDecimal assistValueCny)
    {
        this.assistValueCny = assistValueCny;
    }

    public java.math.BigDecimal getAssistValueUsdt()
    {
        return assistValueUsdt;
    }

    public void setAssistValueUsdt(java.math.BigDecimal assistValueUsdt)
    {
        this.assistValueUsdt = assistValueUsdt;
    }

    public Integer getPrincipalReturnDays()
    {
        return principalReturnDays;
    }

    public void setPrincipalReturnDays(Integer principalReturnDays)
    {
        this.principalReturnDays = principalReturnDays;
    }

    public String getIncomeMode()
    {
        return incomeMode;
    }

    public void setIncomeMode(String incomeMode)
    {
        this.incomeMode = incomeMode;
    }

    public boolean accumulateIncome()
    {
        return "ACCUMULATE".equalsIgnoreCase(incomeMode);
    }

    public Integer getAccumulateCycleDays()
    {
        return accumulateCycleDays;
    }

    public void setAccumulateCycleDays(Integer accumulateCycleDays)
    {
        this.accumulateCycleDays = accumulateCycleDays;
    }

    public Long getRelatedProductId()
    {
        return relatedProductId;
    }

    public void setRelatedProductId(Long relatedProductId)
    {
        this.relatedProductId = relatedProductId;
    }

    public String getRelatedProductName()
    {
        return relatedProductName;
    }

    public void setRelatedProductName(String relatedProductName)
    {
        this.relatedProductName = relatedProductName;
    }

    /**
     * 按认购币种取助力值（仅该币种配置，不做跨币回退）。
     */
    public java.math.BigDecimal resolveAssistValue()
    {
        return assistValueOf(null);
    }

    /**
     * 取指定币种助力值配置；currency 空则取 CNY。不做跨币回退。
     */
    public java.math.BigDecimal assistValueOf(String currency)
    {
        boolean usdt = "USDT".equalsIgnoreCase(currency);
        java.math.BigDecimal preferred = usdt ? assistValueUsdt : assistValueCny;
        if (preferred != null && preferred.compareTo(java.math.BigDecimal.ZERO) > 0)
        {
            return preferred;
        }
        return java.math.BigDecimal.ZERO;
    }

    private static boolean positive(java.math.BigDecimal v)
    {
        return v != null && v.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * 按运营配置的发放模式解析本次认购应发放的助力列表。
     * MATCH=跟认购币；CNY/USDT=固定送该币；BOTH=两侧都送。
     */
    public java.util.List<AssistGrant> resolveAssistGrants(String payCurrency)
    {
        java.util.List<AssistGrant> list = new java.util.ArrayList<AssistGrant>(2);
        String mode = resolveAssistGrantMode();
        if ("BOTH".equals(mode))
        {
            if (positive(assistValueCny))
            {
                list.add(new AssistGrant("CNY", assistValueCny));
            }
            if (positive(assistValueUsdt))
            {
                list.add(new AssistGrant("USDT", assistValueUsdt));
            }
            return list;
        }
        if ("CNY".equals(mode))
        {
            if (positive(assistValueCny))
            {
                list.add(new AssistGrant("CNY", assistValueCny));
            }
            return list;
        }
        if ("USDT".equals(mode))
        {
            if (positive(assistValueUsdt))
            {
                list.add(new AssistGrant("USDT", assistValueUsdt));
            }
            return list;
        }
        // MATCH：认购什么币就发什么币助力
        boolean payUsdt = "USDT".equalsIgnoreCase(payCurrency);
        if (payUsdt)
        {
            if (positive(assistValueUsdt))
            {
                list.add(new AssistGrant("USDT", assistValueUsdt));
            }
        }
        else if (positive(assistValueCny))
        {
            list.add(new AssistGrant("CNY", assistValueCny));
        }
        return list;
    }

    /** @deprecated 改用 {@link #resolveAssistGrants(String)} */
    public AssistGrant resolveAssistGrant(String payCurrency)
    {
        java.util.List<AssistGrant> list = resolveAssistGrants(payCurrency);
        if (list == null || list.isEmpty())
        {
            return new AssistGrant(null, java.math.BigDecimal.ZERO);
        }
        return list.get(0);
    }

    /** 助力发放：入账币种 + 单份数额 */
    public static final class AssistGrant
    {
        private final String currency;
        private final java.math.BigDecimal unit;

        public AssistGrant(String currency, java.math.BigDecimal unit)
        {
            this.currency = currency;
            this.unit = unit == null ? java.math.BigDecimal.ZERO : unit;
        }

        public String getCurrency()
        {
            return currency;
        }

        public java.math.BigDecimal getUnit()
        {
            return unit;
        }

        public boolean valid()
        {
            return currency != null && unit.compareTo(java.math.BigDecimal.ZERO) > 0;
        }
    }

    @ApiModelProperty("是否开售，true 可进详情")
    public Boolean getOnSaleFlag()
    {
        return Boolean.valueOf(saleOpen());
    }

    public boolean saleOpen()
    {
        return onSale == null || "1".equals(onSale);
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

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String templateCode)
    {
        this.templateCode = templateCode;
    }

    public String getLayoutType()
    {
        return layoutType;
    }

    public void setLayoutType(String layoutType)
    {
        this.layoutType = layoutType;
    }

    public String getTheme()
    {
        return theme;
    }

    public void setTheme(String theme)
    {
        this.theme = theme;
    }

    public String getBadgeText()
    {
        return badgeText;
    }

    public void setBadgeText(String badgeText)
    {
        this.badgeText = badgeText;
    }

    public String getCardNo()
    {
        return cardNo;
    }

    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }

    public String getCtaText()
    {
        return ctaText;
    }

    public void setCtaText(String ctaText)
    {
        this.ctaText = ctaText;
    }

    public String getTemplateDefaultCta()
    {
        return templateDefaultCta;
    }

    public void setTemplateDefaultCta(String templateDefaultCta)
    {
        this.templateDefaultCta = templateDefaultCta;
    }

    public String getHasMainAmount()
    {
        return hasMainAmount;
    }

    public void setHasMainAmount(String hasMainAmount)
    {
        this.hasMainAmount = hasMainAmount;
    }

    public String getMainAmountDisplay()
    {
        return mainAmountDisplay;
    }

    public void setMainAmountDisplay(String mainAmountDisplay)
    {
        this.mainAmountDisplay = mainAmountDisplay;
    }

    public List<BizProductCardMetric> getMetrics()
    {
        return metrics;
    }

    public void setMetrics(List<BizProductCardMetric> metrics)
    {
        this.metrics = metrics;
    }
}
