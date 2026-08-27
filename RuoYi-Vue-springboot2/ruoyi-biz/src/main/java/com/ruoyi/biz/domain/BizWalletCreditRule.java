package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("奖励入账钱包配置")
public class BizWalletCreditRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("规则ID")
    private Long ruleId;

    @ApiModelProperty("业务类型，如 CHECKIN")
    private String bizType;

    @ApiModelProperty("业务名称")
    private String bizName;

    @ApiModelProperty("入账钱包类型编码")
    private String typeCode;

    @ApiModelProperty("入账钱包名称，列表回显")
    private String typeName;

    @ApiModelProperty("1内置不可删除")
    private String builtin;

    @ApiModelProperty("排序")
    private Integer sort;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizName() { return bizName; }
    public void setBizName(String bizName) { this.bizName = bizName; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getBuiltin() { return builtin; }
    public void setBuiltin(String builtin) { this.builtin = builtin; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
