package com.ruoyi.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.model.AppLoginMember;
import com.ruoyi.common.exception.ServiceException;

/**
 * App会员安全工具
 */
public class AppSecurityUtils
{
    public static Long getMemberId()
    {
        return getLoginMember().getMemberId();
    }

    public static AppLoginMember getLoginMember()
    {
        try
        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return (AppLoginMember) authentication.getPrincipal();
        }
        catch (Exception e)
        {
            throw new ServiceException("获取会员信息异常", HttpStatus.UNAUTHORIZED);
        }
    }
}
