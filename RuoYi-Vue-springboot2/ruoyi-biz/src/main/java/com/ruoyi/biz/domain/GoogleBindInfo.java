package com.ruoyi.biz.domain;

public class GoogleBindInfo
{
    private Boolean bound;
    private Boolean enabled;
    private Boolean requireWithdraw;
    private String secret;
    private String otpauthUrl;
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
