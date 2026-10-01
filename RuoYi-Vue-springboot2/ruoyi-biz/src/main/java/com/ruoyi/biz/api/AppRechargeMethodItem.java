package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModelProperty;

public class AppRechargeMethodItem
{
    @ApiModelProperty("方式ID")
    private Long methodId;
    @ApiModelProperty("编码")
    private String methodCode;
    @ApiModelProperty("展示名")
    private String label;
    @ApiModelProperty("图标URL")
    private String iconUrl;
    @ApiModelProperty("是否跳转客服")
    private Boolean isCs;
    @ApiModelProperty("排序")
    private Integer sortOrder;

    public Long getMethodId() { return methodId; }
    public void setMethodId(Long methodId) { this.methodId = methodId; }
    public String getMethodCode() { return methodCode; }
    public void setMethodCode(String methodCode) { this.methodCode = methodCode; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public Boolean getIsCs() { return isCs; }
    public void setIsCs(Boolean isCs) { this.isCs = isCs; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
