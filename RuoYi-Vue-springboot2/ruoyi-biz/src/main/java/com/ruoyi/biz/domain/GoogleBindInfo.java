package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("谷歌验证器信息")
public class GoogleBindInfo
{
    @ApiModelProperty("当前会员是否已绑定")
    private Boolean bound;
    @ApiModelProperty("后台是否开启谷歌验证")
    private Boolean enabled;
    @ApiModelProperty("提现是否强制先绑定")
    private Boolean requireWithdraw;
    @ApiModelProperty("密钥，仅开始绑定时返回，确认后不再返回")
    private String secret;
    @ApiModelProperty("otpauth 链接，给 Google Authenticator 扫码，仅开始绑定时返回")
    private String otpauthUrl;
    @ApiModelProperty("验证器里显示的名称")
    private String issuer;

    public Boolean getBound() { return bound; }
    public void setBound(Boolean bound) { this.bound = bound; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Boolean getRequireWithdraw() { return requireWithdraw; }
    public void setRequireWithdraw(Boolean requireWithdraw) { this.requireWithdraw = requireWithdraw; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getOtpauthUrl() { return otpauthUrl; }
    public void setOtpauthUrl(String otpauthUrl) { this.otpauthUrl = otpauthUrl; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
