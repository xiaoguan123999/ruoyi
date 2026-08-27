package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("关于我们响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppAboutResult extends AppDataResult<AppAboutItem>
{
    public static AppAboutResult ok(AppAboutItem data)
    {
        return fillOk(new AppAboutResult(), data);
    }

    public static AppAboutResult fail(String message)
    {
        return fillFail(new AppAboutResult(), message);
    }
}
