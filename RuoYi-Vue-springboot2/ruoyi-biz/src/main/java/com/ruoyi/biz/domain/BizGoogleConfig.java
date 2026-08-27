package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App谷歌验证配置")
public class BizGoogleConfig
{
    @ApiModelProperty("是否开启App谷歌验证绑定")
    private Boolean enabled;
    @ApiModelProperty("谷歌验证器中显示的名称")
    private String issuer;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
