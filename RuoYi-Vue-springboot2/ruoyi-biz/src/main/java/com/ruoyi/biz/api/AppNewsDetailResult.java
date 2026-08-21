package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("新闻详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNewsDetailResult extends AppDataResult<AppNewsItem>
{
    public static AppNewsDetailResult ok(AppNewsItem data)
    {
        return fillOk(new AppNewsDetailResult(), data);
    }

    public static AppNewsDetailResult fail(String message)
    {
        return fillFail(new AppNewsDetailResult(), message);
    }
}
