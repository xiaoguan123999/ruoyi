package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizWallet;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 后台-会员钱包余额总览
 */
@Api(tags = "后台-会员钱包")
@RestController
@RequestMapping("/biz/wallet")
public class BizWalletController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @ApiOperation("会员钱包余额列表（按会员/手机号/类型/币种筛选）")
    @PreAuthorize("@ss.hasPermi('biz:wallet:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWallet query)
    {
        startPage();
        List<BizWallet> list = walletService.selectWalletList(query);
        return getDataTable(list);
    }
}
