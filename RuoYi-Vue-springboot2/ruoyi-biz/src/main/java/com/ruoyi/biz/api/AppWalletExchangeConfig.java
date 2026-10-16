package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("钱包互转规则与余额")
public class AppWalletExchangeConfig
{
    @ApiModelProperty("1 USDT 折合多少 CNY")
    private BigDecimal usdtToCny;

    @ApiModelProperty("产品收益 CNY 可用")
    private BigDecimal productCny;

    @ApiModelProperty("产品收益 USDT 可用")
    private BigDecimal productUsdt;

    @ApiModelProperty("推广收益 CNY 可用")
    private BigDecimal promoCny;

    @ApiModelProperty("推广收益 USDT 可用")
    private BigDecimal promoUsdt;

    public BigDecimal getUsdtToCny() { return usdtToCny; }
    public void setUsdtToCny(BigDecimal usdtToCny) { this.usdtToCny = usdtToCny; }
    public BigDecimal getProductCny() { return productCny; }
    public void setProductCny(BigDecimal productCny) { this.productCny = productCny; }
    public BigDecimal getProductUsdt() { return productUsdt; }
    public void setProductUsdt(BigDecimal productUsdt) { this.productUsdt = productUsdt; }
    public BigDecimal getPromoCny() { return promoCny; }
    public void setPromoCny(BigDecimal promoCny) { this.promoCny = promoCny; }
    public BigDecimal getPromoUsdt() { return promoUsdt; }
    public void setPromoUsdt(BigDecimal promoUsdt) { this.promoUsdt = promoUsdt; }
}
