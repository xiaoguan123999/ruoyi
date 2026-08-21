package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizMember;

public interface IBizMemberService
{
    BizMember selectMemberById(Long memberId);

    BizMember selectMemberByPhone(String phone);

    List<BizMember> selectMemberList(BizMember member);

    BizMember register(AppRegisterBody body);

    BizMember createRootMember(String phone, String password);

    void updateMember(BizMember member);

    void submitKyc(Long memberId, AppKycBody body);

    void changePassword(Long memberId, String oldPassword, String newPassword, String confirmPassword);

    List<BizMember> selectTeamMembers(Long memberId, Integer teamLevel);

    void refreshLevel(Long memberId);
}
