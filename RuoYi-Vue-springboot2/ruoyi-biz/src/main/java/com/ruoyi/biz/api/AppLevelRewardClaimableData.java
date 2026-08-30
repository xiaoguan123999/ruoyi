package com.ruoyi.biz.api;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("等级奖励可领列表")
public class AppLevelRewardClaimableData
{
    @ApiModelProperty("当前可领取的等级奖励，没有则为空数组")
    private List<AppLevelRewardClaimItem> items;

    public List<AppLevelRewardClaimItem> getItems() { return items; }
    public void setItems(List<AppLevelRewardClaimItem> items) { this.items = items; }
}
