package com.ruoyi.biz.domain;

import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销标签人群包
 */
public class BizCrowdPackage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 人群包ID */
    private Long packageId;

    /** 人群包名称 */
    private String packageName;

    /** 0正常 1停用 */
    private String status;

    /** 条件列表（详情/保存） */
    private transient List<BizCrowdCondition> conditions;

    /** 条件数量（列表） */
    private Integer conditionCount;

    public Long getPackageId()
    {
        return packageId;
    }

    public void setPackageId(Long packageId)
    {
        this.packageId = packageId;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<BizCrowdCondition> getConditions()
    {
        return conditions;
    }

    public void setConditions(List<BizCrowdCondition> conditions)
    {
        this.conditions = conditions;
    }

    public Integer getConditionCount()
    {
        return conditionCount;
    }

    public void setConditionCount(Integer conditionCount)
    {
        this.conditionCount = conditionCount;
    }
}
