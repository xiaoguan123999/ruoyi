package com.ruoyi.biz.service.impl;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.biz.constant.BizConstants;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.GoogleBindInfo;
import com.ruoyi.biz.mapper.BizMemberMapper;
import com.ruoyi.biz.service.IBizConfigService;
import com.ruoyi.biz.service.IBizGoogleAuthService;
import com.ruoyi.common.utils.GoogleAuthUtils;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class BizGoogleAuthServiceImpl implements IBizGoogleAuthService
{
    private static final String BIND_KEY = "biz_google_bind:";

    @Autowired
    private BizMemberMapper memberMapper;

    @Autowired
    private IBizConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Override
    public GoogleBindInfo status(Long memberId)
    {
        BizMember member = requireMember(memberId);
        GoogleBindInfo info = new GoogleBindInfo();
        info.setBound(isBound(member));
        info.setEnabled(configService.isGoogleEnabled());
        info.setRequireWithdraw(Boolean.FALSE);
        info.setIssuer(configService.getGoogleIssuer());
        return info;
    }

    @Override
    public GoogleBindInfo startBind(Long memberId)
    {
        assertGoogleEnabled();
        BizMember member = requireMember(memberId);
        if (isBound(member))
        {
            throw new ServiceException("已绑定谷歌验证器");
        }
        String secret = GoogleAuthUtils.generateSecret();
        redisCache.setCacheObject(BIND_KEY + memberId, secret, 10, TimeUnit.MINUTES);
        String issuer = configService.getGoogleIssuer();
        GoogleBindInfo info = status(memberId);
        info.setBound(false);
        info.setSecret(secret);
        info.setOtpauthUrl(GoogleAuthUtils.otpAuthUrl(issuer, member.getPhone(), secret));
        info.setIssuer(issuer);
        return info;
    }

    @Override
    public void confirmBind(Long memberId, String googleCode)
    {
        assertGoogleEnabled();
        BizMember member = requireMember(memberId);
        if (isBound(member))
        {
            throw new ServiceException("已绑定谷歌验证器");
        }
        String secret = redisCache.getCacheObject(BIND_KEY + memberId);
        if (StringUtils.isEmpty(secret))
        {
            throw new ServiceException("请先获取谷歌验证密钥");
        }
        if (!GoogleAuthUtils.verify(secret, googleCode))
        {
            throw new ServiceException("谷歌验证码错误");
        }
        memberMapper.updateGoogleAuth(memberId, secret, BizConstants.GA_BOUND);
        redisCache.deleteObject(BIND_KEY + memberId);
    }

    @Override
    public void unbind(Long memberId, String googleCode)
    {
        BizMember member = requireMember(memberId);
        if (!isBound(member))
        {
            throw new ServiceException("尚未绑定谷歌验证器");
        }
        verifyBound(member, googleCode);
        memberMapper.updateGoogleAuth(memberId, "", BizConstants.GA_NONE);
    }

    @Override
    public void reset(Long memberId)
    {
        requireMember(memberId);
        memberMapper.updateGoogleAuth(memberId, "", BizConstants.GA_NONE);
        redisCache.deleteObject(BIND_KEY + memberId);
    }

    @Override
    public void assertForLogin(BizMember member, String googleCode)
    {
    }

    @Override
    public void assertForWithdraw(Long memberId, String googleCode)
    {
    }

    private void verifyBound(BizMember member, String googleCode)
    {
        if (StringUtils.isEmpty(googleCode))
        {
            throw new ServiceException("请输入谷歌验证码");
        }
        if (!GoogleAuthUtils.verify(member.getGaSecret(), googleCode))
        {
            throw new ServiceException("谷歌验证码错误");
        }
    }

    private void assertGoogleEnabled()
    {
        if (!configService.isGoogleEnabled())
        {
            throw new ServiceException("谷歌验证未开启");
        }
    }

    private BizMember requireMember(Long memberId)
    {
        BizMember member = memberMapper.selectMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("会员不存在");
        }
        return member;
    }

    private boolean isBound(BizMember member)
    {
        return member != null && BizConstants.GA_BOUND.equals(member.getGaStatus());
    }
}
