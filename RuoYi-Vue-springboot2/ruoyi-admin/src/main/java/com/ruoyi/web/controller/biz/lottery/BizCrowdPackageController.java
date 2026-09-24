package com.ruoyi.web.controller.biz.lottery;

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
import com.ruoyi.biz.domain.BizCrowdPackage;
import com.ruoyi.biz.service.IBizCrowdPackageService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-标签人群")
@RestController
@RequestMapping("/biz/crowdPackage")
public class BizCrowdPackageController extends BaseController
{
    @Autowired
    private IBizCrowdPackageService crowdPackageService;

    @ApiOperation("人群包列表")
    @PreAuthorize("@ss.hasPermi('biz:crowdPackage:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCrowdPackage query)
    {
        startPage();
        List<BizCrowdPackage> list = crowdPackageService.selectCrowdPackageList(query);
        return getDataTable(list);
    }

    @ApiOperation("启用人群包下拉")
    @PreAuthorize("@ss.hasAnyPermi('biz:crowdPackage:query,biz:lotteryActivity:query,biz:lotteryStrategy:edit')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(crowdPackageService.selectEnabledOptions());
    }

    @ApiOperation("人群包详情")
    @PreAuthorize("@ss.hasPermi('biz:crowdPackage:query')")
    @GetMapping("/{packageId}")
    public AjaxResult getInfo(@PathVariable Long packageId)
    {
        return success(crowdPackageService.selectCrowdPackageById(packageId));
    }

    @ApiOperation("新增人群包")
    @PreAuthorize("@ss.hasPermi('biz:crowdPackage:add')")
    @Log(title = "标签人群", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizCrowdPackage crowdPackage)
    {
        crowdPackage.setCreateBy(getUsername());
        return toAjax(crowdPackageService.insertCrowdPackage(crowdPackage));
    }

    @ApiOperation("修改人群包")
    @PreAuthorize("@ss.hasPermi('biz:crowdPackage:edit')")
    @Log(title = "标签人群", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizCrowdPackage crowdPackage)
    {
        crowdPackage.setUpdateBy(getUsername());
        return toAjax(crowdPackageService.updateCrowdPackage(crowdPackage));
    }

    @ApiOperation("删除人群包")
    @PreAuthorize("@ss.hasPermi('biz:crowdPackage:remove')")
    @Log(title = "标签人群", businessType = BusinessType.DELETE)
    @DeleteMapping("/{packageIds}")
    public AjaxResult remove(@PathVariable Long[] packageIds)
    {
        return toAjax(crowdPackageService.deleteCrowdPackageByIds(packageIds));
    }
}
