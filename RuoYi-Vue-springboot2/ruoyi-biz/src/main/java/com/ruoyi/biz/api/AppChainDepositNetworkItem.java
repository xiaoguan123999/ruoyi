package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModelProperty;

public class AppChainDepositNetworkItem
{
    @ApiModelProperty("TRC20 或 BEP20，下单时回传 network")
    private String network;
    @ApiModelProperty("展示名，如 TRC20 / BEP20")
    private String name;
    private String asset;
    private String address;
    @ApiModelProperty("SYSTEM 系统默认，TEAM 最近配置了该网络地址的上级")
    private String addressSource;
    private String contractAddress;
    @ApiModelProperty("链上代币精度，TRC20=6 BEP20=18。转账金额仍用订单 payAmountText 的6位小数")
    private Integer decimals;
    private Boolean enabled;

    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressSource() { return addressSource; }
    public void setAddressSource(String addressSource) { this.addressSource = addressSource; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public Integer getDecimals() { return decimals; }
    public void setDecimals(Integer decimals) { this.decimals = decimals; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
