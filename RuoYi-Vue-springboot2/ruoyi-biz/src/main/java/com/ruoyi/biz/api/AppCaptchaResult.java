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
    @ApiModelProperty(value = "PNG 图片 base64，不含 data:image 前缀。显示时拼 data:image/png;base64,")
    private String img;
    @ApiModelProperty(value = "图片类型", example = "png")
    private String imgType;
    @ApiModelProperty(value = "是否启用验证码，当前恒为 true")
    private Boolean captchaEnabled;

    public static AppCaptchaResult of(String uuid, String img)
    {
        AppCaptchaResult r = new AppCaptchaResult();
        r.setCode(Integer.valueOf(200));
        r.setMsg("操作成功");
        r.uuid = uuid;
        r.img = img;
        r.imgType = "png";
        r.captchaEnabled = Boolean.TRUE;
        return r;
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
    public String getImgType() { return imgType; }
    public void setImgType(String imgType) { this.imgType = imgType; }
    public Boolean getCaptchaEnabled() { return captchaEnabled; }
    public void setCaptchaEnabled(Boolean captchaEnabled) { this.captchaEnabled = captchaEnabled; }
}
