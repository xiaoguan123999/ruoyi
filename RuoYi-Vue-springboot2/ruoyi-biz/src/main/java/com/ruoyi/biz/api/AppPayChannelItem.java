package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class AppPayChannelItem
{
    @ApiModelProperty("通道编码")
    private String channelCode;
    @ApiModelProperty("展示名")
    private String name;
    @ApiModelProperty("场景 alipay/wechat/union/usdt")
    private String scene;
    @ApiModelProperty("服务商")
    private String providerCode;
    @ApiModelProperty("服务商名")
    private String providerName;
    @ApiModelProperty("入账币种")
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    @ApiModelProperty("是否模拟通道")
    private Boolean mock;

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Boolean getMock() { return mock; }
    public void setMock(Boolean mock) { this.mock = mock; }
}
