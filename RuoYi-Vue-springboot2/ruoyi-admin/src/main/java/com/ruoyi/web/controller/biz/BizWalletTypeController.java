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
import com.ruoyi.biz.domain.BizWalletType;
import com.ruoyi.biz.service.IBizWalletTypeService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-钱包类型")
@RestController
@RequestMapping("/biz/walletType")
public class BizWalletTypeController extends BaseController
{
    @Autowired
    private IBizWalletTypeService walletTypeService;

    @ApiOperation("钱包类型列表")
    @PreAuthorize("@ss.hasPermi('biz:walletType:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWalletType type)
    {
        startPage();
        List<BizWalletType> list = walletTypeService.selectWalletTypeList(type);
        return getDataTable(list);
    }

    @ApiOperation("钱包类型下拉")
    @PreAuthorize("@ss.hasAnyPermi('biz:walletType:list,biz:walletCredit:list,biz:wallet:adjust,biz:walletLog:list,biz:member:list,biz:checkin:rule,biz:kycReward:query,biz:promo:query,biz:levelReward:query,biz:recharge:list,biz:product:list,biz:commission:list,biz:withdraw:list,biz:withdraw:audit')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(walletTypeService.selectWalletTypeList(new BizWalletType()));
    }

    @ApiOperation("钱包类型详情")
    @PreAuthorize("@ss.hasPermi('biz:walletType:query')")
    @GetMapping("/{typeId}")
    public AjaxResult getInfo(@PathVariable Long typeId)
    {
        return success(walletTypeService.selectWalletTypeById(typeId));
    }

    @ApiOperation("新增钱包类型")
    @PreAuthorize("@ss.hasPermi('biz:walletType:add')")
    @Log(title = "钱包类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizWalletType type)
    {
        type.setCreateBy(getUsername());
        return toAjax(walletTypeService.insertWalletType(type));
    }

    @ApiOperation("修改钱包类型")
    @PreAuthorize("@ss.hasPermi('biz:walletType:edit')")
    @Log(title = "钱包类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizWalletType type)
    {
        type.setUpdateBy(getUsername());
        return toAjax(walletTypeService.updateWalletType(type));
    }

    @ApiOperation("删除钱包类型")
    @PreAuthorize("@ss.hasPermi('biz:walletType:remove')")
    @Log(title = "钱包类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{typeIds}")
    public AjaxResult remove(@PathVariable Long[] typeIds)
    {
        return toAjax(walletTypeService.deleteWalletTypeByIds(typeIds));
    }
}
