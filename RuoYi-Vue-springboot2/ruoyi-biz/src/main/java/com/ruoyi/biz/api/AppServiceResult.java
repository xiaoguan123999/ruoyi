package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("客服中心响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppServiceResult extends AppDataResult<AppServiceData>
{
    public static AppServiceResult ok(AppServiceData data)
    {
        return fillOk(new AppServiceResult(), data);
    }
}
