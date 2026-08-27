package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 会员等级
 */
@ApiModel("会员等级")
public class BizLevel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 等级ID */
    @ApiModelProperty("等级ID")
    private Long levelId;

    /** 等级名称 */
    @ApiModelProperty("等级名称")
    private String levelName;

    /** 最低有效会员人数 */
    @ApiModelProperty("最低有效会员人数")
    private Integer minValidMembers;

    @ApiModelProperty("团队要求，App等级表展示")
    private String teamDepth;

    /** 最低累计充值CNY */
    @ApiModelProperty("最低累计充值CNY")
    private BigDecimal minRechargeCny;

    /** 最低累计充值USDT */
    @ApiModelProperty("最低累计充值USDT")
    private BigDecimal minRechargeUsdt;

    /** 排序 */
    @ApiModelProperty("排序")
    private Integer sort;

    /** 状态 */
    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("最低团队业绩CNY，0不限制")
    private java.math.BigDecimal minTeamPerfCny;

    @ApiModelProperty("最低团队业绩USDT，0不限制")
    private java.math.BigDecimal minTeamPerfUsdt;

    @ApiModelProperty("是否启用该等级奖励：1是 0否")
    private String rewardEnabled;

    @ApiModelProperty("奖励周期：NONE/ONCE/MONTHLY/PERMANENT")
    private String rewardCycle;

    @ApiModelProperty("发放方式：AUTO自动 MANUAL客服")
    private String rewardMode;

    @ApiModelProperty("永久档重复领取：NONE/MONTHLY/UNLIMITED")
    private String rewardRepeat;

    @ApiModelProperty("奖励金额CNY")
    private java.math.BigDecimal rewardCny;

    @ApiModelProperty("奖励金额USDT")
    private java.math.BigDecimal rewardUsdt;

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

    public String getTeamDepth()
    {
        return teamDepth;
    }

    public void setTeamDepth(String teamDepth)
    {
        this.teamDepth = teamDepth;
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

    public java.math.BigDecimal getMinTeamPerfCny() { return minTeamPerfCny; }
    public void setMinTeamPerfCny(java.math.BigDecimal minTeamPerfCny) { this.minTeamPerfCny = minTeamPerfCny; }
    public java.math.BigDecimal getMinTeamPerfUsdt() { return minTeamPerfUsdt; }
    public void setMinTeamPerfUsdt(java.math.BigDecimal minTeamPerfUsdt) { this.minTeamPerfUsdt = minTeamPerfUsdt; }
    public String getRewardEnabled() { return rewardEnabled; }
    public void setRewardEnabled(String rewardEnabled) { this.rewardEnabled = rewardEnabled; }
    public String getRewardCycle() { return rewardCycle; }
    public void setRewardCycle(String rewardCycle) { this.rewardCycle = rewardCycle; }
    public String getRewardMode() { return rewardMode; }
    public void setRewardMode(String rewardMode) { this.rewardMode = rewardMode; }
    public String getRewardRepeat() { return rewardRepeat; }
    public void setRewardRepeat(String rewardRepeat) { this.rewardRepeat = rewardRepeat; }
    public java.math.BigDecimal getRewardCny() { return rewardCny; }
    public void setRewardCny(java.math.BigDecimal rewardCny) { this.rewardCny = rewardCny; }
    public java.math.BigDecimal getRewardUsdt() { return rewardUsdt; }
    public void setRewardUsdt(java.math.BigDecimal rewardUsdt) { this.rewardUsdt = rewardUsdt; }

}
