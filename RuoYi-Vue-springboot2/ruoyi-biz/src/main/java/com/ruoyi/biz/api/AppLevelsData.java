package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.domain.BizMember;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("会员等级页")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLevelsData
{
    @ApiModelProperty("当前会员（含等级名称）")
    private BizMember current;
    @ApiModelProperty("全部启用中的等级配置")
    private List<BizLevel> levels;
    @ApiModelProperty("成长激励金规则说明，App 右上角「规则说明」打开")
    private String ruleText;
    @ApiModelProperty("等级页表格上方注释")
    private String hint;
    @ApiModelProperty("hint 别名")
    private String note;
    @ApiModelProperty("1 USDT 折合多少人民币")
    private java.math.BigDecimal usdtToCny;
    @ApiModelProperty("当前可领取的等级奖励")
    private List<AppLevelRewardClaimItem> claimable;

    public BizMember getCurrent() { return current; }
    public void setCurrent(BizMember current) { this.current = current; }
    public List<BizLevel> getLevels() { return levels; }
    public void setLevels(List<BizLevel> levels) { this.levels = levels; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public java.math.BigDecimal getUsdtToCny() { return usdtToCny; }
    public void setUsdtToCny(java.math.BigDecimal usdtToCny) { this.usdtToCny = usdtToCny; }
    public List<AppLevelRewardClaimItem> getClaimable() { return claimable; }
    public void setClaimable(List<AppLevelRewardClaimItem> claimable) { this.claimable = claimable; }
}
