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
import com.ruoyi.biz.domain.BizBlacklist;
import com.ruoyi.biz.domain.BizBlacklistLog;
import com.ruoyi.biz.service.IBizBlacklistService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-黑名单")
@RestController
@RequestMapping("/biz/blacklist")
public class BizBlacklistController extends BaseController
{
    @Autowired
    private IBizBlacklistService blacklistService;

    @ApiOperation("黑名单列表")
    @PreAuthorize("@ss.hasPermi('biz:blacklist:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizBlacklist query)
    {
        startPage();
        List<BizBlacklist> list = blacklistService.selectBlacklistList(query);
        return getDataTable(list);
    }

    @ApiOperation("黑名单详情")
    @PreAuthorize("@ss.hasPermi('biz:blacklist:query')")
    @GetMapping("/{blacklistId}")
    public AjaxResult getInfo(@PathVariable Long blacklistId)
    {
        return success(blacklistService.selectBlacklistById(blacklistId));
    }

    @ApiOperation("新增黑名单")
    @PreAuthorize("@ss.hasPermi('biz:blacklist:add')")
    @Log(title = "黑名单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizBlacklist row)
    {
        row.setCreateBy(getUsername());
        return toAjax(blacklistService.insertBlacklist(row));
    }

    @ApiOperation("修改黑名单")
    @PreAuthorize("@ss.hasPermi('biz:blacklist:edit')")
    @Log(title = "黑名单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizBlacklist row)
    {
        row.setUpdateBy(getUsername());
        return toAjax(blacklistService.updateBlacklist(row));
    }

    @ApiOperation("删除黑名单")
    @PreAuthorize("@ss.hasPermi('biz:blacklist:remove')")
    @Log(title = "黑名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{blacklistIds}")
    public AjaxResult remove(@PathVariable Long[] blacklistIds)
    {
        return toAjax(blacklistService.deleteBlacklistByIds(blacklistIds));
    }

    @ApiOperation("拦截记录")
    @PreAuthorize("@ss.hasPermi('biz:blacklistLog:list')")
    @GetMapping("/log/list")
    public TableDataInfo logs(BizBlacklistLog query)
    {
        startPage();
        List<BizBlacklistLog> list = blacklistService.selectLogList(query);
        return getDataTable(list);
    }

    @ApiOperation("删除拦截记录")
    @PreAuthorize("@ss.hasPermi('biz:blacklistLog:remove')")
    @Log(title = "黑名单记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/log/{logIds}")
    public AjaxResult removeLogs(@PathVariable Long[] logIds)
    {
        return toAjax(blacklistService.deleteLogByIds(logIds));
    }
}
