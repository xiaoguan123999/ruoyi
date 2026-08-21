package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * App 公告（复用 sys_notice，仅展示类型=公告且状态=正常）
 */
@Api(tags = "App-公告")
@RestController
@RequestMapping("/app/notices")
public class AppNoticeController extends BaseController
{
    private static final String TYPE_ANNOUNCEMENT = "2";

    private static final String STATUS_NORMAL = "0";

    private static final int LIST_LIMIT = 20;

    @Autowired
    private ISysNoticeService noticeService;

    @Anonymous
    @ApiOperation("公告列表")
    @GetMapping
    public AjaxResult list()
    {
        SysNotice query = new SysNotice();
        query.setNoticeType(TYPE_ANNOUNCEMENT);
        query.setStatus(STATUS_NORMAL);
        List<SysNotice> notices = noticeService.selectNoticeList(query);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        int count = 0;
        for (SysNotice notice : notices)
        {
            if (count >= LIST_LIMIT)
            {
                break;
            }
            rows.add(toItem(notice, false));
            count++;
        }
        return success(rows);
    }

    @Anonymous
    @ApiOperation("公告详情")
    @GetMapping("/{noticeId}")
    public AjaxResult detail(@PathVariable Long noticeId)
    {
        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null || !TYPE_ANNOUNCEMENT.equals(notice.getNoticeType())
                || !STATUS_NORMAL.equals(notice.getStatus()))
        {
            return error("公告不存在或已关闭");
        }
        return success(toItem(notice, true));
    }

    private Map<String, Object> toItem(SysNotice notice, boolean withContent)
    {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("noticeId", notice.getNoticeId());
        item.put("noticeTitle", notice.getNoticeTitle());
        item.put("createTime", notice.getCreateTime());
        if (withContent)
        {
            item.put("noticeContent", toPlainText(notice.getNoticeContent()));
        }
        return item;
    }

    private String toPlainText(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return "";
        }
        String text = html.replaceAll("(?i)<script[\\s\\S]*?</script>", "")
                .replaceAll("(?i)<style[\\s\\S]*?</style>", "")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)<[^>]+>", "");
        text = HtmlUtils.htmlUnescape(text).replace('\u00A0', ' ');
        return text.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }
}
