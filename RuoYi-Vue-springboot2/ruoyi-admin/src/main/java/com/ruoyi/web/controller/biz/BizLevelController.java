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
import com.ruoyi.biz.domain.BizLevel;
import com.ruoyi.biz.service.IBizLevelService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-会员等级")
@RestController
@RequestMapping("/biz/level")
public class BizLevelController extends BaseController
{
    @Autowired
    private IBizLevelService levelService;

    @ApiOperation("等级列表")
    @PreAuthorize("@ss.hasPermi('biz:level:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizLevel level)
    {
        startPage();
        List<BizLevel> list = levelService.selectLevelList(level);
        return getDataTable(list);
    }

    @ApiOperation("等级详情")
    @PreAuthorize("@ss.hasPermi('biz:level:query')")
    @GetMapping("/{levelId}")
    public AjaxResult getInfo(@PathVariable Long levelId)
    {
        return success(levelService.selectLevelById(levelId));
    }

    @ApiOperation("新增等级")
    @PreAuthorize("@ss.hasPermi('biz:level:add')")
    @Log(title = "会员等级", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizLevel level)
    {
        level.setCreateBy(getUsername());
        return toAjax(levelService.insertLevel(level));
    }

    @ApiOperation("修改等级")
    @PreAuthorize("@ss.hasPermi('biz:level:edit')")
    @Log(title = "会员等级", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizLevel level)
    {
        level.setUpdateBy(getUsername());
        return toAjax(levelService.updateLevel(level));
    }

    @ApiOperation("删除等级")
    @PreAuthorize("@ss.hasPermi('biz:level:remove')")
    @Log(title = "会员等级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{levelIds}")
    public AjaxResult remove(@PathVariable Long[] levelIds)
    {
        return toAjax(levelService.deleteLevelByIds(levelIds));
    }
}
