package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("钱包类型")
public class BizWalletType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("类型ID")
    private Long typeId;

    @ApiModelProperty("类型编码，如 BALANCE")
    private String typeCode;

    @ApiModelProperty("类型名称，如 余额")
    private String typeName;

    @ApiModelProperty("提现规则 NONE/OPEN/ANY_ORDER/PRODUCT_REQUIRED")
    private String withdrawMode;

    @ApiModelProperty("0正常 1停用")
    private String status;

    @ApiModelProperty("排序，越小越靠前")
    private Integer sort;

    @ApiModelProperty("1内置不可删改编码")
    private String builtin;

    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getWithdrawMode() { return withdrawMode; }
    public void setWithdrawMode(String withdrawMode) { this.withdrawMode = withdrawMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getBuiltin() { return builtin; }
    public void setBuiltin(String builtin) { this.builtin = builtin; }
}
