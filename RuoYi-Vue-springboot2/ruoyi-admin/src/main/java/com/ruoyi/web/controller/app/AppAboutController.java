package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.ruoyi.biz.api.AppAboutItem;
import com.ruoyi.biz.api.AppAboutResult;
import com.ruoyi.biz.constant.BizConstants;
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
    @ApiOperation(value = "关于我们", notes = "data 为单条。mode=TEXT 看 title/subtitle/content/imageUrl；mode=PDF 看 pdfUrl。content 已转成纯文本。")
    @GetMapping
    public AppAboutResult get()
    {
        BizAbout item = aboutService.getSingleton();
        if (item == null || !BizConstants.STATUS_OK.equals(item.getStatus()))
        {
            return AppAboutResult.fail("暂无内容");
        }
        AppAboutItem row = new AppAboutItem();
        row.setMode(item.getMode());
        row.setTitle(item.getTitle() == null ? "" : item.getTitle());
        row.setSubtitle(item.getSubtitle() == null ? "" : item.getSubtitle());
        row.setContent(toPlainText(item.getContent()));
        row.setImageUrl(item.getImageUrl() == null ? "" : item.getImageUrl());
        row.setPdfUrl(item.getPdfUrl() == null ? "" : item.getPdfUrl());
        return AppAboutResult.ok(row);
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
