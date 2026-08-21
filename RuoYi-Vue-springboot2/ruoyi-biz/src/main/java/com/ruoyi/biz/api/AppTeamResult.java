package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel("团队响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppTeamResult extends AppDataResult<AppTeamData>
{
    public static AppTeamResult ok(AppTeamData data)
    {
        return fillOk(new AppTeamResult(), data);
    }

    public static AppTeamResult fail(String message)
    {
        return fillFail(new AppTeamResult(), message);
    }
}
