package com.ruoyi.web.controller.biz;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.domain.BizWalletAdjustBody;
import com.ruoyi.biz.domain.BizWalletLog;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-资金流水")
@RestController
@RequestMapping("/biz/walletLog")
public class BizWalletLogController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @Autowired
    private IBizMemberService memberService;

    @ApiOperation("资金流水")
    @PreAuthorize("@ss.hasPermi('biz:walletLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWalletLog log)
    {
        startPage();
        List<BizWalletLog> list = walletService.selectWalletLogList(log);
        return getDataTable(list);
    }

    @ApiOperation("调账前查看钱包余额")
    @PreAuthorize("@ss.hasPermi('biz:wallet:adjust')")
    @GetMapping("/balance")
    public AjaxResult balance(@RequestParam Long memberId,
            @RequestParam(required = false) String typeCode,
            @RequestParam String currency)
    {
        BizWallet wallet = walletService.getWallet(memberId, typeCode, currency);
        AjaxResult ajax = success();
        ajax.put("available", wallet == null || wallet.getAvailable() == null ? BigDecimal.ZERO : wallet.getAvailable());
        ajax.put("frozen", wallet == null || wallet.getFrozen() == null ? BigDecimal.ZERO : wallet.getFrozen());
        ajax.put("typeCode", typeCode);
        ajax.put("currency", currency);
        return ajax;
    }

    @ApiOperation("钱包调账")
    @PreAuthorize("@ss.hasPermi('biz:wallet:adjust')")
    @Log(title = "钱包调账", businessType = BusinessType.UPDATE)
    @PutMapping("/adjust")
    public AjaxResult adjust(@RequestBody BizWalletAdjustBody body)
    {
        if (body == null || body.getMemberId() == null)
        {
            return error("请选择会员");
        }
        BizMember member = memberService.selectMemberById(body.getMemberId());
        if (member == null)
        {
            return error("会员不存在");
        }
        walletService.adjust(body.getMemberId(), body.getTypeCode(), body.getCurrency(), body.getDirection(),
                body.getAmount(), body.getRemark(), getUsername());
        return success();
    }
}
