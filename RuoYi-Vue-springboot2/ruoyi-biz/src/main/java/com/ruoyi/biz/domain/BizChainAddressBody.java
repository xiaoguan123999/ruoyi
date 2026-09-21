package com.ruoyi.biz.domain;

import io.swagger.annotations.ApiModelProperty;

public class BizChainAddressBody
{
    @ApiModelProperty("USDT-TRC20 收款地址，空字符串表示清空；null 表示不改")
    private String chainAddress;
    @ApiModelProperty("USDT-BEP20 收款地址，空字符串表示清空；null 表示不改")
    private String chainAddressBep20;

    public String getChainAddress() { return chainAddress; }
    public void setChainAddress(String chainAddress) { this.chainAddress = chainAddress; }
    public String getChainAddressBep20() { return chainAddressBep20; }
    public void setChainAddressBep20(String chainAddressBep20) { this.chainAddressBep20 = chainAddressBep20; }
}
