package com.ruoyi.biz.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

@ApiModel("产品系列列表响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppProductSeriesListResult extends AppDataResult<List<AppProductSeries>>
{
    public static AppProductSeriesListResult ok(List<AppProductSeries> data)
    {
        return fillOk(new AppProductSeriesListResult(), data);
    }

    public static AppProductSeriesListResult fail(String message)
    {
        return fillFail(new AppProductSeriesListResult(), message);
    }
}
