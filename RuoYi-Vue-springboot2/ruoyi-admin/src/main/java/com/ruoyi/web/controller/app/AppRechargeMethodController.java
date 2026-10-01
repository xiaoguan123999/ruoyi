package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppPayChannelListResult;
import com.ruoyi.biz.api.AppRechargeMethodListResult;
import com.ruoyi.biz.service.IBizRechargeMethodService;
import com.ruoyi.common.core.controller.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-充值方式")
@RestController
@RequestMapping("/app/recharge")
public class AppRechargeMethodController extends BaseController
{
    @Autowired
    private IBizRechargeMethodService rechargeMethodService;

    @ApiOperation("支付方式列表（不含通道）")
    @GetMapping("/methods")
    public AppRechargeMethodListResult methods()
    {
        return AppRechargeMethodListResult.ok(rechargeMethodService.listAppMethods());
    }

    @ApiOperation("按支付方式编码查询可用支付通道")
    @GetMapping("/methods/{methodCode}/channels")
    public AppPayChannelListResult channels(@PathVariable String methodCode)
    {
        return AppPayChannelListResult.ok(rechargeMethodService.listAppChannelsByMethodCode(methodCode));
    }
}
