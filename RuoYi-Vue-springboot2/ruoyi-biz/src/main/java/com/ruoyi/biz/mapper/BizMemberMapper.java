package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizMember;

public interface BizMemberMapper
{
    BizMember selectMemberById(Long memberId);

    BizMember selectMemberByPhone(String phone);

    BizMember selectMemberByInviteCode(String inviteCode);

    List<BizMember> selectMemberList(BizMember member);

    int insertMember(BizMember member);

    int updateMember(BizMember member);

    int updateGoogleAuth(@Param("memberId") Long memberId, @Param("gaSecret") String gaSecret, @Param("gaStatus") String gaStatus);

    int countValidTeamMembers(Long memberId);

    int countDirectMembers(Long memberId);

    List<BizMember> selectTeamMembers(@Param("memberId") Long memberId, @Param("teamLevel") Integer teamLevel);
}
