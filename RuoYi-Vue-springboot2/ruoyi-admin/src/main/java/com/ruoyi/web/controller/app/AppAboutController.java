package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.biz.domain.BizAbout;
import com.ruoyi.biz.service.IBizAboutService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * App 关于我们（展示内容，后台手改）
 */
@Api(tags = "App-关于我们")
@RestController
@RequestMapping("/app/about")
public class AppAboutController extends BaseController
{
    @Autowired
    private IBizAboutService aboutService;

    @Anonymous
    @ApiOperation("关于我们")
    @GetMapping
    public AjaxResult list()
    {
        List<BizAbout> items = aboutService.selectAppAboutList();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (BizAbout item : items)
        {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("aboutId", item.getAboutId());
            row.put("title", item.getTitle());
            row.put("subtitle", item.getSubtitle() == null ? "" : item.getSubtitle());
            row.put("content", toPlainText(item.getContent()));
            row.put("imageUrl", item.getImageUrl() == null ? "" : item.getImageUrl());
            row.put("sort", item.getSort());
            rows.add(row);
        }
        return success(rows);
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
