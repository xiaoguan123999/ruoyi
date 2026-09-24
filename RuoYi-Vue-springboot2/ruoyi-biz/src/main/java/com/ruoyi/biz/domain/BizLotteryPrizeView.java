package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * App 奖品扇区视图（隐藏库存）
 */
@ApiModel("大转盘奖品扇区")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BizLotteryPrizeView
{
    @ApiModelProperty("奖品ID")
    private Long prizeId;

    @ApiModelProperty("奖品名称")
    private String prizeName;

    @ApiModelProperty("奖品说明")
    private String prizeDesc;

    @ApiModelProperty("奖品图片")
    private String imageUrl;

    @ApiModelProperty("扇区背景色")
    private String sectorBgColor;

    @ApiModelProperty("名称字体颜色")
    private String nameColor;

    @ApiModelProperty("说明字体颜色")
    private String descColor;

    @ApiModelProperty("1实物 2现金 3虚拟")
    private Integer prizeType;

    @ApiModelProperty("转盘排序/扇区序号，从1起可扩展")
    private Integer position;

    @ApiModelProperty("是否兜底奖 0否 1是")
    private String isFallback;

    public Long getPrizeId() { return prizeId; }
    public void setPrizeId(Long prizeId) { this.prizeId = prizeId; }
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
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public String getIsFallback() { return isFallback; }
    public void setIsFallback(String isFallback) { this.isFallback = isFallback; }
}
