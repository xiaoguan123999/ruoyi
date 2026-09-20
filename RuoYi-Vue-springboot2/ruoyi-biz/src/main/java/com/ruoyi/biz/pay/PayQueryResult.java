package com.ruoyi.biz.pay;

public class PayQueryResult
{
    private boolean paid;
    private String providerTradeNo;
    private String raw;

    public static PayQueryResult unpaid(String raw)
    {
        PayQueryResult r = new PayQueryResult();
        r.paid = false;
        r.raw = raw;
        return r;
    }

    public static PayQueryResult paid(String providerTradeNo, String raw)
    {
        PayQueryResult r = new PayQueryResult();
        r.paid = true;
        r.providerTradeNo = providerTradeNo;
        r.raw = raw;
        return r;
    }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public String getProviderTradeNo() { return providerTradeNo; }
    public void setProviderTradeNo(String providerTradeNo) { this.providerTradeNo = providerTradeNo; }
    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
}
