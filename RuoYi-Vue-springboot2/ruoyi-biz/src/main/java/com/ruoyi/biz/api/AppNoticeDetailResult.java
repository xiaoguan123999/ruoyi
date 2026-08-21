package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("公告详情响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNoticeDetailResult extends AppDataResult<AppNoticeItem>
{
    public static AppNoticeDetailResult ok(AppNoticeItem data)
    {
        return fillOk(new AppNoticeDetailResult(), data);
    }

    public static AppNoticeDetailResult fail(String message)
    {
        return fillFail(new AppNoticeDetailResult(), message);
    }
}
