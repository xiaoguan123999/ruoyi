package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 抽奖奖品池（独立管理）
 */
public class BizLotteryPrizePool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long poolId;
    private String prizeName;
    private String prizeDesc;
    private String imageUrl;
    private String sectorBgColor;
    private String nameColor;
    private String descColor;
    private Integer prizeType;
    private BigDecimal assistAmount;
    private String currency;
    private Integer sort;
    private String status;

    public Long getPoolId() { return poolId; }
    public void setPoolId(Long poolId) { this.poolId = poolId; }
    public String getPrizeName() { return prizeName; }
    public void setPrizeName(String prizeName) { this.prizeName = prizeName; }
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
    public Integer getPrizeType() { return prizeType; }
    public void setPrizeType(Integer prizeType) { this.prizeType = prizeType; }
    public BigDecimal getAssistAmount() { return assistAmount; }
    public void setAssistAmount(BigDecimal assistAmount) { this.assistAmount = assistAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
