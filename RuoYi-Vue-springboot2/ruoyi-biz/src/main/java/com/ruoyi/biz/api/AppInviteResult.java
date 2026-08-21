package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("邀请信息响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppInviteResult extends AppDataResult<AppInviteData>
{
    public static AppInviteResult ok(AppInviteData data)
    {
        return fillOk(new AppInviteResult(), data);
    }

    public static AppInviteResult fail(String message)
    {
        return fillFail(new AppInviteResult(), message);
    }
}
