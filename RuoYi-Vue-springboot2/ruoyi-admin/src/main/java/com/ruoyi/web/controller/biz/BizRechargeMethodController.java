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
import com.ruoyi.biz.domain.BizRechargeMethod;
import com.ruoyi.biz.service.IBizRechargeMethodService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-充值方式")
@RestController
@RequestMapping("/biz/rechargeMethod")
public class BizRechargeMethodController extends BaseController
{
    @Autowired
    private IBizRechargeMethodService rechargeMethodService;

    @ApiOperation("充值方式列表")
    @PreAuthorize("@ss.hasPermi('biz:rechargeMethod:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizRechargeMethod query)
    {
        startPage();
        List<BizRechargeMethod> list = rechargeMethodService.selectRechargeMethodList(query);
        return getDataTable(list);
    }

    @ApiOperation("充值方式详情")
    @PreAuthorize("@ss.hasPermi('biz:rechargeMethod:query')")
    @GetMapping("/{methodId}")
    public AjaxResult getInfo(@PathVariable Long methodId)
    {
        return success(rechargeMethodService.selectRechargeMethodById(methodId));
    }

    @ApiOperation("新增充值方式")
    @PreAuthorize("@ss.hasPermi('biz:rechargeMethod:add')")
    @Log(title = "充值方式", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizRechargeMethod row)
    {
        row.setCreateBy(getUsername());
        return toAjax(rechargeMethodService.insertRechargeMethod(row));
    }

    @ApiOperation("修改充值方式")
    @PreAuthorize("@ss.hasPermi('biz:rechargeMethod:edit')")
    @Log(title = "充值方式", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizRechargeMethod row)
    {
        row.setUpdateBy(getUsername());
        return toAjax(rechargeMethodService.updateRechargeMethod(row));
    }

    @ApiOperation("删除充值方式")
    @PreAuthorize("@ss.hasPermi('biz:rechargeMethod:remove')")
    @Log(title = "充值方式", businessType = BusinessType.DELETE)
    @DeleteMapping("/{methodIds}")
    public AjaxResult remove(@PathVariable Long[] methodIds)
    {
        return toAjax(rechargeMethodService.deleteRechargeMethodByIds(methodIds));
    }
}
