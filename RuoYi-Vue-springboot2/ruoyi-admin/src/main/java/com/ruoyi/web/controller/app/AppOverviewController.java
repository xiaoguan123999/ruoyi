package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppOverviewItem;
import com.ruoyi.biz.api.AppOverviewResult;
import com.ruoyi.biz.domain.BizOverview;
import com.ruoyi.biz.service.IBizOverviewService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-运行概览")
@RestController
@RequestMapping("/app/overview")
public class AppOverviewController extends BaseController
{
    @Autowired
    private IBizOverviewService overviewService;

    @Anonymous
    @ApiOperation(value = "运行概览", notes = "data 为卡片数组。itemKey：satellite / coverage / terminal，给 App 匹配本地 3D 图。")
    @GetMapping
    public AppOverviewResult list()
    {
        List<BizOverview> items = overviewService.selectAppOverviewList();
        List<AppOverviewItem> rows = new ArrayList<AppOverviewItem>();
        for (BizOverview item : items)
        {
            AppOverviewItem row = new AppOverviewItem();
            row.setItemKey(item.getItemKey());
            row.setTitle(item.getTitle());
            row.setDisplayValue(item.getDisplayValue());
            row.setStatusText(item.getStatusText());
            row.setStatusColor(item.getStatusColor());
            row.setImageUrl(item.getImageUrl() == null ? "" : item.getImageUrl());
            row.setSort(item.getSort());
            rows.add(row);
        }
        return AppOverviewResult.ok(rows);
    }
}
