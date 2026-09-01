package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppVersionLatestData;
import com.ruoyi.biz.api.AppVersionLatestItem;
import com.ruoyi.biz.api.AppVersionLatestQuery;
import com.ruoyi.biz.api.AppVersionLatestResult;
import com.ruoyi.biz.service.IBizAppVersionService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "App-版本")
@RestController
@RequestMapping("/app")
public class AppVersionController extends BaseController
{
    @Autowired
    private IBizAppVersionService versionService;

    @Anonymous
    @ApiOperation(value = "最新版本", notes = "未登录可调。platform=android 或 ios。没有启用版本时 data.version 为 null。")
    @GetMapping({"/version/latest", "/application/latest_version"})
    public AppVersionLatestResult latestGet(
            @ApiParam("android 或 ios") @RequestParam(value = "platform", required = false) String platform,
            @ApiParam("当前版本号，可选") @RequestParam(value = "version", required = false) String version)
    {
        return latest(platform);
    }

    @Anonymous
    @ApiOperation(value = "最新版本", notes = "POST { platform, version }，version 可选。")
    @PostMapping({"/version/latest", "/application/latest_version"})
    public AppVersionLatestResult latestPost(@RequestBody(required = false) AppVersionLatestQuery body)
    {
        String platform = body == null ? null : body.getPlatform();
        return latest(platform);
    }

    private AppVersionLatestResult latest(String platform)
    {
        if (StringUtils.isEmpty(platform))
        {
            return AppVersionLatestResult.fail("请选择平台 android 或 ios");
        }
        try
        {
            return AppVersionLatestResult.ok(AppVersionLatestData.of(
                    AppVersionLatestItem.from(versionService.selectLatest(platform))));
        }
        catch (Exception e)
        {
            return AppVersionLatestResult.fail(e.getMessage());
        }
    }
}
