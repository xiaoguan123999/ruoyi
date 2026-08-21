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
import com.ruoyi.biz.domain.BizAbout;
import com.ruoyi.biz.service.IBizAboutService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-关于我们")
@RestController
@RequestMapping("/biz/about")
public class BizAboutController extends BaseController
{
    @Autowired
    private IBizAboutService aboutService;

    @ApiOperation("关于我们列表")
    @PreAuthorize("@ss.hasPermi('biz:about:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizAbout about)
    {
        startPage();
        List<BizAbout> list = aboutService.selectAboutList(about);
        return getDataTable(list);
    }

    @ApiOperation("关于我们详情")
    @PreAuthorize("@ss.hasPermi('biz:about:query')")
    @GetMapping("/{aboutId}")
    public AjaxResult getInfo(@PathVariable Long aboutId)
    {
        return success(aboutService.selectAboutById(aboutId));
    }

    @ApiOperation("新增关于我们")
    @PreAuthorize("@ss.hasPermi('biz:about:add')")
    @Log(title = "关于我们", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizAbout about)
    {
        about.setCreateBy(getUsername());
        return toAjax(aboutService.insertAbout(about));
    }

    @ApiOperation("修改关于我们")
    @PreAuthorize("@ss.hasPermi('biz:about:edit')")
    @Log(title = "关于我们", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizAbout about)
    {
        about.setUpdateBy(getUsername());
        return toAjax(aboutService.updateAbout(about));
    }

    @ApiOperation("删除关于我们")
    @PreAuthorize("@ss.hasPermi('biz:about:remove')")
    @Log(title = "关于我们", businessType = BusinessType.DELETE)
    @DeleteMapping("/{aboutIds}")
    public AjaxResult remove(@PathVariable Long[] aboutIds)
    {
        return toAjax(aboutService.deleteAboutByIds(aboutIds));
    }
}
