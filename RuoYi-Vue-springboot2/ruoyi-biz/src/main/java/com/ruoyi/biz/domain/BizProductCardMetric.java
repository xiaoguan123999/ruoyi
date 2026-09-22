package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("产品卡片指标槽")
public class BizProductCardMetric
{
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("产品ID")
    private Long productId;

    @ApiModelProperty("坑位，从1开始")
    private Integer slotIndex;

    @ApiModelProperty("展示标签")
    private String label;

    @ApiModelProperty("数据来源：PRICE/DAILY_REBATE/DURATION/BUY_LIMIT/ASSIST_VALUE/CUSTOM")
    private String source;

    @ApiModelProperty("CUSTOM 时文案")
    private String customText;

    @ApiModelProperty("后端组装的展示文案（仅接口下发）")
    private String display;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Integer getSlotIndex()
    {
        return slotIndex;
    }

    public void setSlotIndex(Integer slotIndex)
    {
        this.slotIndex = slotIndex;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getCustomText()
    {
        return customText;
    }

    public void setCustomText(String customText)
    {
        this.customText = customText;
    }

    public String getDisplay()
    {
        return display;
    }

    public void setDisplay(String display)
    {
        this.display = display;
    }
}
