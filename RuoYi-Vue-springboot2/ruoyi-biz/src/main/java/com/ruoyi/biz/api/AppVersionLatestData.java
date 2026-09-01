package com.ruoyi.biz.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("最新版本包装")
public class AppVersionLatestData
{
    @ApiModelProperty("最新启用版本，没有则为 null")
    private AppVersionLatestItem version;

    public static AppVersionLatestData of(AppVersionLatestItem version)
    {
        AppVersionLatestData data = new AppVersionLatestData();
        data.version = version;
        return data;
    }

    public AppVersionLatestItem getVersion() { return version; }
    public void setVersion(AppVersionLatestItem version) { this.version = version; }
}
