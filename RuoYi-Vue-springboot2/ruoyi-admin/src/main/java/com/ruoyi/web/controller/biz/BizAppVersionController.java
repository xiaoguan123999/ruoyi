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
import com.ruoyi.biz.domain.BizAppVersion;
import com.ruoyi.biz.service.IBizAppVersionService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-App版本")
@RestController
@RequestMapping("/biz/appVersion")
public class BizAppVersionController extends BaseController
{
    @Autowired
    private IBizAppVersionService versionService;

    @ApiOperation("版本列表")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizAppVersion query)
    {
        startPage();
        List<BizAppVersion> list = versionService.selectVersionList(query);
        return getDataTable(list);
    }

    @ApiOperation("版本详情")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:query')")
    @GetMapping("/{versionId}")
    public AjaxResult getInfo(@PathVariable Long versionId)
    {
        return success(versionService.selectVersionById(versionId));
    }

    @ApiOperation("新增版本")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:add')")
    @Log(title = "App版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizAppVersion row)
    {
        row.setCreateBy(getUsername());
        return toAjax(versionService.insertVersion(row));
    }

    @ApiOperation("修改版本")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:edit')")
    @Log(title = "App版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizAppVersion row)
    {
        row.setUpdateBy(getUsername());
        return toAjax(versionService.updateVersion(row));
    }

    @ApiOperation("设为/取消最新版本")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:edit')")
    @Log(title = "App版本", businessType = BusinessType.UPDATE)
    @PostMapping("/{versionId}/latest")
    public AjaxResult latest(@PathVariable Long versionId, @RequestBody BizAppVersion body)
    {
        boolean on = body != null && (body.latest() || Boolean.TRUE.equals(body.getLatestFlag()));
        return toAjax(versionService.setLatest(versionId, on));
    }

    @ApiOperation("设为/取消强制更新")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:edit')")
    @Log(title = "App版本", businessType = BusinessType.UPDATE)
    @PostMapping("/{versionId}/forceUpdate")
    public AjaxResult forceUpdate(@PathVariable Long versionId, @RequestBody BizAppVersion body)
    {
        boolean on = body != null && (body.force() || Boolean.TRUE.equals(body.getForceUpdateFlag()));
        return toAjax(versionService.setForceUpdate(versionId, on));
    }

    @ApiOperation("启用/禁用")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:edit')")
    @Log(title = "App版本", businessType = BusinessType.UPDATE)
    @PostMapping("/{versionId}/enabled")
    public AjaxResult enabled(@PathVariable Long versionId, @RequestBody BizAppVersion body)
    {
        boolean on = body != null && (body.on() || Boolean.TRUE.equals(body.getEnabledFlag()));
        return toAjax(versionService.setEnabled(versionId, on));
    }

    @ApiOperation("删除版本")
    @PreAuthorize("@ss.hasPermi('biz:appVersion:remove')")
    @Log(title = "App版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{versionIds}")
    public AjaxResult remove(@PathVariable Long[] versionIds)
    {
        return toAjax(versionService.deleteVersionByIds(versionIds));
    }
}
