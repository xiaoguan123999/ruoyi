package com.ruoyi.web.controller.app;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import com.google.code.kaptcha.Producer;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppLoginBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sign.Base64;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.web.service.AppTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-认证")
@RestController
@RequestMapping("/app/auth")
public class AppAuthController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AppAuthController.class);

    @Resource(name = "appCaptchaProducer")
    private Producer captchaProducer;

    @Resource(name = "appCaptchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IBizMemberService memberService;

    @Autowired
    private AppTokenService appTokenService;

    @Autowired
    private IBizGoogleAuthService googleAuthService;

    @ApiOperation("获取登录验证码")
    @GetMapping("/captcha")
    public AjaxResult captcha()
    {
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String capStr;
        String code;
        BufferedImage image;
        String captchaType = RuoYiConfig.getCaptchaType();
        if ("math".equals(captchaType))
        {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        }
        else
        {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try
        {
            ImageIO.write(image, "png", os);
        }
        catch (IOException e)
        {
            throw new ServiceException("验证码生成失败");
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("uuid", uuid);
        ajax.put("img", Base64.encode(os.toByteArray()));
        ajax.put("imgType", "png");
        ajax.put("captchaEnabled", true);
        return ajax;
    }

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody AppRegisterBody body)
    {
        if (body == null)
        {
            throw new ServiceException("请输入验证码");
        }
        validateCaptcha(body.getCode(), body.getUuid());
        BizMember member = memberService.register(body);
        return buildToken(member);
    }

    @ApiOperation("会员登录")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody AppLoginBody body)
    {
        if (body == null || StringUtils.isEmpty(body.getPhone()) || StringUtils.isEmpty(body.getPassword()))
        {
            throw new ServiceException("手机号和密码不能为空");
        }
        validateCaptcha(body.getCode(), body.getUuid());
        BizMember member;
        try
        {
            member = memberService.selectMemberByPhone(body.getPhone());
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            log.error("App login query failed", e);
            throw new ServiceException(Constants.NETWORK_RETRY);
        }
        if (member == null || !SecurityUtils.matchesPassword(body.getPassword(), member.getPassword()))
        {
            throw new ServiceException("手机号或密码错误");
        }
        if (BizConstants.STATUS_DISABLE.equals(member.getStatus()))
        {
            throw new ServiceException("账号已停用");
        }
        googleAuthService.assertForLogin(member, body.getGoogleCode());
        return buildToken(member);
    }

    @ApiOperation("会员退出")
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request)
    {
        appTokenService.delLoginMember(request);
        return success();
    }

    private void validateCaptcha(String code, String uuid)
    {
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid))
        {
            throw new ServiceException("请输入验证码");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        if (captcha == null)
        {
            throw new ServiceException("验证码已失效");
        }
        redisCache.deleteObject(verifyKey);
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new ServiceException("验证码错误");
        }
    }

    private AjaxResult buildToken(BizMember member)
    {
        AppLoginMember loginMember = new AppLoginMember();
        loginMember.setMemberId(member.getMemberId());
        loginMember.setPhone(member.getPhone());
        String token = appTokenService.createToken(loginMember);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, token);
        ajax.put("memberId", member.getMemberId());
        ajax.put("inviteCode", member.getInviteCode());
        ajax.put("gaBound", BizConstants.GA_BOUND.equals(member.getGaStatus()));
        return ajax;
    }
}
