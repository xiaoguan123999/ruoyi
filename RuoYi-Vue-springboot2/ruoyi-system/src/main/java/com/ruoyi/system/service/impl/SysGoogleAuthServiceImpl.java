package com.ruoyi.system.service.impl;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.GoogleAuthUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysGoogleBindInfo;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysGoogleAuthService;

@Service
public class SysGoogleAuthServiceImpl implements ISysGoogleAuthService
{
    private static final String BIND_KEY = "sys_google_bind:";

    private static final String GA_NONE = "0";

    private static final String GA_BOUND = "1";

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public SysGoogleBindInfo status(Long userId)
    {
        SysUser user = requireUser(userId);
        SysGoogleBindInfo info = new SysGoogleBindInfo();
        info.setBound(isBound(user));
        info.setEnabled(isGoogleEnabled());
        info.setIssuer(getIssuer());
        return info;
    }

    @Override
    public SysGoogleBindInfo startBind(Long userId)
    {
        assertGoogleEnabled();
        SysUser user = requireUser(userId);
        if (isBound(user))
        {
            throw new ServiceException("已绑定谷歌验证器");
        }
        String secret = GoogleAuthUtils.generateSecret();
        redisCache.setCacheObject(BIND_KEY + userId, secret, 10, TimeUnit.MINUTES);
        String issuer = getIssuer();
        SysGoogleBindInfo info = status(userId);
        info.setBound(false);
        info.setSecret(secret);
        info.setOtpauthUrl(GoogleAuthUtils.otpAuthUrl(issuer, user.getUserName(), secret));
        info.setIssuer(issuer);
        return info;
    }

    @Override
    public void confirmBind(Long userId, String googleCode)
    {
        assertGoogleEnabled();
        SysUser user = requireUser(userId);
        if (isBound(user))
        {
            throw new ServiceException("已绑定谷歌验证器");
        }
        String secret = redisCache.getCacheObject(BIND_KEY + userId);
        if (StringUtils.isEmpty(secret))
        {
            throw new ServiceException("请先获取谷歌验证密钥");
        }
        if (!GoogleAuthUtils.verify(secret, googleCode))
        {
            throw new ServiceException("谷歌验证码错误");
        }
        userMapper.updateGoogleAuth(userId, secret, GA_BOUND);
        redisCache.deleteObject(BIND_KEY + userId);
    }

    @Override
    public void unbind(Long userId, String googleCode)
    {
        SysUser user = requireUser(userId);
        if (!isBound(user))
        {
            throw new ServiceException("尚未绑定谷歌验证器");
        }
        verifyBound(user, googleCode);
        userMapper.updateGoogleAuth(userId, "", GA_NONE);
    }

    @Override
    public void reset(Long userId)
    {
        requireUser(userId);
        userMapper.updateGoogleAuth(userId, "", GA_NONE);
        redisCache.deleteObject(BIND_KEY + userId);
    }

    @Override
    public void assertForLogin(SysUser user, String googleCode)
    {
        if (!isGoogleEnabled() || !isBound(user))
        {
            return;
        }
        verifyBound(user, googleCode);
    }

    private void verifyBound(SysUser user, String googleCode)
    {
        if (StringUtils.isEmpty(googleCode))
        {
            throw new ServiceException("请输入谷歌验证码");
        }
        if (!GoogleAuthUtils.verify(user.getGaSecret(), googleCode))
        {
            throw new ServiceException("谷歌验证码错误");
        }
    }

    private void assertGoogleEnabled()
    {
        if (!isGoogleEnabled())
        {
            throw new ServiceException("谷歌验证未开启");
        }
    }

    private boolean isGoogleEnabled()
    {
        return "true".equalsIgnoreCase(StringUtils.nvl(configService.selectConfigByKey("sys.google.enabled"), "true"));
    }

    private String getIssuer()
    {
        String issuer = configService.selectConfigByKey("sys.google.issuer");
        return StringUtils.isEmpty(issuer) ? "后台管理" : issuer;
    }

    private SysUser requireUser(Long userId)
    {
        SysUser user = userMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    private boolean isBound(SysUser user)
    {
        return user != null && GA_BOUND.equals(user.getGaStatus());
    }
}
