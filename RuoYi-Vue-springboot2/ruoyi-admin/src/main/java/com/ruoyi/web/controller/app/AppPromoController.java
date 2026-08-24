package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppPromoClaimResult;
import com.ruoyi.biz.api.AppPromoResult;
import com.ruoyi.biz.domain.AppPromoClaimBody;
import com.ruoyi.biz.service.IBizPromoService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-注册推广")
@RestController
@RequestMapping("/app")
public class AppPromoController extends BaseController
{
    @Autowired
    private IBizPromoService promoService;

    @Anonymous
    @ApiOperation(value = "注册推广规则", notes = "未登录也可看金额和规则说明。已登录会额外返回实名奖励是否已领、能否领取。别名 GET /app/registerReward")
    @GetMapping({"/promo", "/registerReward"})
    public AppPromoResult promo()
    {
        return AppPromoResult.ok(promoService.getAppPromo(AppSecurityUtils.getMemberIdOrNull()));
    }

    @ApiOperation(value = "领取实名注册奖励", notes = "实名后 CNY 或 USDT 任选其一，每人一次。别名 POST /app/registerReward")
    @PostMapping({"/promo/kycReward", "/registerReward"})
    public AppPromoClaimResult claimKycReward(@RequestBody AppPromoClaimBody body)
    {
        return AppPromoClaimResult.ok(promoService.claimKycReward(AppSecurityUtils.getMemberId(), body));
    }
}
