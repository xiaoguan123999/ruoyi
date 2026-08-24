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

@Api(tags = "App-资金流水")
@RestController
@RequestMapping("/app")
public class AppWalletLogController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @ApiOperation(value = "充值余额/交易记录", notes = "分页。资金明细「充值余额」Tab 用。收入为正、支出为负。可选 currency、bizType。别名 /app/wallet/logs 、 /app/funds")
    @GetMapping({"/walletLog", "/wallet/logs", "/funds"})
    public TableDataInfo list(String currency, String bizType)
    {
        startPage();
        List<AppWalletLogItem> list = walletService.selectAppWalletLogList(
                AppSecurityUtils.getMemberId(), currency, bizType);
        return getDataTable(list);
    }
}
