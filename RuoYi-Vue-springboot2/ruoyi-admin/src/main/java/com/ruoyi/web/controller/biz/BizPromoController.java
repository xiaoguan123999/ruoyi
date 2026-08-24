package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizPromoGrant;
import com.ruoyi.biz.domain.BizPromoRule;
import com.ruoyi.biz.service.IBizPromoService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-注册推广规则")
@RestController
@RequestMapping("/biz/promo")
public class BizPromoController extends BaseController
{
    @Autowired
    private IBizPromoService promoService;

    @ApiOperation("注册推广规则")
    @PreAuthorize("@ss.hasPermi('biz:promo:query')")
    @GetMapping("/rule")
    public AjaxResult rule()
    {
        return success(promoService.getRule());
    }

    @ApiOperation("保存注册推广规则")
    @PreAuthorize("@ss.hasPermi('biz:promo:edit')")
    @Log(title = "注册推广规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult saveRule(@RequestBody BizPromoRule rule)
    {
        promoService.saveRule(rule);
        return success();
    }

    @ApiOperation("奖励发放记录")
    @PreAuthorize("@ss.hasPermi('biz:promo:query')")
    @GetMapping("/grant/list")
    public TableDataInfo grantList(BizPromoGrant grant)
    {
        startPage();
        List<BizPromoGrant> list = promoService.selectGrantList(grant);
        return getDataTable(list);
    }
}
