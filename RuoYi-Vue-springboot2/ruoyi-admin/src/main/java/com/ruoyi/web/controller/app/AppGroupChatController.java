package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppGroupChatItem;
import com.ruoyi.biz.api.AppGroupChatResult;
import com.ruoyi.biz.domain.BizGroupChat;
import com.ruoyi.biz.service.IBizGroupChatService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-官方群聊")
@RestController
@RequestMapping("/app/group-chat")
public class AppGroupChatController extends BaseController
{
    @Autowired
    private IBizGroupChatService groupChatService;

    @Autowired
    private ServerConfig serverConfig;

    @Anonymous
    @ApiOperation(value = "官方群聊", notes = "data 为数组。qrUrl 是完整图片地址，App 直接展示。")
    @GetMapping
    public AppGroupChatResult list()
    {
        List<BizGroupChat> items = groupChatService.selectAppGroupChatList();
        List<AppGroupChatItem> rows = new ArrayList<AppGroupChatItem>();
        for (BizGroupChat item : items)
        {
            AppGroupChatItem row = new AppGroupChatItem();
            row.setGroupId(item.getGroupId());
            row.setTitle(item.getTitle());
            row.setHint(item.getHint() == null ? "扫码进群" : item.getHint());
            row.setQrUrl(toPublicUrl(item.getQrUrl()));
            row.setRemark(item.getRemark() == null ? "" : item.getRemark());
            row.setSort(item.getSort());
            rows.add(row);
        }
        return AppGroupChatResult.ok(rows);
    }

    private String toPublicUrl(String stored)
    {
        if (StringUtils.isEmpty(stored))
        {
            return "";
        }
        if (stored.startsWith("http://") || stored.startsWith("https://"))
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
