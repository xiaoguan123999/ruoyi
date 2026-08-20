package com.ruoyi.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.BizWalletLog;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "后台-资金流水")
@RestController
@RequestMapping("/biz/walletLog")
public class BizWalletLogController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @ApiOperation("资金流水")
    @PreAuthorize("@ss.hasPermi('biz:walletLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizWalletLog log)
    {
        startPage();
        List<BizWalletLog> list = walletService.selectWalletLogList(log);
        return getDataTable(list);
    }
}
