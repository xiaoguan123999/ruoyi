package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizPayOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long payOrderId;
    private String outTradeNo;
    private Long rechargeId;
    private Long memberId;
    private String phone;
    private String providerCode;
    private String providerName;
    private String channelCode;
    private String channelName;
    private String productId;
    private String currency;
    private BigDecimal amount;
    private BigDecimal providerAmount;
    private String status;
    private String payType;
    private String payUrl;
    private String providerTradeNo;
    private String notifyPayload;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paidTime;
    private String mockMode;

    public Long getPayOrderId() { return payOrderId; }
    public void setPayOrderId(Long payOrderId) { this.payOrderId = payOrderId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getProviderAmount() { return providerAmount; }
    public void setProviderAmount(BigDecimal providerAmount) { this.providerAmount = providerAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }
    public String getPayUrl() { return payUrl; }
    public void setPayUrl(String payUrl) { this.payUrl = payUrl; }
    public String getProviderTradeNo() { return providerTradeNo; }
    public void setProviderTradeNo(String providerTradeNo) { this.providerTradeNo = providerTradeNo; }
    public String getNotifyPayload() { return notifyPayload; }
    public void setNotifyPayload(String notifyPayload) { this.notifyPayload = notifyPayload; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Date getPaidTime() { return paidTime; }
    public void setPaidTime(Date paidTime) { this.paidTime = paidTime; }
    public String getMockMode() { return mockMode; }
    public void setMockMode(String mockMode) { this.mockMode = mockMode; }
}
