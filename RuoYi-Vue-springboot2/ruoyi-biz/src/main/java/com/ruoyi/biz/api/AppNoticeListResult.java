package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("公告列表响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppNoticeListResult extends AppDataResult<java.util.List<AppNoticeItem>>
{
    public static AppNoticeListResult ok(java.util.List<AppNoticeItem> data)
    {
        return fillOk(new AppNoticeListResult(), data);
    }

    public static AppNoticeListResult fail(String message)
    {
        return fillFail(new AppNoticeListResult(), message);
    }
}
