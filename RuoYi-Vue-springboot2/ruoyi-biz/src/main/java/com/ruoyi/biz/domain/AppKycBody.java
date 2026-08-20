package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("实名认证请求")
public class AppKycBody
{
    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    private String realName;

    @ApiModelProperty(value = "身份证号", required = true, example = "110101199001011234")
    private String idCard;

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }
}
