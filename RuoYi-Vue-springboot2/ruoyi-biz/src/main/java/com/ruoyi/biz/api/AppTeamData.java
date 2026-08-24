package com.ruoyi.biz.api;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("我的团队")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppTeamData
{
    @ApiModelProperty("1-7级汇总")
    private AppTeamSummary summary;
    @ApiModelProperty("按层级成员，key 为 1-7")
    private Map<String, List<AppTeamMemberItem>> members;
    private List<AppTeamMemberItem> level1;
    private List<AppTeamMemberItem> level2;
    private List<AppTeamMemberItem> level3;
    private List<AppTeamMemberItem> level4;
    private List<AppTeamMemberItem> level5;
    private List<AppTeamMemberItem> level6;
    private List<AppTeamMemberItem> level7;
    private List<AppTeamMemberItem> level1Members;
    private List<AppTeamMemberItem> members1;

    public AppTeamSummary getSummary() { return summary; }
    public void setSummary(AppTeamSummary summary) { this.summary = summary; }
    public Map<String, List<AppTeamMemberItem>> getMembers() { return members; }
    public void setMembers(Map<String, List<AppTeamMemberItem>> members) { this.members = members; }
    public List<AppTeamMemberItem> getLevel1() { return level1; }
    public void setLevel1(List<AppTeamMemberItem> level1) { this.level1 = level1; }
    public List<AppTeamMemberItem> getLevel2() { return level2; }
    public void setLevel2(List<AppTeamMemberItem> level2) { this.level2 = level2; }
    public List<AppTeamMemberItem> getLevel3() { return level3; }
    public void setLevel3(List<AppTeamMemberItem> level3) { this.level3 = level3; }
    public List<AppTeamMemberItem> getLevel4() { return level4; }
    public void setLevel4(List<AppTeamMemberItem> level4) { this.level4 = level4; }
    public List<AppTeamMemberItem> getLevel5() { return level5; }
    public void setLevel5(List<AppTeamMemberItem> level5) { this.level5 = level5; }
    public List<AppTeamMemberItem> getLevel6() { return level6; }
    public void setLevel6(List<AppTeamMemberItem> level6) { this.level6 = level6; }
    public List<AppTeamMemberItem> getLevel7() { return level7; }
    public void setLevel7(List<AppTeamMemberItem> level7) { this.level7 = level7; }
    public List<AppTeamMemberItem> getLevel1Members() { return level1Members; }
    public void setLevel1Members(List<AppTeamMemberItem> level1Members) { this.level1Members = level1Members; }
    public List<AppTeamMemberItem> getMembers1() { return members1; }
    public void setMembers1(List<AppTeamMemberItem> members1) { this.members1 = members1; }
}
