package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BizPayProvider extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long providerId;
    private String providerCode;
    private String providerName;
    private String adapterFamily;
    private String gatewayUrl;
    private String appId;
    private String secretKey;
    private String mockMode;
    private String status;
    private Integer sortOrder;

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
    public String getProviderCode() { return providerCode; }
    public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getAdapterFamily() { return adapterFamily; }
    public void setAdapterFamily(String adapterFamily) { this.adapterFamily = adapterFamily; }
    public String getGatewayUrl() { return gatewayUrl; }
    public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getMockMode() { return mockMode; }
    public void setMockMode(String mockMode) { this.mockMode = mockMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
