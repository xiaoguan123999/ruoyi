package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("修改密码请求")
public class AppPasswordBody
{
    @ApiModelProperty(value = "原密码", required = true, example = "admin123")
    private String oldPassword;

    @ApiModelProperty(value = "新密码，5-20位", required = true, example = "654321")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码，须与新密码一致", required = true, example = "654321")
    private String confirmPassword;

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword)
    {
        this.confirmPassword = confirmPassword;
    }
}
