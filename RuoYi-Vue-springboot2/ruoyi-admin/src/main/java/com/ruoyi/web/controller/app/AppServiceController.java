package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppCsChannelItem;
import com.ruoyi.biz.api.AppServiceData;
import com.ruoyi.biz.api.AppServiceResult;
import com.ruoyi.biz.service.IBizCsService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-客服中心")
@RestController
@RequestMapping("/app")
public class AppServiceController extends BaseController
{
    @Autowired
    private IBizCsService csService;

    @Autowired
    private ServerConfig serverConfig;

    @Anonymous
    @ApiOperation(value = "客服中心", notes = "登录页「联系客服」也可调。qrUrl 是完整图片地址。别名 /app/customer-service。")
    @GetMapping({"/service", "/customer-service"})
    public AppServiceResult service()
    {
        AppServiceData data = csService.getAppService();
        List<AppCsChannelItem> channels = data.getChannels();
        if (channels != null)
        {
            for (int i = 0; i < channels.size(); i++)
            {
                AppCsChannelItem item = channels.get(i);
                item.setQrUrl(toPublicUrl(item.getQrUrl()));
            }
        }
        return AppServiceResult.ok(data);
    }

    private String toPublicUrl(String stored)
    {
        if (StringUtils.isEmpty(stored))
        {
            return "";
        }
        if (stored.startsWith("http://") || stored.startsWith("https://")
                || stored.startsWith("tel:") || stored.startsWith("mailto:"))
        {
            return stored;
        }
        String domain = serverConfig.getUrl();
        if (stored.startsWith("/"))
        {
            return domain + stored;
        }
        return domain + "/" + stored;
    }
}
