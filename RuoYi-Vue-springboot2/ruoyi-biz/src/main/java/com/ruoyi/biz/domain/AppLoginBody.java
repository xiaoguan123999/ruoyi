package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App登录请求")
public class AppLoginBody
{
    @ApiModelProperty(value = "手机号", required = true, example = "13800000001")
    private String phone;

    @ApiModelProperty(value = "密码", required = true, example = "admin123")
    private String password;

    @ApiModelProperty(value = "验证码", required = true, example = "12")
    private String code;

    @ApiModelProperty(value = "验证码uuid，来自 /app/auth/captcha", required = true)
    private String uuid;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
