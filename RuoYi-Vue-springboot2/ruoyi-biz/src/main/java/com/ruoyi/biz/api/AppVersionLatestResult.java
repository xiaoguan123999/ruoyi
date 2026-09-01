package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("最新版本响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppVersionLatestResult extends AppDataResult<AppVersionLatestData>
{
    public static AppVersionLatestResult ok(AppVersionLatestData data)
    {
        return fillOk(new AppVersionLatestResult(), data);
    }

    public static AppVersionLatestResult fail(String message)
    {
        return fillFail(new AppVersionLatestResult(), message);
    }
}
