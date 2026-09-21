package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class BizChainDepositConfig
{
    @ApiModelProperty("TRC20 开关")
    private Boolean tronEnabled;
    @ApiModelProperty("系统默认 TRC20 收款地址")
    private String tronAddress;
    @ApiModelProperty("TronGrid API Key")
    private String tronApiKey;

    @ApiModelProperty("BEP20 开关")
    private Boolean bscEnabled;
    @ApiModelProperty("系统默认 BEP20 收款地址")
    private String bscAddress;
    @ApiModelProperty("BscScan / Etherscan API Key")
    private String bscApiKey;
    @ApiModelProperty("可空。空=https://api.bscscan.com/api")
    private String bscApiUrl;

    private Integer expireMinutes;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    @ApiModelProperty("true 时不扫链，只能后台模拟到账")
    private Boolean mock;
    private String hint;

    public Boolean getTronEnabled() { return tronEnabled; }
    public void setTronEnabled(Boolean tronEnabled) { this.tronEnabled = tronEnabled; }
    public String getTronAddress() { return tronAddress; }
    public void setTronAddress(String tronAddress) { this.tronAddress = tronAddress; }
    public String getTronApiKey() { return tronApiKey; }
    public void setTronApiKey(String tronApiKey) { this.tronApiKey = tronApiKey; }
    public Boolean getBscEnabled() { return bscEnabled; }
    public void setBscEnabled(Boolean bscEnabled) { this.bscEnabled = bscEnabled; }
    public String getBscAddress() { return bscAddress; }
    public void setBscAddress(String bscAddress) { this.bscAddress = bscAddress; }
    public String getBscApiKey() { return bscApiKey; }
    public void setBscApiKey(String bscApiKey) { this.bscApiKey = bscApiKey; }
    public String getBscApiUrl() { return bscApiUrl; }
    public void setBscApiUrl(String bscApiUrl) { this.bscApiUrl = bscApiUrl; }
    public Integer getExpireMinutes() { return expireMinutes; }
    public void setExpireMinutes(Integer expireMinutes) { this.expireMinutes = expireMinutes; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Boolean getMock() { return mock; }
    public void setMock(Boolean mock) { this.mock = mock; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
}
