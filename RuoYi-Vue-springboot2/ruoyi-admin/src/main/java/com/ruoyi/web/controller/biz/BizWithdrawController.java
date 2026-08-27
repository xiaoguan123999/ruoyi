package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizAuditBody;
import com.ruoyi.biz.domain.BizWithdraw;
import com.ruoyi.biz.domain.BizWithdrawRule;
import com.ruoyi.biz.service.IBizWithdrawService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-提现审核")
@RestController
@RequestMapping("/biz/withdraw")
public class BizWithdrawController extends BaseController
{
    @Autowired
    private IBizWithdrawService withdrawService;

    @ApiOperation("提现规则")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:list')")
    @GetMapping("/config")
    public AjaxResult config()
    {
        return success(withdrawService.getRule());
    }

    @ApiOperation("保存提现规则")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:audit')")
    @Log(title = "提现规则", businessType = BusinessType.UPDATE)
    @PutMapping("/config")
    public AjaxResult saveConfig(@RequestBody BizWithdrawRule rule)
    {
        withdrawService.saveRule(rule);
        return success();
    }

    @ApiOperation("提现列表")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWithdraw withdraw)
    {
        startPage();
        List<BizWithdraw> list = withdrawService.selectWithdrawList(withdraw);
        return getDataTable(list);
    }

    @ApiOperation("提现详情")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:query')")
    @GetMapping("/{withdrawId}")
    public AjaxResult getInfo(@PathVariable Long withdrawId)
    {
        return success(withdrawService.selectWithdrawById(withdrawId));
    }

    @ApiOperation("审核提现")
    @PreAuthorize("@ss.hasPermi('biz:withdraw:audit')")
    @Log(title = "提现审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody BizAuditBody body)
    {
        withdrawService.audit(body.getId(), body.getStatus(), getUsername(), body.getAuditRemark(), body.getPayProofUrl());
        return success();
    }
}
