package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppCarouselItem;
import com.ruoyi.biz.api.AppCarouselListResult;
import com.ruoyi.biz.domain.BizCarousel;
import com.ruoyi.biz.service.IBizCarouselService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-视频轮播")
@RestController
@RequestMapping({ "/app/carousel", "/app/video" })
public class AppCarouselController extends BaseController
{
    @Autowired
    private IBizCarouselService carouselService;

    @Autowired
    private ServerConfig serverConfig;

    @Anonymous
    @ApiOperation(value = "首页视频轮播", notes = "不需要登录。data 为数组，按 sort 升序。空数组时 App 用本地默认图。")
    @GetMapping
    public AppCarouselListResult list()
    {
        List<BizCarousel> items = carouselService.selectAppCarouselList();
        List<AppCarouselItem> rows = new ArrayList<AppCarouselItem>();
        for (int i = 0; i < items.size(); i++)
        {
            rows.add(toItem(items.get(i)));
        }
        return AppCarouselListResult.ok(rows);
    }

    private AppCarouselItem toItem(BizCarousel carousel)
    {
        AppCarouselItem item = new AppCarouselItem();
        item.setCarouselId(carousel.getCarouselId());
        item.setTitle(carousel.getTitle() == null ? "" : carousel.getTitle());
        item.setVideoUrl(toPublicUrl(carousel.getVideoUrl()));
        item.setCoverUrl(toPublicUrl(carousel.getCoverUrl()));
        item.setSort(carousel.getSort());
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
}
