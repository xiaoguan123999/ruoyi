package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("等级响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppLevelsResult extends AppDataResult<AppLevelsData>
{
    public static AppLevelsResult ok(AppLevelsData data)
    {
        return fillOk(new AppLevelsResult(), data);
    }

    public static AppLevelsResult fail(String message)
    {
        return fillFail(new AppLevelsResult(), message);
    }
}
