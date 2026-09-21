package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizChainDeposit extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long depositId;
    private String outTradeNo;
    private Long rechargeId;
    private Long memberId;
    private String phone;
    private String asset;
    private String network;
    private String currency;
    private BigDecimal amount;
    private BigDecimal payAmount;
    private String address;
    private String addressSource;
    private Long collectMemberId;
    private String collectPhone;
    private String collectInviteCode;
    private String status;
    private String txHash;
    private String fromAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paidTime;

    public Long getDepositId() { return depositId; }
    public void setDepositId(Long depositId) { this.depositId = depositId; }
    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressSource() { return addressSource; }
    public void setAddressSource(String addressSource) { this.addressSource = addressSource; }
    public Long getCollectMemberId() { return collectMemberId; }
    public void setCollectMemberId(Long collectMemberId) { this.collectMemberId = collectMemberId; }
    public String getCollectPhone() { return collectPhone; }
    public void setCollectPhone(String collectPhone) { this.collectPhone = collectPhone; }
    public String getCollectInviteCode() { return collectInviteCode; }
    public void setCollectInviteCode(String collectInviteCode) { this.collectInviteCode = collectInviteCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Date getPaidTime() { return paidTime; }
    public void setPaidTime(Date paidTime) { this.paidTime = paidTime; }
}
