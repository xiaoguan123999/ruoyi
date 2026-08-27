package com.ruoyi.framework.web.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * App会员 token：默认 7 天，剩余不足 1 天且有请求时自动续签。
 */
@Component
public class AppTokenService
{
    private static final Logger log = LoggerFactory.getLogger(AppTokenService.class);

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    /** App 有效期（分钟），默认 7 天 */
    @Value("${token.appExpireTime:10080}")
    private int expireTime;

    protected static final long MILLIS_MINUTE = 60 * 1000L;

    /** 剩余不足 1 天则续签 */
    private static final long MILLIS_DAY = 24 * 60 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    public AppLoginMember getLoginMember(HttpServletRequest request)
    {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(Constants.LOGIN_APP_MEMBER_KEY);
                if (StringUtils.isEmpty(uuid))
                {
                    return null;
                }
                Object cached = redisCache.getCacheObject(getTokenKey(uuid));
                if (cached instanceof AppLoginMember)
                {
                    return (AppLoginMember) cached;
                }
                if (cached != null)
                {
                    log.error("App登录缓存类型异常 {}", cached.getClass().getName());
                    redisCache.deleteObject(getTokenKey(uuid));
                }
                return null;
            }
            catch (Exception e)
            {
                log.error("获取App会员信息异常 {}", e.getClass().getSimpleName());
            }
        }
        return null;
    }

    public String createToken(AppLoginMember loginMember)
    {
        String token = IdUtils.fastUUID();
        loginMember.setToken(token);
        refreshToken(loginMember);
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(Constants.LOGIN_APP_MEMBER_KEY, token);
        claims.put(Constants.JWT_USERNAME, loginMember.getPhone());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public void verifyToken(AppLoginMember loginMember)
    {
        if (loginMember == null || loginMember.getExpireTime() == null)
        {
            return;
        }
        long expire = loginMember.getExpireTime().longValue();
        long currentTime = System.currentTimeMillis();
        if (expire - currentTime <= MILLIS_DAY)
        {
            refreshToken(loginMember);
        }
    }

    public void refreshToken(AppLoginMember loginMember)
    {
        loginMember.setLoginTime(System.currentTimeMillis());
        loginMember.setExpireTime(Long.valueOf(loginMember.getLoginTime() + expireTime * MILLIS_MINUTE));
        redisCache.setCacheObject(getTokenKey(loginMember.getToken()), loginMember, expireTime, TimeUnit.MINUTES);
    }

    public void delLoginMember(HttpServletRequest request)
    {
        String token = getToken(request);
        if (StringUtils.isEmpty(token))
        {
            return;
        }
        try
        {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(Constants.LOGIN_APP_MEMBER_KEY);
            if (StringUtils.isNotEmpty(uuid))
            {
                redisCache.deleteObject(getTokenKey(uuid));
            }
        }
        catch (Exception e)
        {
            log.error("App logout failed: {}", e.getMessage());
        }
    }

    private Claims parseToken(String token)
    {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.APP_LOGIN_TOKEN_KEY + uuid;
    }
}
