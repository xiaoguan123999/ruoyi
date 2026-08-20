package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.biz.domain.AppGoogleCodeBody;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
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

    @ApiOperation("谷歌验证状态")
    @GetMapping("/status")
    public AjaxResult status()
    {
        return success(googleAuthService.status(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("开始绑定谷歌验证器，返回密钥和 otpauth 链接")
    @GetMapping("/bind")
    public AjaxResult startBind()
    {
        return success(googleAuthService.startBind(AppSecurityUtils.getMemberId()));
    }

    @ApiOperation("确认绑定")
    @PostMapping("/bind")
    public AjaxResult confirmBind(@RequestBody AppGoogleCodeBody body)
    {
        googleAuthService.confirmBind(AppSecurityUtils.getMemberId(), body == null ? null : body.getGoogleCode());
        return success();
    }

    @ApiOperation("解绑谷歌验证器")
    @PostMapping("/unbind")
    public AjaxResult unbind(@RequestBody AppGoogleCodeBody body)
    {
        googleAuthService.unbind(AppSecurityUtils.getMemberId(), body == null ? null : body.getGoogleCode());
        return success();
    }
}
