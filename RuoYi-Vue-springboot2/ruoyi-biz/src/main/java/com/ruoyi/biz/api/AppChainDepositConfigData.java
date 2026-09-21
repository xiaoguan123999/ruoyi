package com.ruoyi.biz.api;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.annotations.ApiModelProperty;

public class AppChainDepositConfigData
{
    @ApiModelProperty("false 时 App 隐藏链上充值入口")
    private Boolean enabled;
    @ApiModelProperty("兼容字段：默认网络，优先 TRC20")
    private String network;
    private String asset;
    private String currency;
    @ApiModelProperty("兼容字段：默认网络的收款地址")
    private String address;
    @ApiModelProperty("SYSTEM 系统默认地址，TEAM 最近配置了地址的上级")
    private String addressSource;
    private String contractAddress;
    private Integer decimals;
    private Integer expireMinutes;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String hint;
    @ApiModelProperty("TRC20 / BEP20 列表，App 按此渲染网络切换")
    private List<AppChainDepositNetworkItem> networks;

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getNetwork() { return network; }
    public void setNetwork(String network) { this.network = network; }
    public String getAsset() { return asset; }
    public void setAsset(String asset) { this.asset = asset; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressSource() { return addressSource; }
    public void setAddressSource(String addressSource) { this.addressSource = addressSource; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public Integer getDecimals() { return decimals; }
    public void setDecimals(Integer decimals) { this.decimals = decimals; }
    public Integer getExpireMinutes() { return expireMinutes; }
    public void setExpireMinutes(Integer expireMinutes) { this.expireMinutes = expireMinutes; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public List<AppChainDepositNetworkItem> getNetworks() { return networks; }
    public void setNetworks(List<AppChainDepositNetworkItem> networks) { this.networks = networks; }
}
