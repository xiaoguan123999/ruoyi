package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppWalletLogItem;
import com.ruoyi.biz.service.IBizWalletService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "App-资金流水")
@RestController
@RequestMapping("/app")
public class AppWalletLogController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @ApiOperation(value = "资金明细", notes = "分页。收入为正、支出为负。bizType 只过滤流水业务类型，typeCode 只过滤钱包类型，互不转换。两者可同时传。")
    @GetMapping({"/walletLog", "/wallet/logs", "/funds"})
    public TableDataInfo list(
            @ApiParam("币种 CNY / USDT") String currency,
            @ApiParam("业务类型，精确值，多个逗号分隔。如 RECHARGE、REBATE、SUBSCRIBE、ADJUST、WITHDRAW_FREEZE") String bizType,
            @ApiParam("钱包类型，精确值。如 BALANCE、PRODUCT、PROMO、ASSIST") String typeCode)
    {
        startPage();
        List<AppWalletLogItem> list = walletService.selectAppWalletLogList(
                AppSecurityUtils.getMemberId(), currency, bizType, typeCode);
        return getDataTable(list);
    }
}
