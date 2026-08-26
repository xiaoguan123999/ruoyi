package com.ruoyi.biz.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizPayChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long channelId;
    private String providerCode;
    private String channelCode;
    private String channelName;
    private String displayName;
    private String scene;
    private String productId;
    private String currency;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer weight;
    private String status;
    private Integer sortOrder;
    private String providerName;
    private String mockMode;
    private String providerStatus;
    private String adapterFamily;

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getScene() { return scene; }
    public void setScene(String scene) { this.scene = scene; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getMockMode() { return mockMode; }
    public void setMockMode(String mockMode) { this.mockMode = mockMode; }
    public String getProviderStatus() { return providerStatus; }
    public void setProviderStatus(String providerStatus) { this.providerStatus = providerStatus; }
    public String getAdapterFamily() { return adapterFamily; }
    public void setAdapterFamily(String adapterFamily) { this.adapterFamily = adapterFamily; }
}
