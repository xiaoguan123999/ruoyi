package com.ruoyi.biz.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("App版本")
public class BizAppVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("版本ID")
    private Long versionId;

    @ApiModelProperty("平台 android / ios")
    private String platform;

    @ApiModelProperty("版本号，如 1.0.11")
    private String version;

    @ApiModelProperty("下载链接")
    private String downloadUrl;

    @ApiModelProperty("版本说明")
    private String description;

    @ApiModelProperty("强制更新：1是 0否")
    private String forceUpdate;

    @ApiModelProperty("最新版本：1是 0否")
    private String isLatest;

    @ApiModelProperty("是否启用：1是 0否")
    private String isEnabled;

    @ApiModelProperty("排序，越大越靠前")
    private Integer sortOrder;

    private String delFlag;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }

    @ApiModelProperty("同 versionId，兼容 RWA")
    public Long getId() { return versionId; }
    public void setId(Long id) { this.versionId = id; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getForceUpdate() { return forceUpdate; }
    public void setForceUpdate(Object forceUpdate) { this.forceUpdate = normObj(forceUpdate); }
    public String getIsLatest() { return isLatest; }
    public void setIsLatest(Object isLatest) { this.isLatest = normObj(isLatest); }
    public String getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Object isEnabled) { this.isEnabled = normObj(isEnabled); }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @ApiModelProperty("强制更新")
    public Boolean getForceUpdateFlag() { return flag(forceUpdate); }
    public void setForceUpdateFlag(Boolean v) { this.forceUpdate = v != null && v.booleanValue() ? "1" : "0"; }
    @ApiModelProperty("最新版本")
    public Boolean getLatestFlag() { return flag(isLatest); }
    public void setLatestFlag(Boolean v) { this.isLatest = v != null && v.booleanValue() ? "1" : "0"; }
    @ApiModelProperty("是否启用")
    public Boolean getEnabledFlag() { return flag(isEnabled); }
    public void setEnabledFlag(Boolean v) { this.isEnabled = v != null && v.booleanValue() ? "1" : "0"; }

    public boolean on() { return "1".equals(isEnabled); }
    public boolean latest() { return "1".equals(isLatest); }
    public boolean force() { return "1".equals(forceUpdate); }

    private static Boolean flag(String v) { return Boolean.valueOf("1".equals(v) || "true".equalsIgnoreCase(v)); }

    public static String norm(String v)
    {
        if (v == null)
        {
            return null;
        }
        String t = v.trim();
        if ("1".equals(t) || "true".equalsIgnoreCase(t) || "yes".equalsIgnoreCase(t))
        {
            return "1";
        }
        if ("0".equals(t) || "false".equalsIgnoreCase(t) || "no".equalsIgnoreCase(t) || t.isEmpty())
        {
            return "0";
        }
        return t;
    }

    private static String normObj(Object v)
    {
        if (v == null)
        {
            return null;
        }
        if (v instanceof Boolean)
        {
            return ((Boolean) v).booleanValue() ? "1" : "0";
        }
        return norm(String.valueOf(v));
    }
}
