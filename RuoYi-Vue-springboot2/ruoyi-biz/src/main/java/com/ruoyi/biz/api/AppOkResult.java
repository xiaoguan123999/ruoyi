package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "统一响应壳。失败时没有 data，看 msg")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppOkResult
{
    @ApiModelProperty(value = "状态码：200成功，401未登录/token失效，500失败", example = "200", position = 1)
    private Integer code;

    @ApiModelProperty(value = "提示文案。失败原因、校验错误都看这里", example = "操作成功", position = 2)
    private String msg;

    public static AppOkResult ok()
    {
        return of(200, "操作成功");
    }

    public static AppOkResult fail(String message)
    {
        return of(500, message);
    }

    public static AppOkResult of(int code, String message)
    {
        AppOkResult r = new AppOkResult();
        r.code = Integer.valueOf(code);
        r.msg = message;
        return r;
    }

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
}
