package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class AppChainDepositBody
{
    @ApiModelProperty(value = "用户填写的充值金额，按2位四舍五入后再加4位指纹", required = true)
    private BigDecimal amount;
    @ApiModelProperty("TRC20 或 BEP20，不传默认 TRC20")
    private String network;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
}
