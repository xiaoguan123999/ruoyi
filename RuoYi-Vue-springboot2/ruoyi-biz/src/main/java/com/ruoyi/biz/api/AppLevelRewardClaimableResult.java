package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("等级奖励可领响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLevelRewardClaimableResult extends AppDataResult<AppLevelRewardClaimableData>
{
    public static AppLevelRewardClaimableResult ok(AppLevelRewardClaimableData data)
    {
        return fillOk(new AppLevelRewardClaimableResult(), data);
    }
}
