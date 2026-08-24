package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("实名注册奖励领取响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPromoClaimResult extends AppDataResult<AppPromoClaimData>
{
    public static AppPromoClaimResult ok(AppPromoClaimData data)
    {
        return fillOk(new AppPromoClaimResult(), data);
    }

    public static AppPromoClaimResult fail(String message)
    {
        return fillFail(new AppPromoClaimResult(), message);
    }
}
