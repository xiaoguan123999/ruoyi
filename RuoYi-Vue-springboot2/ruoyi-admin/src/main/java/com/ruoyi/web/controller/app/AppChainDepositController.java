package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppChainDepositBody;
import com.ruoyi.biz.api.AppChainDepositConfigResult;
import com.ruoyi.biz.api.AppChainDepositOrderResult;
import com.ruoyi.biz.service.IBizChainDepositService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-链上充值")
@RestController
@RequestMapping("/app/chain/deposit")
public class AppChainDepositController extends BaseController
{
    @Autowired
    private IBizChainDepositService chainDepositService;

    @ApiOperation("按当前用户解析收款地址。enabled=false 时隐藏入口。networks[] 含 TRC20/BEP20")
    @GetMapping("/config")
    public AppChainDepositConfigResult config()
    {
        return AppChainDepositConfigResult.ok(chainDepositService.getAppConfig(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("创建链上充值单，body.network=TRC20|BEP20，返回必须转账的6位小数金额和收款地址")
    @PostMapping
    public AppChainDepositOrderResult create(@RequestBody AppChainDepositBody body)
    {
        if (body == null || body.getAmount() == null)
        {
            return AppChainDepositOrderResult.fail("请填写金额");
        }
        return AppChainDepositOrderResult.ok(
                chainDepositService.create(AppSecurityUtils.getMemberId(), body.getAmount(), body.getNetwork()));
    }

    @ApiOperation("查询链上充值单，建议每5秒轮询，status=1 已入账。倒计时用 expireTime")
    @GetMapping("/order")
    public AppChainDepositOrderResult order(@RequestParam("outTradeNo") String outTradeNo)
    {
        return AppChainDepositOrderResult.ok(
                chainDepositService.getOrder(AppSecurityUtils.getMemberId(), outTradeNo));
    }
}
