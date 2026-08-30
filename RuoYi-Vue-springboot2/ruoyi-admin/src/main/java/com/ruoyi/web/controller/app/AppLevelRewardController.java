package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppLevelRewardClaimResult;
import com.ruoyi.biz.api.AppLevelRewardClaimableData;
import com.ruoyi.biz.api.AppLevelRewardClaimableResult;
import com.ruoyi.biz.domain.AppLevelRewardClaimBody;
import com.ruoyi.biz.service.IBizLevelRewardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-等级奖励领取")
@RestController
@RequestMapping("/app")
public class AppLevelRewardController extends BaseController
{
    @Autowired
    private IBizLevelRewardService levelRewardService;

    @ApiOperation(value = "可领取的等级奖励", notes = "需登录。只返回发放方式为 CLAIM 且当前仍达标、本周期还未领完的项。")
    @GetMapping({"/levelReward/claimable", "/level/reward/claimable"})
    public AppLevelRewardClaimableResult claimable()
    {
        AppLevelRewardClaimableData data = new AppLevelRewardClaimableData();
        data.setItems(levelRewardService.listClaimable(AppSecurityUtils.getMemberId()));
        return AppLevelRewardClaimableResult.ok(data);
    }

    @ApiOperation(value = "领取等级奖励", notes = "body: levelId、currency=CNY|USDT。二选一时只能领一种；都可领取时每种各一次。领取时仍须匹配该等级。")
    @PostMapping({"/levelReward/claim", "/level/reward/claim"})
    public AppLevelRewardClaimResult claim(@RequestBody AppLevelRewardClaimBody body)
    {
        AppLevelRewardClaimResult result = AppLevelRewardClaimResult.ok(
                levelRewardService.claimReward(AppSecurityUtils.getMemberId(), body));
        result.setMsg("领取成功，已到账");
        return result;
    }
}
