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
    @ApiModelProperty("全部等级配置")
    private List<BizLevel> levels;

    public BizMember getCurrent() { return current; }
    public void setCurrent(BizMember current) { this.current = current; }
    public List<BizLevel> getLevels() { return levels; }
    public void setLevels(List<BizLevel> levels) { this.levels = levels; }
}
