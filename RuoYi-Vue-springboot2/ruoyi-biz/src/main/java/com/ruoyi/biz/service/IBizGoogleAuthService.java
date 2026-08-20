package com.ruoyi.biz.service;

import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.GoogleBindInfo;

public interface IBizGoogleAuthService
{
    GoogleBindInfo status(Long memberId);

    GoogleBindInfo startBind(Long memberId);

    void confirmBind(Long memberId, String googleCode);

    void unbind(Long memberId, String googleCode);

    void reset(Long memberId);

    void assertForLogin(BizMember member, String googleCode);

    void assertForWithdraw(Long memberId, String googleCode);
}
