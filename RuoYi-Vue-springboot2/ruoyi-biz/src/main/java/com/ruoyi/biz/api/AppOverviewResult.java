package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("运行概览响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppOverviewResult extends AppDataResult<java.util.List<AppOverviewItem>>
{
    public static AppOverviewResult ok(java.util.List<AppOverviewItem> data)
    {
        return fillOk(new AppOverviewResult(), data);
    }

    public static AppOverviewResult fail(String message)
    {
        return fillFail(new AppOverviewResult(), message);
    }
}
