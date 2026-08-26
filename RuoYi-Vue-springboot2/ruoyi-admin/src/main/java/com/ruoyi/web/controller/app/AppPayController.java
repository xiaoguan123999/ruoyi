package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppPayChannelListResult;
import com.ruoyi.biz.api.AppPayDepositBody;
import com.ruoyi.biz.api.AppPayDepositResult;
import com.ruoyi.biz.service.IBizOnlinePayService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.AppSecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-线上充值")
@RestController
@RequestMapping("/app/pay")
public class AppPayController extends BaseController
{
    @Autowired
    private IBizOnlinePayService onlinePayService;

    @Autowired
    private ServerConfig serverConfig;

    @ApiOperation("可用支付通道")
    @GetMapping("/channels")
    public AppPayChannelListResult channels(@RequestParam(value = "scene", required = false) String scene)
    {
        return AppPayChannelListResult.ok(onlinePayService.listAppChannels(scene));
    }

    @ApiOperation("创建线上充值单并返回支付地址")
    @PostMapping("/deposit")
    public AppPayDepositResult deposit(@RequestBody AppPayDepositBody body)
    {
        if (body == null)
        {
            return AppPayDepositResult.fail("请填写金额");
        }
        return AppPayDepositResult.ok(onlinePayService.createDeposit(
                AppSecurityUtils.getMemberId(),
                body.getAmount(),
                body.getScene(),
                body.getChannelCode(),
                serverConfig.getUrl(),
                IpUtils.getIpAddr(),
                body.getReturnUrl()));
    }

    @ApiOperation("查询支付单")
    @GetMapping("/order")
    public AjaxResult order(@RequestParam("outTradeNo") String outTradeNo)
    {
        return success(onlinePayService.selectPayOrderByOutTradeNo(outTradeNo));
    }
}
