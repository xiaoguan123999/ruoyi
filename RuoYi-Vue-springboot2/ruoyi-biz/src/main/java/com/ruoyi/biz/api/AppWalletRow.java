package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("单个币种资产")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWalletRow
{
    @ApiModelProperty(value = "币种：CNY / USDT", example = "CNY")
    private String currency;

    @ApiModelProperty(value = "可用余额", example = "202.00")
    private BigDecimal available;

    @ApiModelProperty(value = "冻结金额，提现审核中会冻在这里", example = "0")
    private BigDecimal frozen;

    @ApiModelProperty(value = "累计产品收益（该币种日返合计）", example = "12.50")
    private BigDecimal productIncome;

    @ApiModelProperty(value = "推广收益累计（签到、实名奖励、邀请、分佣、等级奖励），字段名仍为 assistValue", example = "0")
    private BigDecimal assistValue;

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getAvailable() { return available; }
    public void setAvailable(BigDecimal available) { this.available = available; }
    public BigDecimal getFrozen() { return frozen; }
    public void setFrozen(BigDecimal frozen) { this.frozen = frozen; }
    public BigDecimal getProductIncome() { return productIncome; }
    public void setProductIncome(BigDecimal productIncome) { this.productIncome = productIncome; }
    public BigDecimal getAssistValue() { return assistValue; }
    public void setAssistValue(BigDecimal assistValue) { this.assistValue = assistValue; }
}
