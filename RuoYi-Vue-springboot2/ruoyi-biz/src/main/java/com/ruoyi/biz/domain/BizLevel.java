package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会员等级
 */
public class BizLevel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 等级ID */
    private Long levelId;

    /** 等级名称 */
    private String levelName;

    /** 最低有效会员人数 */
    private Integer minValidMembers;

    /** 最低累计充值CNY */
    private BigDecimal minRechargeCny;

    /** 最低累计充值USDT */
    private BigDecimal minRechargeUsdt;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private String status;

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    public Integer getMinValidMembers()
    {
        return minValidMembers;
    }

    public void setMinValidMembers(Integer minValidMembers)
    {
        this.minValidMembers = minValidMembers;
    }

    public BigDecimal getMinRechargeCny()
    {
        return minRechargeCny;
    }

    public void setMinRechargeCny(BigDecimal minRechargeCny)
    {
        this.minRechargeCny = minRechargeCny;
    }

    public BigDecimal getMinRechargeUsdt()
    {
        return minRechargeUsdt;
    }

    public void setMinRechargeUsdt(BigDecimal minRechargeUsdt)
    {
        this.minRechargeUsdt = minRechargeUsdt;
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
