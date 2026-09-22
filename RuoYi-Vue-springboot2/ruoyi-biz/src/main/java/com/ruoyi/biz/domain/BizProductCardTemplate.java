package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("产品卡片布局模板")
public class BizProductCardTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模板ID")
    private Long templateId;

    @ApiModelProperty("App编码")
    private String templateCode;

    @ApiModelProperty("运营名称")
    private String templateName;

    @ApiModelProperty("预览图")
    private String previewUrl;

    @ApiModelProperty("指标坑数量")
    private Integer metricSlotCount;

    @ApiModelProperty("是否有大号主金额区：1是 0否")
    private String hasMainAmount;

    @ApiModelProperty("默认按钮文案")
    private String defaultCtaText;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("0启用 1停用")
    private String status;

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String templateCode)
    {
        this.templateCode = templateCode;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getPreviewUrl()
    {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl)
    {
        this.previewUrl = previewUrl;
    }

    public Integer getMetricSlotCount()
    {
        return metricSlotCount;
    }

    public void setMetricSlotCount(Integer metricSlotCount)
    {
        this.metricSlotCount = metricSlotCount;
    }

    public String getHasMainAmount()
    {
        return hasMainAmount;
    }

    public void setHasMainAmount(String hasMainAmount)
    {
        this.hasMainAmount = hasMainAmount;
    }

    public String getDefaultCtaText()
    {
        return defaultCtaText;
    }

    public void setDefaultCtaText(String defaultCtaText)
    {
        this.defaultCtaText = defaultCtaText;
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
