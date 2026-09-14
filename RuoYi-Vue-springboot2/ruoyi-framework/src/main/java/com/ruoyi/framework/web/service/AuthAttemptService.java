package com.ruoyi.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * Login/register fail counter by IP or username.
 */
@Component
public class AuthAttemptService
{
    private static final String TOO_FAST = "\u64cd\u4f5c\u8fc7\u4e8e\u9891\u7e41\uff0c\u8bf7\u7a0d\u540e\u518d\u8bd5";

    private static final int INVITE_MAX = 8;

    private static final int INVITE_LOCK_MINUTES = 15;

    private static final int GOOGLE_MAX = 8;

    private static final int GOOGLE_LOCK_MINUTES = 15;

    @Autowired
    private RedisCache redisCache;

    public void assertInviteAllowed()
    {
        assertAllowed(inviteKey(), INVITE_MAX);
    }

    public void recordInviteFail()
    {
        increase(inviteKey(), INVITE_LOCK_MINUTES);
    }

    public void clearInviteFail()
    {
        redisCache.deleteObject(inviteKey());
    }

    public void assertGoogleAllowed(String username)
    {
        assertAllowed(googleKey(username), GOOGLE_MAX);
    }

    public void recordGoogleFail(String username)
    {
        increase(googleKey(username), GOOGLE_LOCK_MINUTES);
    }

    public void clearGoogleFail(String username)
    {
        redisCache.deleteObject(googleKey(username));
    }

    private void assertAllowed(String key, int max)
    {
        Integer n = redisCache.getCacheObject(key);
        if (n != null && n.intValue() >= max)
        {
            throw new ServiceException(TOO_FAST);
        }
    }

    private void increase(String key, int lockMinutes)
    {
        Integer n = redisCache.getCacheObject(key);
        if (n == null)
        {
            n = Integer.valueOf(0);
        }
        redisCache.setCacheObject(key, Integer.valueOf(n.intValue() + 1), Integer.valueOf(lockMinutes), TimeUnit.MINUTES);
    }

    private String inviteKey()
    {
        return CacheConstants.INVITE_FAIL_IP_KEY + StringUtils.nvl(IpUtils.getIpAddr(), "unknown");
    }

    private String googleKey(String username)
    {
        return CacheConstants.GOOGLE_FAIL_KEY + StringUtils.nvl(username, "") + ":" + StringUtils.nvl(IpUtils.getIpAddr(), "unknown");
    }
}
