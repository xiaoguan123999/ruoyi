package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysGoogleBindInfo;

public interface ISysGoogleAuthService
{
    SysGoogleBindInfo status(Long userId);

    SysGoogleBindInfo startBind(Long userId);

    void confirmBind(Long userId, String googleCode);

    void unbind(Long userId, String googleCode);

    void reset(Long userId);

    void assertForLogin(SysUser user, String googleCode);
}
