package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("登录/注册验证码")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCaptchaResult extends AppOkResult
{
    @ApiModelProperty(value = "验证码uuid，登录注册时原样带回", example = "a1b2c3")
    private String uuid;
    @ApiModelProperty(value = "4位数字，直接展示在输入框右侧，用户照着填写", example = "1234")
    private String text;
    @ApiModelProperty(value = "是否启用验证码，当前恒为 true")
    private Boolean captchaEnabled;

    public static AppCaptchaResult of(String uuid, String text)
    {
        AppCaptchaResult r = new AppCaptchaResult();
        r.setCode(Integer.valueOf(200));
        r.setMsg("操作成功");
        r.uuid = uuid;
        r.text = text;
        r.captchaEnabled = Boolean.TRUE;
        return r;
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Boolean getCaptchaEnabled() { return captchaEnabled; }
    public void setCaptchaEnabled(Boolean captchaEnabled) { this.captchaEnabled = captchaEnabled; }
}
