package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class AppChainDepositOrderData
{
    private String outTradeNo;
    private Long rechargeId;
    @ApiModelProperty("TRC20 或 BEP20")
    private String network;
    private String address;
    @ApiModelProperty("SYSTEM 系统默认，TEAM 团队长地址")
    private String addressSource;
    private String asset;
    private String currency;
    private String contractAddress;
    @ApiModelProperty("链上代币精度，TRC20=6 BEP20=18")
    private Integer decimals;
    @ApiModelProperty("用户申请金额，2位小数")
    private BigDecimal amount;
    @ApiModelProperty("必须转账的精确金额，6位小数。前端请用 payAmountText，避免 JS 精度丢失")
    private BigDecimal payAmount;
    @ApiModelProperty("6位小数字符串，复制转账金额用这个")
    private String payAmountText;
    @ApiModelProperty("展示用，无时区，不要用来做倒计时")
    private String expireTime;
    @ApiModelProperty("到期时刻 UTC，ISO-8601，例如 2026-09-21T10:46:00.000Z。倒计时优先用 expireAt")
    private String expireTimeUtc;
    @ApiModelProperty("到期时刻 UTC 毫秒时间戳。倒计时 = expireAt - Date.now()")
    private Long expireAt;
    @ApiModelProperty("本响应时刻剩余秒数，小于等于0视为已到期")
    private Integer remainSeconds;
    @ApiModelProperty("0待转账 1已入账 2已过期")
    private String status;
    private String txHash;
    private String hint;

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }
    public Long getRechargeId() { return rechargeId; }
    public void setRechargeId(Long rechargeId) { this.rechargeId = rechargeId; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressSource() { return addressSource; }
    public void setAddressSource(String addressSource) { this.addressSource = addressSource; }
    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public Integer getDecimals() { return decimals; }
    public void setDecimals(Integer decimals) { this.decimals = decimals; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getPayAmountText() { return payAmountText; }
    public void setPayAmountText(String payAmountText) { this.payAmountText = payAmountText; }
    public String getExpireTime() { return expireTime; }
    public void setExpireTime(String expireTime) { this.expireTime = expireTime; }
    public String getExpireTimeUtc() { return expireTimeUtc; }
    public void setExpireTimeUtc(String expireTimeUtc) { this.expireTimeUtc = expireTimeUtc; }
    public Long getExpireAt() { return expireAt; }
    public void setExpireAt(Long expireAt) { this.expireAt = expireAt; }
    public Integer getRemainSeconds() { return remainSeconds; }
    public void setRemainSeconds(Integer remainSeconds) { this.remainSeconds = remainSeconds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
}
