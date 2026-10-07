package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("确认请求")
public class BizConfirmBody
{
    @ApiModelProperty(value = "必须 true 才执行", required = true)
    private Boolean confirm;

    public Boolean getConfirm()
    {
        return confirm;
    }

    public void setConfirm(Boolean confirm)
    {
        this.confirm = confirm;
    }
}
