package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("谷歌验证码")
public class AppGoogleCodeBody
{
    @ApiModelProperty(value = "谷歌验证器6位数字", required = true, example = "123456")
    private String googleCode;

    public String getGoogleCode()
    {
        return googleCode;
    }

    public void setGoogleCode(String googleCode)
    {
        this.googleCode = googleCode;
    }
}
