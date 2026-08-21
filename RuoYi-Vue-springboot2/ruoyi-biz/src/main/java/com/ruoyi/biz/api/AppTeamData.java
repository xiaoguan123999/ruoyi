package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import com.ruoyi.biz.domain.BizMember;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("我的团队")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppTeamData
{
    @ApiModelProperty("一级下线")
    private List<BizMember> level1;
    @ApiModelProperty("二级下线")
    private List<BizMember> level2;
    @ApiModelProperty("三级下线")
    private List<BizMember> level3;

    public List<BizMember> getLevel1() { return level1; }
    public void setLevel1(List<BizMember> level1) { this.level1 = level1; }
    public List<BizMember> getLevel2() { return level2; }
    public void setLevel2(List<BizMember> level2) { this.level2 = level2; }
    public List<BizMember> getLevel3() { return level3; }
    public void setLevel3(List<BizMember> level3) { this.level3 = level3; }
}
