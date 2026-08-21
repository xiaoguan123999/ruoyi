package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.biz.api.AppNewsDetailResult;
import com.ruoyi.biz.api.AppNewsItem;
import com.ruoyi.biz.api.AppNewsListResult;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizNews;
import com.ruoyi.biz.service.IBizNewsService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-新闻资讯")
@RestController
@RequestMapping("/app/news")
public class AppNewsController extends BaseController
{
    @Autowired
    private IBizNewsService newsService;

    @Autowired
    private ServerConfig serverConfig;

    @Anonymous
    @ApiOperation(value = "新闻列表", notes = "data 为数组。列表没有 content，详情才有。coverUrl 为空时 App 用本地默认封面。")
    @GetMapping
    public AppNewsListResult list()
    {
        List<BizNews> items = newsService.selectAppNewsList();
        List<AppNewsItem> rows = new ArrayList<AppNewsItem>();
        for (BizNews item : items)
        {
            rows.add(toItem(item, false));
        }
        return AppNewsListResult.ok(rows);
    }

    @Anonymous
    @ApiOperation(value = "新闻详情", notes = "data.content 是纯文本。不存在或已隐藏返回 code=500。")
    @GetMapping("/{newsId}")
    public AppNewsDetailResult detail(@PathVariable Long newsId)
    {
        BizNews news = newsService.selectNewsById(newsId);
        if (news == null || !BizConstants.STATUS_OK.equals(news.getStatus()))
        {
            return AppNewsDetailResult.fail("新闻不存在或已关闭");
        }
        return AppNewsDetailResult.ok(toItem(news, true));
    }

    private AppNewsItem toItem(BizNews news, boolean withContent)
    {
        AppNewsItem item = new AppNewsItem();
        item.setNewsId(news.getNewsId());
        item.setTitle(news.getTitle());
        item.setSummary(news.getSummary() == null ? "" : news.getSummary());
        item.setCoverUrl(toPublicUrl(news.getCoverUrl()));
        item.setPublishDate(news.getPublishTime() == null ? "" : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, news.getPublishTime()));
        item.setSort(news.getSort());
        if (withContent)
        {
            item.setContent(toPlainText(news.getContent()));
        }
        return item;
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
