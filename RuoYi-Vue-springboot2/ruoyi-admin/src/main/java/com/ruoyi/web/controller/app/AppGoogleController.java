package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.api.AppGoogleResult;
import com.ruoyi.biz.api.AppOkResult;
import com.ruoyi.biz.domain.AppGoogleCodeBody;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.AppSecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-谷歌验证")
@RestController
@RequestMapping("/app/google")
public class AppGoogleController extends BaseController
{
    @Autowired
    private IBizGoogleAuthService googleAuthService;

    @ApiOperation(value = "谷歌验证状态", notes = "data.bound 是否已绑定。App 登录和提现不要求谷歌验证，requireWithdraw 固定为 false。")
    @GetMapping("/status")
    public AppGoogleResult status()
    {
        return AppGoogleResult.ok(googleAuthService.status(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "开始绑定", notes = "data 含 secret、otpauthUrl，10 分钟内有效。App 用 otpauthUrl 生成二维码。")
    @GetMapping("/bind")
    public AppGoogleResult startBind()
    {
        return AppGoogleResult.ok(googleAuthService.startBind(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation(value = "确认绑定", notes = "成功仅 code/msg，没有 data。")
    @PostMapping("/bind")
    public AppOkResult confirmBind(@RequestBody AppGoogleCodeBody body)
    {
        googleAuthService.confirmBind(AppSecurityUtils.getMemberId(), body == null ? null : body.getGoogleCode());
        return AppOkResult.ok();
    }

    @ApiOperation(value = "解绑", notes = "成功仅 code/msg。需要当前谷歌验证码。")
    @PostMapping("/unbind")
    public AppOkResult unbind(@RequestBody AppGoogleCodeBody body)
    {
        googleAuthService.unbind(AppSecurityUtils.getMemberId(), body == null ? null : body.getGoogleCode());
        return AppOkResult.ok();
    }
}
