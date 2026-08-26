package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class AppPayDepositData
{
    private String outTradeNo;
    private Long rechargeId;
    private String payUrl;
    private String payType;
    private BigDecimal amount;
    private String currency;
    private String channelCode;
    private String channelName;
    private String providerCode;
    @ApiModelProperty("true 表示模拟收银台，不是真实三方")
    private Boolean mock;
    private String expireTime;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public Boolean getMock() { return mock; }
    public void setMock(Boolean mock) { this.mock = mock; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
}
