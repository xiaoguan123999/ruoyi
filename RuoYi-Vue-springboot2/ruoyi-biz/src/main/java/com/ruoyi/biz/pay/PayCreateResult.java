package com.ruoyi.biz.pay;

public class PayCreateResult
{
    private String payType;
    private String payUrl;
    private String providerTradeNo;

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getProviderTradeNo() { return providerTradeNo; }
    public void setProviderTradeNo(String providerTradeNo) { this.providerTradeNo = providerTradeNo; }
}
