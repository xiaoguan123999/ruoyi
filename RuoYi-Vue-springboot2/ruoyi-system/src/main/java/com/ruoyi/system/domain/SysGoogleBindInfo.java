package com.ruoyi.system.domain;

public class SysGoogleBindInfo
{
    private Boolean bound;
    private Boolean enabled;
    private String secret;
    private String otpauthUrl;
    private String issuer;

    public Boolean getBound() { return bound; }
    public void setBound(Boolean bound) { this.bound = bound; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getOtpauthUrl() { return otpauthUrl; }
    public void setOtpauthUrl(String otpauthUrl) { this.otpauthUrl = otpauthUrl; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
