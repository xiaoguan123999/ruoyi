package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("提现规则")
public class BizWithdrawRule
{
    @ApiModelProperty("人民币最低提现")
    private BigDecimal minCny;
    @ApiModelProperty("人民币最高提现，0或不填表示不限")
    private BigDecimal maxCny;
    @ApiModelProperty("USDT最低提现")
    private BigDecimal minUsdt;
    @ApiModelProperty("USDT最高提现，0或不填表示不限")
    private BigDecimal maxUsdt;
    @ApiModelProperty("是否开放USDT充值和提现")
    private Boolean usdtEnabled;
    @ApiModelProperty("withdraw fee percent, 3 means 3%, 0 means free")
    private BigDecimal feeRate;
    @ApiModelProperty("App产品收益提现扣这个钱包")
    private String productWalletType;
    @ApiModelProperty("App推广收益提现扣这个钱包")
    private String promoWalletType;
    @ApiModelProperty("是否需要完成实名才能提现，true需要 false不需要")
    private Boolean needKyc;

    public BigDecimal getMinCny() { return minCny; }
    public void setMinCny(BigDecimal minCny) { this.minCny = minCny; }
    public BigDecimal getMaxCny() { return maxCny; }
    public void setMaxCny(BigDecimal maxCny) { this.maxCny = maxCny; }
    public BigDecimal getMinUsdt() { return minUsdt; }
    public void setMinUsdt(BigDecimal minUsdt) { this.minUsdt = minUsdt; }
    public BigDecimal getMaxUsdt() { return maxUsdt; }
    public void setMaxUsdt(BigDecimal maxUsdt) { this.maxUsdt = maxUsdt; }
    public Boolean getUsdtEnabled() { return usdtEnabled; }
    public void setUsdtEnabled(Boolean usdtEnabled) { this.usdtEnabled = usdtEnabled; }
    public BigDecimal getFeeRate() { return feeRate; }
    public void setFeeRate(BigDecimal feeRate) { this.feeRate = feeRate; }
    public String getProductWalletType() { return productWalletType; }
    public void setProductWalletType(String productWalletType) { this.productWalletType = productWalletType; }
    public String getPromoWalletType() { return promoWalletType; }
    public void setPromoWalletType(String promoWalletType) { this.promoWalletType = promoWalletType; }
    public Boolean getNeedKyc() { return needKyc; }
    public void setNeedKyc(Boolean needKyc) { this.needKyc = needKyc; }
}
