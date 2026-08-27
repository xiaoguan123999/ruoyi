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
import com.ruoyi.biz.domain.BizWalletCreditRule;
import com.ruoyi.biz.service.IBizWalletCreditRuleService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-奖励入账")
@RestController
@RequestMapping("/biz/walletCredit")
public class BizWalletCreditRuleController extends BaseController
{
    @Autowired
    private IBizWalletCreditRuleService creditRuleService;

    private static final String FEATURE_CREDIT_PERMS =
            "biz:walletCredit:query,biz:walletCredit:list,biz:walletCredit:edit,"
            + "biz:checkin:rule,biz:kycReward:query,biz:kycReward:edit,biz:promo:query,biz:promo:edit,"
            + "biz:levelReward:query,biz:levelReward:edit,biz:recharge:list,biz:recharge:audit,"
            + "biz:product:list,biz:product:edit,biz:commission:list,biz:member:list,"
            + "biz:withdraw:list,biz:withdraw:audit";
    @ApiOperation("入账配置列表")
    @PreAuthorize("@ss.hasPermi('biz:walletCredit:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWalletCreditRule rule)
    {
        startPage();
        List<BizWalletCreditRule> list = creditRuleService.selectCreditRuleList(rule);
        return getDataTable(list);
    }

    @ApiOperation("按业务查到账钱包")
    @PreAuthorize("@ss.hasAnyPermi('" + FEATURE_CREDIT_PERMS + "')")
    @GetMapping("/byBiz/{bizType}")
    public AjaxResult getByBiz(@PathVariable String bizType)
    {
        return success(creditRuleService.selectCreditRuleByBizType(bizType));
    }

    @ApiOperation("按业务保存到账钱包")
    @PreAuthorize("@ss.hasAnyPermi('" + FEATURE_CREDIT_PERMS + "')")
    @Log(title = "奖励入账", businessType = BusinessType.UPDATE)
    @PutMapping("/byBiz/{bizType}")
    public AjaxResult saveByBiz(@PathVariable String bizType, @RequestBody BizWalletCreditRule body)
    {
        String typeCode = body == null ? null : body.getTypeCode();
        creditRuleService.saveTypeCodeByBizType(bizType, typeCode, getUsername());
        return success();
    }

    @ApiOperation("入账配置详情")
    @PreAuthorize("@ss.hasPermi('biz:walletCredit:query')")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return success(creditRuleService.selectCreditRuleById(ruleId));
    }

    @ApiOperation("新增入账配置")
    @PreAuthorize("@ss.hasPermi('biz:walletCredit:add')")
    @Log(title = "奖励入账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizWalletCreditRule rule)
    {
        rule.setCreateBy(getUsername());
        return toAjax(creditRuleService.insertCreditRule(rule));
    }

    @ApiOperation("修改入账配置")
    @PreAuthorize("@ss.hasPermi('biz:walletCredit:edit')")
    @Log(title = "奖励入账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizWalletCreditRule rule)
    {
        rule.setUpdateBy(getUsername());
        return toAjax(creditRuleService.updateCreditRule(rule));
    }

    @ApiOperation("删除入账配置")
    @PreAuthorize("@ss.hasPermi('biz:walletCredit:remove')")
    @Log(title = "奖励入账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(creditRuleService.deleteCreditRuleByIds(ruleIds));
    }
}
