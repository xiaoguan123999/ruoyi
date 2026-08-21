package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.biz.api.AppAboutItem;
import com.ruoyi.biz.api.AppAboutResult;
import com.ruoyi.biz.domain.BizAbout;
import com.ruoyi.biz.service.IBizAboutService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-关于我们")
@RestController
@RequestMapping("/app/about")
public class AppAboutController extends BaseController
{
    @Autowired
    private IBizAboutService aboutService;

    @Anonymous
    @ApiOperation(value = "关于我们", notes = "data 为数组。content 已转成纯文本。")
    @GetMapping
    public AppAboutResult list()
    {
        List<BizAbout> items = aboutService.selectAppAboutList();
        List<AppAboutItem> rows = new ArrayList<AppAboutItem>();
        for (BizAbout item : items)
        {
            AppAboutItem row = new AppAboutItem();
            row.setAboutId(item.getAboutId());
            row.setTitle(item.getTitle());
            row.setSubtitle(item.getSubtitle() == null ? "" : item.getSubtitle());
            row.setContent(toPlainText(item.getContent()));
            row.setImageUrl(item.getImageUrl() == null ? "" : item.getImageUrl());
            row.setSort(item.getSort());
            rows.add(row);
        }
        return AppAboutResult.ok(rows);
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
