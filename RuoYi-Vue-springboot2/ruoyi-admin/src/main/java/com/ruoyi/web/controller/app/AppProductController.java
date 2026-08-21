package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppProductListResult;
import com.ruoyi.biz.api.AppProductSeries;
import com.ruoyi.biz.api.AppProductSeriesListResult;
import com.ruoyi.biz.api.AppProductSeriesResult;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizProduct;
import com.ruoyi.biz.domain.BizProductCategory;
import com.ruoyi.biz.service.IBizProductCategoryService;
import com.ruoyi.biz.service.IBizProductService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "App-产品")
@RestController
@RequestMapping("/app")
public class AppProductController extends BaseController
{
    @Autowired
    private IBizProductCategoryService categoryService;

    @Autowired
    private IBizProductService productService;

    @Autowired
    private ServerConfig serverConfig;

    @ApiOperation(value = "产品系列列表", notes = "Tab 渲染系列卡片。点某一项再调产品列表，带 seriesId。coverUrl 为空时 App 用本地默认图。")
    @GetMapping("/product/series")
    public AppProductSeriesListResult series()
    {
        List<BizProductCategory> items = categoryService.selectAppSeriesList();
        List<AppProductSeries> rows = new ArrayList<AppProductSeries>();
        for (int i = 0; i < items.size(); i++)
        {
            rows.add(toSeries(items.get(i)));
        }
        return AppProductSeriesListResult.ok(rows);
    }

    @ApiOperation(value = "产品系列详情", notes = "系列页标题、封面。不存在或已隐藏返回 500。")
    @GetMapping("/product/series/{seriesId}")
    public AppProductSeriesResult seriesDetail(@PathVariable Long seriesId)
    {
        BizProductCategory category = categoryService.selectCategoryById(seriesId);
        if (category == null || !BizConstants.STATUS_OK.equals(category.getStatus()))
        {
            return AppProductSeriesResult.fail("系列不存在或已关闭");
        }
        return AppProductSeriesResult.ok(toSeries(category));
    }

    @ApiOperation(value = "产品列表", notes = "不带 seriesId 返回全部上架产品。带 seriesId（或 categoryId）只返回该系列下产品。")
    @GetMapping("/products")
    public AppProductListResult products(
            @ApiParam("系列ID") @RequestParam(value = "seriesId", required = false) Long seriesId,
            @ApiParam("分类ID，同 seriesId") @RequestParam(value = "categoryId", required = false) Long categoryId)
    {
        Long cid = seriesId != null ? seriesId : categoryId;
        BizProduct query = new BizProduct();
        query.setStatus(BizConstants.STATUS_OK);
        query.setCategoryId(cid);
        List<BizProduct> list = productService.selectProductList(query);
        for (int i = 0; i < list.size(); i++)
        {
            BizProduct item = list.get(i);
            item.setCoverUrl(toPublicUrl(item.getCoverUrl()));
        }
        return AppProductListResult.ok(list);
    }

    private AppProductSeries toSeries(BizProductCategory category)
    {
        AppProductSeries item = new AppProductSeries();
        item.setSeriesId(category.getCategoryId());
        item.setSeriesName(category.getCategoryName());
        item.setCoverUrl(toPublicUrl(category.getCoverUrl()));
        item.setSort(category.getSort());
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
