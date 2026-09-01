package com.ruoyi.biz.api;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.biz.domain.BizAppVersion;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("最新版本")
public class AppVersionLatestItem
{
    @ApiModelProperty("版本ID")
    private Long id;
    private Long versionId;
    private String platform;
    private String version;
    private String downloadUrl;
    private String description;
    private Boolean forceUpdate;
    private Boolean isLatest;
    private Boolean isEnabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public static AppVersionLatestItem from(BizAppVersion row)
    {
        if (row == null)
        {
            return null;
        }
        AppVersionLatestItem item = new AppVersionLatestItem();
        item.id = row.getVersionId();
        item.versionId = row.getVersionId();
        item.platform = row.getPlatform();
        item.version = row.getVersion();
        item.downloadUrl = row.getDownloadUrl();
        item.description = row.getDescription();
        item.forceUpdate = Boolean.valueOf(row.force());
        item.isLatest = Boolean.valueOf(row.latest());
        item.isEnabled = Boolean.valueOf(row.on());
        item.createTime = row.getCreateTime();
        return item;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getForceUpdate() { return forceUpdate; }
    public void setForceUpdate(Boolean forceUpdate) { this.forceUpdate = forceUpdate; }
    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }
    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
