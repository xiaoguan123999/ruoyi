package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销人群包原子条件
 */
public class BizCrowdCondition extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 条件ID */
    private Long conditionId;

    /** 所属人群包ID */
    private Long packageId;

    /** 标签维度 IMAGE/TRANSACTION/ACTION */
    private String labelType;

    /** 标签字段 */
    private String labelField;

    /** 运算符 EQUALS/GREATER_THAN/LESS_THAN */
    private String operatorType;

    /** 限制值 */
    private String ruleValue;

    /** 排序 */
    private Integer sort;

    public Long getConditionId()
    {
        return conditionId;
    }

    public void setConditionId(Long conditionId)
    {
        this.conditionId = conditionId;
    }

    public Long getPackageId()
    {
        return packageId;
    }

    public void setPackageId(Long packageId)
    {
        this.packageId = packageId;
    }

    public String getLabelType()
    {
        return labelType;
    }

    public void setLabelType(String labelType)
    {
        this.labelType = labelType;
    }

    public String getLabelField()
    {
        return labelField;
    }

    public void setLabelField(String labelField)
    {
        this.labelField = labelField;
    }

    public String getOperatorType()
    {
        return operatorType;
    }

    public void setOperatorType(String operatorType)
    {
        this.operatorType = operatorType;
    }

    public String getRuleValue()
    {
        return ruleValue;
    }

    public void setRuleValue(String ruleValue)
    {
        this.ruleValue = ruleValue;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }
}
