package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizOverview;
import com.ruoyi.biz.service.IBizOverviewService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * App 首页运行概览（展示数字，后台手改）
 */
@Api(tags = "App-运行概览")
@RestController
@RequestMapping("/app/overview")
public class AppOverviewController extends BaseController
{
    @Autowired
    private IBizOverviewService overviewService;

    @Anonymous
    @ApiOperation("运行概览")
    @GetMapping
    public AjaxResult list()
    {
        List<BizOverview> items = overviewService.selectAppOverviewList();
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (BizOverview item : items)
        {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("itemKey", item.getItemKey());
            row.put("title", item.getTitle());
            row.put("displayValue", item.getDisplayValue());
            row.put("statusText", item.getStatusText());
            row.put("statusColor", item.getStatusColor());
            row.put("imageUrl", item.getImageUrl() == null ? "" : item.getImageUrl());
            row.put("sort", item.getSort());
            rows.add(row);
        }
        return success(rows);
    }
}
