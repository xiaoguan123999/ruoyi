package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 大转盘奖品
 */
public class BizLotteryPrize extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 奖品ID */
    private Long prizeId;

    /** 活动ID */
    private Long activityId;

    /** 奖品池ID */
    private Long poolId;

    /** 奖品名称 */
    private String prizeName;

    private String prizeDesc;
    private String imageUrl;
    private String sectorBgColor;
    private String nameColor;
    private String descColor;

    /** 1实物 2现金 3虚拟 */
    private Integer prizeType;

    /** 公海库存 -1无限(仅虚拟) */
    private Integer publicStock;

    /** 专属库存 -1无限(仅虚拟) */
    private Integer exclusiveStock;

    /** 公海中奖概率(万分制) */
    private Integer probability;

    /** 转盘排序/扇区序号，从1起可扩展 */
    private Integer position;

    /** 是否兜底奖 0否 1是 */
    private String isFallback;

    /** 虚拟助力值额度 */
    private BigDecimal assistAmount;

    /** 虚拟入账币种 */
    private String currency;

    /** 排序 */
    private Integer sort;

    /** 0正常 1停用 */
    private String status;

    public Long getPrizeId()
    {
        return prizeId;
    }

    public void setPrizeId(Long prizeId)
    {
        this.prizeId = prizeId;
    }

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Long getPoolId()
    {
        return poolId;
    }

    public void setPoolId(Long poolId)
    {
        this.poolId = poolId;
    }

    public String getPrizeName()
    {
        return prizeName;
    }

    public void setPrizeName(String prizeName)
    {
        this.prizeName = prizeName;
    }

    public String getPrizeDesc() { return prizeDesc; }
    public void setPrizeDesc(String prizeDesc) { this.prizeDesc = prizeDesc; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getSectorBgColor() { return sectorBgColor; }
    public void setSectorBgColor(String sectorBgColor) { this.sectorBgColor = sectorBgColor; }
    public String getNameColor() { return nameColor; }
    public void setNameColor(String nameColor) { this.nameColor = nameColor; }
    public String getDescColor() { return descColor; }
    public void setDescColor(String descColor) { this.descColor = descColor; }

    public Integer getPrizeType()
    {
        return prizeType;
    }

    public void setPrizeType(Integer prizeType)
    {
        this.prizeType = prizeType;
    }

    public Integer getPublicStock()
    {
        return publicStock;
    }

    public void setPublicStock(Integer publicStock)
    {
        this.publicStock = publicStock;
    }

    public Integer getExclusiveStock()
    {
        return exclusiveStock;
    }

    public void setExclusiveStock(Integer exclusiveStock)
    {
        this.exclusiveStock = exclusiveStock;
    }

    public Integer getProbability()
    {
        return probability;
    }

    public void setProbability(Integer probability)
    {
        this.probability = probability;
    }

    public Integer getPosition()
    {
        return position;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public String getIsFallback()
    {
        return isFallback;
    }

    public void setIsFallback(String isFallback)
    {
        this.isFallback = isFallback;
    }

    public BigDecimal getAssistAmount()
    {
        return assistAmount;
    }

    public void setAssistAmount(BigDecimal assistAmount)
    {
        this.assistAmount = assistAmount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
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
