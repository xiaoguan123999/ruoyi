package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("实名认证请求")
public class AppKycBody
{
    @ApiModelProperty(value = "真实姓名，2-20个中文，可含·", required = true, example = "张三")
    private String realName;

    @ApiModelProperty(value = "18位身份证号，不可与其他App会员重复", required = true, example = "110101199001011237")
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
