package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("注册推广规则响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppPromoResult extends AppDataResult<AppPromoData>
{
    public static AppPromoResult ok(AppPromoData data)
    {
        return fillOk(new AppPromoResult(), data);
    }

    public static AppPromoResult fail(String message)
    {
        return fillFail(new AppPromoResult(), message);
    }
}
