package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("钱包互转结果")
public class AppWalletExchangeData
{
    @ApiModelProperty("钱包类型")
    private String typeCode;

    @ApiModelProperty("转出币种")
    private String fromCurrency;

    @ApiModelProperty("转入币种")
    private String toCurrency;

    @ApiModelProperty("转出金额")
    private BigDecimal fromAmount;

    @ApiModelProperty("转入金额")
    private BigDecimal toAmount;

    @ApiModelProperty("使用汇率：1 USDT = N CNY")
    private BigDecimal usdtToCny;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    public BigDecimal getFromAmount() { return fromAmount; }
    public void setFromAmount(BigDecimal fromAmount) { this.fromAmount = fromAmount; }
    public BigDecimal getToAmount() { return toAmount; }
    public void setToAmount(BigDecimal toAmount) { this.toAmount = toAmount; }
    public BigDecimal getUsdtToCny() { return usdtToCny; }
    public void setUsdtToCny(BigDecimal usdtToCny) { this.usdtToCny = usdtToCny; }
}
