package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("新闻列表响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNewsListResult extends AppDataResult<java.util.List<AppNewsItem>>
{
    public static AppNewsListResult ok(java.util.List<AppNewsItem> data)
    {
        return fillOk(new AppNewsListResult(), data);
    }

    public static AppNewsListResult fail(String message)
    {
        return fillFail(new AppNewsListResult(), message);
    }
}
