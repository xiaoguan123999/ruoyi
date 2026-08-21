package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("产品系列详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProductSeriesResult extends AppDataResult<AppProductSeries>
{
    public static AppProductSeriesResult ok(AppProductSeries data)
    {
        return fillOk(new AppProductSeriesResult(), data);
    }

    public static AppProductSeriesResult fail(String message)
    {
        return fillFail(new AppProductSeriesResult(), message);
    }
}
