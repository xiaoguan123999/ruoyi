package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("团队1-7级汇总")
public class AppTeamSummary
{
    private AppTeamLevelStats level1;
    private AppTeamLevelStats level2;
    private AppTeamLevelStats level3;
    private AppTeamLevelStats level4;
    private AppTeamLevelStats level5;
    private AppTeamLevelStats level6;
    private AppTeamLevelStats level7;

    public AppTeamLevelStats getLevel1() { return level1; }
    public void setLevel1(AppTeamLevelStats level1) { this.level1 = level1; }
    public AppTeamLevelStats getLevel2() { return level2; }
    public void setLevel2(AppTeamLevelStats level2) { this.level2 = level2; }
    public AppTeamLevelStats getLevel3() { return level3; }
    public void setLevel3(AppTeamLevelStats level3) { this.level3 = level3; }
    public AppTeamLevelStats getLevel4() { return level4; }
    public void setLevel4(AppTeamLevelStats level4) { this.level4 = level4; }
    public AppTeamLevelStats getLevel5() { return level5; }
    public void setLevel5(AppTeamLevelStats level5) { this.level5 = level5; }
    public AppTeamLevelStats getLevel6() { return level6; }
    public void setLevel6(AppTeamLevelStats level6) { this.level6 = level6; }
    public AppTeamLevelStats getLevel7() { return level7; }
    public void setLevel7(AppTeamLevelStats level7) { this.level7 = level7; }
}
