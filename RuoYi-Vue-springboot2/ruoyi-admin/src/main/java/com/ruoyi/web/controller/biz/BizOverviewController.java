package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizOverview;
import com.ruoyi.biz.service.IBizOverviewService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-运行概览")
@RestController
@RequestMapping("/biz/overview")
public class BizOverviewController extends BaseController
{
    @Autowired
    private IBizOverviewService overviewService;

    @ApiOperation("概览列表")
    @PreAuthorize("@ss.hasPermi('biz:overview:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizOverview overview)
    {
        startPage();
        List<BizOverview> list = overviewService.selectOverviewList(overview);
        return getDataTable(list);
    }

    @ApiOperation("概览详情")
    @PreAuthorize("@ss.hasPermi('biz:overview:query')")
    @GetMapping("/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return success(overviewService.selectOverviewById(itemId));
    }

    @ApiOperation("新增概览")
    @PreAuthorize("@ss.hasPermi('biz:overview:add')")
    @Log(title = "运行概览", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizOverview overview)
    {
        overview.setCreateBy(getUsername());
        return toAjax(overviewService.insertOverview(overview));
    }

    @ApiOperation("修改概览")
    @PreAuthorize("@ss.hasPermi('biz:overview:edit')")
    @Log(title = "运行概览", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizOverview overview)
    {
        overview.setUpdateBy(getUsername());
        return toAjax(overviewService.updateOverview(overview));
    }

    @ApiOperation("删除概览")
    @PreAuthorize("@ss.hasPermi('biz:overview:remove')")
    @Log(title = "运行概览", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(overviewService.deleteOverviewByIds(itemIds));
    }
}
