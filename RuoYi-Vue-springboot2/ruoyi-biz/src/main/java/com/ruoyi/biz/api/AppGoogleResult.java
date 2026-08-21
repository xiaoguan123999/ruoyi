package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.ruoyi.biz.domain.GoogleBindInfo;
import io.swagger.annotations.ApiModel;

@ApiModel("谷歌验证响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppGoogleResult extends AppDataResult<GoogleBindInfo>
{
    public static AppGoogleResult ok(GoogleBindInfo data)
    {
        return fillOk(new AppGoogleResult(), data);
    }

    public static AppGoogleResult fail(String message)
    {
        return fillFail(new AppGoogleResult(), message);
    }
}
