package com.ruoyi.biz.api;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModelProperty;

public class AppPayDepositBody
{
    @ApiModelProperty("金额")
    private BigDecimal amount;
    @ApiModelProperty("场景 alipay/wechat/union/usdt，与 channelCode 二选一")
    private String scene;
    @ApiModelProperty("指定通道编码")
    private String channelCode;
    @ApiModelProperty("支付成功回跳，可空")
    private String returnUrl;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
