package com.ruoyi.biz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("资产卡")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppWalletCard
{
    @ApiModelProperty("人民币可用余额（充值余额）")
    private BigDecimal cnyAvailable;
    @ApiModelProperty("人民币冻结")
    private BigDecimal cnyFrozen;
    @ApiModelProperty("人民币产品收益剩余")
    private BigDecimal cnyProductIncome;
    @ApiModelProperty("人民币推广收益剩余，字段名仍为 cnyAssistValue")
    private BigDecimal cnyAssistValue;
    @ApiModelProperty("USDT 可用余额（充值余额）")
    private BigDecimal usdtAvailable;
    @ApiModelProperty("USDT 冻结")
    private BigDecimal usdtFrozen;
    @ApiModelProperty("USDT 产品收益剩余")
    private BigDecimal usdtProductIncome;
    @ApiModelProperty("USDT 推广收益剩余，字段名仍为 usdtAssistValue")
    private BigDecimal usdtAssistValue;
    @ApiModelProperty("人民币一行，给资产卡用")
    private AppWalletRow cny;
    @ApiModelProperty("USDT 一行，给资产卡用")
    private AppWalletRow usdt;
    @ApiModelProperty("两个币种数组，顺序 CNY、USDT")
    private List<AppWalletRow> wallets;
    @ApiModelProperty("按钱包类型拆开的资产，App 可忽略")
    private List<AppTypedWallet> typedWallets;

    public BigDecimal getCnyAvailable() { return cnyAvailable; }
    public void setCnyAvailable(BigDecimal cnyAvailable) { this.cnyAvailable = cnyAvailable; }
    public BigDecimal getCnyFrozen() { return cnyFrozen; }
    public void setCnyFrozen(BigDecimal cnyFrozen) { this.cnyFrozen = cnyFrozen; }
    public BigDecimal getCnyProductIncome() { return cnyProductIncome; }
    public void setCnyProductIncome(BigDecimal cnyProductIncome) { this.cnyProductIncome = cnyProductIncome; }
    public BigDecimal getCnyAssistValue() { return cnyAssistValue; }
    public void setCnyAssistValue(BigDecimal cnyAssistValue) { this.cnyAssistValue = cnyAssistValue; }
    public BigDecimal getUsdtAvailable() { return usdtAvailable; }
    public void setUsdtAvailable(BigDecimal usdtAvailable) { this.usdtAvailable = usdtAvailable; }
    public BigDecimal getUsdtFrozen() { return usdtFrozen; }
    public void setUsdtFrozen(BigDecimal usdtFrozen) { this.usdtFrozen = usdtFrozen; }
    public BigDecimal getUsdtProductIncome() { return usdtProductIncome; }
    public void setUsdtProductIncome(BigDecimal usdtProductIncome) { this.usdtProductIncome = usdtProductIncome; }
    public BigDecimal getUsdtAssistValue() { return usdtAssistValue; }
    public void setUsdtAssistValue(BigDecimal usdtAssistValue) { this.usdtAssistValue = usdtAssistValue; }
    public AppWalletRow getCny() { return cny; }
    public void setCny(AppWalletRow cny) { this.cny = cny; }
    public AppWalletRow getUsdt() { return usdt; }
    public void setUsdt(AppWalletRow usdt) { this.usdt = usdt; }
    public List<AppWalletRow> getWallets() { return wallets; }
    public void setWallets(List<AppWalletRow> wallets) { this.wallets = wallets; }
    public List<AppTypedWallet> getTypedWallets() { return typedWallets; }
    public void setTypedWallets(List<AppTypedWallet> typedWallets) { this.typedWallets = typedWallets; }
}
