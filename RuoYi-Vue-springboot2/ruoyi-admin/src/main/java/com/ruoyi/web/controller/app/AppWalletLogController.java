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
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-资金流水")
@RestController
@RequestMapping("/app")
public class AppWalletLogController extends BaseController
{
    @Autowired
    private IBizWalletService walletService;

    @ApiOperation(value = "资金明细", notes = "分页。收入为正、支出为负。可选 currency、bizType。bizType=RECHARGE 充值记录，WITHDRAW 提现记录（冻结/成功/退回），也可传精确值如 SUBSCRIBE。多个用逗号分隔。别名参数 type。路径别名 /app/wallet/logs 、 /app/funds")
    @GetMapping({"/walletLog", "/wallet/logs", "/funds"})
    public TableDataInfo list(String currency, String bizType, String type)
    {
        String filter = StringUtils.isNotEmpty(bizType) ? bizType : type;
        startPage();
        List<AppWalletLogItem> list = walletService.selectAppWalletLogList(
                AppSecurityUtils.getMemberId(), currency, filter);
        return getDataTable(list);
    }
}
