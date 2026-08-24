package com.ruoyi.biz.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("设置或修改支付密码")
public class AppPayPasswordBody
{
    @ApiModelProperty(value = "原支付密码，已设置过则必填", example = "123456")
    @JsonAlias({"oldPayPassword", "oldTradePassword"})
    private String oldPassword;

    @ApiModelProperty(value = "新支付密码，4-20位", required = true, example = "123456")
    @JsonAlias({"payPassword", "tradePassword", "fundPassword", "payPwd"})
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", example = "123456")
    @JsonAlias({"confirmPayPassword", "confirmTradePassword"})
    private String confirmPassword;

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
