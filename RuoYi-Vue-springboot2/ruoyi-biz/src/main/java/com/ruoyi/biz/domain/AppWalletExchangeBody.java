package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App钱包互转请求")
public class AppWalletExchangeBody
{
    @ApiModelProperty(value = "钱包类型：PRODUCT产品收益 / PROMO推广收益", required = true, example = "PRODUCT")
    private String typeCode;

    @ApiModelProperty(value = "转出币种 CNY 或 USDT", required = true, example = "CNY")
    private String fromCurrency;

    @ApiModelProperty(value = "转出金额", required = true, example = "100")
    private BigDecimal amount;

    @ApiModelProperty(value = "支付密码", required = true, example = "123456")
    @JsonAlias({"tradePassword", "fundPassword", "payPwd"})
    private String payPassword;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPayPassword() { return payPassword; }
    public void setPayPassword(String payPassword) { this.payPassword = payPassword; }
}
