package com.ruoyi.biz.chain;

import java.math.BigDecimal;
import java.util.Date;

public class TronUsdtTransfer
{
    private String network;
    private String txHash;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private Date blockTime;

    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getBlockTime() { return blockTime; }
    public void setBlockTime(Date blockTime) { this.blockTime = blockTime; }
}
