package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("等级奖励领取响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLevelRewardClaimResult extends AppDataResult<AppLevelRewardClaimData>
{
    public static AppLevelRewardClaimResult ok(AppLevelRewardClaimData data)
    {
        return fillOk(new AppLevelRewardClaimResult(), data);
    }
}
