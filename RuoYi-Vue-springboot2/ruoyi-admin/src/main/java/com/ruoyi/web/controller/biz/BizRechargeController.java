package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizAuditBody;
import com.ruoyi.biz.domain.BizRecharge;
import com.ruoyi.biz.service.IBizRechargeService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-充值审核")
@RestController
@RequestMapping("/biz/recharge")
public class BizRechargeController extends BaseController
{
    @Autowired
    private IBizRechargeService rechargeService;

    @ApiOperation("充值列表")
    @PreAuthorize("@ss.hasPermi('biz:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizRecharge recharge)
    {
        startPage();
        List<BizRecharge> list = rechargeService.selectRechargeList(recharge);
        return getDataTable(list);
    }

    @ApiOperation("充值详情")
    @PreAuthorize("@ss.hasPermi('biz:recharge:query')")
    @GetMapping("/{rechargeId}")
    public AjaxResult getInfo(@PathVariable Long rechargeId)
    {
        return success(rechargeService.selectRechargeById(rechargeId));
    }

    @ApiOperation("代客申请充值")
    @PreAuthorize("@ss.hasPermi('biz:recharge:add')")
    @Log(title = "充值申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizRecharge recharge)
    {
        String currency = StringUtils.isEmpty(recharge.getCurrency()) ? BizConstants.CURRENCY_CNY : recharge.getCurrency();
        return success(rechargeService.apply(recharge.getMemberId(), currency, recharge.getAmount(), recharge.getRemark()));
    }

    @ApiOperation("审核充值")
    @PreAuthorize("@ss.hasPermi('biz:recharge:audit')")
    @Log(title = "充值审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody BizAuditBody body)
    {
        rechargeService.audit(body.getId(), body.getStatus(), getUsername(), body.getAuditRemark());
        return success();
    }
}
