package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.CheckinResult;
import io.swagger.annotations.ApiModel;

@ApiModel("签到响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCheckinResult extends AppDataResult<CheckinResult>
{
    public static AppCheckinResult ok(CheckinResult data)
    {
        return fillOk(new AppCheckinResult(), data);
    }

    public static AppCheckinResult fail(String message)
    {
        return fillFail(new AppCheckinResult(), message);
    }
}
