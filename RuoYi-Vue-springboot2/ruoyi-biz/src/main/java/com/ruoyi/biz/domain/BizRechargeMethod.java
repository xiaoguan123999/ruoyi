package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("充值方式")
public class BizRechargeMethod extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("方式ID")
    private Long methodId;
    @ApiModelProperty("编码")
    private String methodCode;
    @ApiModelProperty("展示名")
    private String label;
    @ApiModelProperty("图标URL")
    private String iconUrl;
    @ApiModelProperty("是否客服 0否 1是")
    private String isCs;
    @ApiModelProperty("0启用 1停用")
    private String status;
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
    public String getIsCs() { return isCs; }
    public void setIsCs(String isCs) { this.isCs = isCs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
