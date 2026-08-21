package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizGroupChat;
import com.ruoyi.biz.service.IBizGroupChatService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * App 官方群聊（展示二维码，后台手改）
 */
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
    @ApiOperation("官方群聊")
    @GetMapping
    public AjaxResult list()
    {
        List<BizGroupChat> items = groupChatService.selectAppGroupChatList();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (BizGroupChat item : items)
        {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("groupId", item.getGroupId());
            row.put("title", item.getTitle());
            row.put("hint", item.getHint() == null ? "扫码进群" : item.getHint());
            row.put("qrUrl", toPublicUrl(item.getQrUrl()));
            row.put("remark", item.getRemark() == null ? "" : item.getRemark());
            row.put("sort", item.getSort());
            rows.add(row);
        }
        return success(rows);
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
