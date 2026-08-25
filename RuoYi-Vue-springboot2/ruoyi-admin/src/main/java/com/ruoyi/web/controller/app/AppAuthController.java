package com.ruoyi.web.controller.app;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.AppLoginBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.service.IBizBlacklistService;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.biz.service.IBizMemberService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.biz.api.AppCaptchaResult;
import com.ruoyi.biz.api.AppLoginResult;
import com.ruoyi.biz.api.AppOkResult;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.web.service.AppTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "App-???")
@RestController
@RequestMapping("/app/auth")
public class AppAuthController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AppAuthController.class);

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IBizMemberService memberService;

    @Autowired
    private AppTokenService appTokenService;

    @Autowired
    private IBizGoogleAuthService googleAuthService;

    @Autowired
    private IBizBlacklistService blacklistService;

    @ApiOperation("???????????")
    @GetMapping("/captcha")
    public AppCaptchaResult captcha()
    {
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String text = String.format("%04d", Integer.valueOf(ThreadLocalRandom.current().nextInt(10000)));
        redisCache.setCacheObject(verifyKey, text, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        return AppCaptchaResult.of(uuid, text);
    }

    @ApiOperation("??????")
    @PostMapping("/register")
    public AppLoginResult register(@RequestBody AppRegisterBody body)
    {
        if (body == null)
        {
            throw new ServiceException("???????????");
        }
        validateCaptcha(body.getCode(), body.getUuid());
        BizMember member = memberService.register(body);
        return buildToken(member);
    }

    @ApiOperation("??????")
    @PostMapping("/login")
    public AppLoginResult login(@RequestBody AppLoginBody body)
    {
        if (body == null || StringUtils.isEmpty(body.getPhone()) || StringUtils.isEmpty(body.getPassword()))
        {
            throw new ServiceException("???????????????");
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
        blacklistService.assertPhone(body.getPhone(), BizConstants.BLACKLIST_LOGIN,
                member == null ? null : member.getMemberId());
        if (member == null || !SecurityUtils.matchesPassword(body.getPassword(), member.getPassword()))
        {
            throw new ServiceException("??????????????");
        }
        if (BizConstants.STATUS_DISABLE.equals(member.getStatus()))
        {
            throw new ServiceException("????????");
        }
        googleAuthService.assertForLogin(member, body.getGoogleCode());
        return buildToken(member);
    }

    @ApiOperation("???????")
    @PostMapping("/logout")
    public AppOkResult logout(HttpServletRequest request)
    {
        appTokenService.delLoginMember(request);
        return AppOkResult.ok();
    }

    private void validateCaptcha(String code, String uuid)
    {
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid))
        {
            throw new ServiceException("???????????");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);
        if (captcha == null)
        {
            throw new ServiceException("????????งน");
        }
        redisCache.deleteObject(verifyKey);
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new ServiceException("????????");
        }
    }

    private AppLoginResult buildToken(BizMember member)
    {
        AppLoginMember loginMember = new AppLoginMember();
        loginMember.setMemberId(member.getMemberId());
        loginMember.setPhone(member.getPhone());
        String token = appTokenService.createToken(loginMember);
        return AppLoginResult.of(token, member.getMemberId(), member.getInviteCode(),
                BizConstants.GA_BOUND.equals(member.getGaStatus()));
    }
}
