package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("查询最新版本")
public class AppVersionLatestQuery
{
    @ApiModelProperty(value = "android 或 ios", required = true)
    private String platform;

    @ApiModelProperty("当前客户端版本号，可选")
    private String version;

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
}
