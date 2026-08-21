package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.biz.api.AppNoticeDetailResult;
import com.ruoyi.biz.api.AppNoticeItem;
import com.ruoyi.biz.api.AppNoticeListResult;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
    @ApiOperation(value = "公告列表", notes = "首页滚动条用。列表没有正文，详情才有 noticeContent。")
    @GetMapping
    public AppNoticeListResult list()
    {
        SysNotice query = new SysNotice();
        query.setNoticeType(TYPE_ANNOUNCEMENT);
        query.setStatus(STATUS_NORMAL);
        List<SysNotice> notices = noticeService.selectNoticeList(query);
        List<AppNoticeItem> rows = new ArrayList<AppNoticeItem>();
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
        return AppNoticeListResult.ok(rows);
    }

    @Anonymous
    @ApiOperation(value = "公告详情", notes = "data.noticeContent 为纯文本。")
    @GetMapping("/{noticeId}")
    public AppNoticeDetailResult detail(@PathVariable Long noticeId)
    {
        SysNotice notice = noticeService.selectNoticeById(noticeId);
        if (notice == null || !TYPE_ANNOUNCEMENT.equals(notice.getNoticeType())
                || !STATUS_NORMAL.equals(notice.getStatus()))
        {
            return AppNoticeDetailResult.fail("公告不存在或已关闭");
        }
        return AppNoticeDetailResult.ok(toItem(notice, true));
    }

    private AppNoticeItem toItem(SysNotice notice, boolean withContent)
    {
        AppNoticeItem item = new AppNoticeItem();
        item.setNoticeId(notice.getNoticeId());
        item.setNoticeTitle(notice.getNoticeTitle());
        item.setCreateTime(notice.getCreateTime());
        if (withContent)
        {
            item.setNoticeContent(toPlainText(notice.getNoticeContent()));
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
