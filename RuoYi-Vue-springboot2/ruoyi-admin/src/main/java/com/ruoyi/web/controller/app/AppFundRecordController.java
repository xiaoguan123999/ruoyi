package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppFundRecordItem;
import com.ruoyi.biz.service.IBizFundRecordService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.AppSecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-资金申请记录")
@RestController
@RequestMapping("/app")
public class AppFundRecordController extends BaseController
{
    @Autowired
    private IBizFundRecordService fundRecordService;

    @ApiOperation(value = "充值/提现申请记录", notes = "分页。返回待审、通过、拒绝全部状态，不是余额流水。type/bizType：RECHARGE 充值、WITHDRAW 提现，不传则两类都返回。可选 currency、status（0待处理 1成功 2拒绝）。别名 /app/fund/records 、 /app/bills")
    @GetMapping({"/fundRecords", "/fund/records", "/bills"})
    public TableDataInfo list(String currency, String bizType, String type, String status)
    {
        String filter = StringUtils.isNotEmpty(bizType) ? bizType : type;
        startPage();
        List<AppFundRecordItem> list = fundRecordService.selectAppFundRecords(
                AppSecurityUtils.getMemberId(), currency, filter, status);
        return getDataTable(list);
    }
}
