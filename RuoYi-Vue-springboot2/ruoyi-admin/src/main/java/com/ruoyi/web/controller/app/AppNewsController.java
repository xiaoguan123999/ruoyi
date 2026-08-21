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
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizNews;
import com.ruoyi.biz.service.IBizNewsService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * App 新闻资讯（展示用，后台手改）
 */
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
    @ApiOperation("新闻列表")
    @GetMapping
    public AjaxResult list()
    {
        List<BizNews> items = newsService.selectAppNewsList();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (BizNews item : items)
        {
            rows.add(toItem(item, false));
        }
        return success(rows);
    }

    @Anonymous
    @ApiOperation("新闻详情")
    @GetMapping("/{newsId}")
    public AjaxResult detail(@PathVariable Long newsId)
    {
        BizNews news = newsService.selectNewsById(newsId);
        if (news == null || !BizConstants.STATUS_OK.equals(news.getStatus()))
        {
            return error("新闻不存在或已关闭");
        }
        return success(toItem(news, true));
    }

    private Map<String, Object> toItem(BizNews news, boolean withContent)
    {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("newsId", news.getNewsId());
        item.put("title", news.getTitle());
        item.put("summary", news.getSummary() == null ? "" : news.getSummary());
        item.put("coverUrl", toPublicUrl(news.getCoverUrl()));
        item.put("publishDate", news.getPublishTime() == null ? "" : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, news.getPublishTime()));
        item.put("sort", news.getSort());
        if (withContent)
        {
            item.put("content", toPlainText(news.getContent()));
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
