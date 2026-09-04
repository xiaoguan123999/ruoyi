package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.api.AppTeamLevelStats;
import com.ruoyi.biz.api.AppTeamMemberItem;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizTeamTreeNode;
import com.ruoyi.biz.domain.BizTeamTreeSummary;

public interface BizMemberMapper
{
    BizMember selectMemberById(Long memberId);

    BizMember selectMemberByPhone(String phone);

    BizMember selectMemberByInviteCode(String inviteCode);

    List<BizMember> selectMemberList(BizMember member);

    int insertMember(BizMember member);

    int updateMember(BizMember member);

    int updateGoogleAuth(@Param("memberId") Long memberId, @Param("gaSecret") String gaSecret, @Param("gaStatus") String gaStatus);

    int updatePayPassword(@Param("memberId") Long memberId, @Param("payPassword") String payPassword);

    int countValidTeamMembers(Long memberId);

    int countValidTeamMembersConfig(@Param("memberId") Long memberId, @Param("needKyc") boolean needKyc,
            @Param("needOrder") boolean needOrder, @Param("maxDepth") Integer maxDepth,
            @Param("viewerDepth") Integer viewerDepth);

    int countDirectMembers(Long memberId);

    List<BizMember> selectTeamMembers(@Param("memberId") Long memberId, @Param("teamLevel") Integer teamLevel,
            @Param("viewerDepth") Integer viewerDepth);

    List<AppTeamMemberItem> selectAppTeamMembers(@Param("memberId") Long memberId, @Param("teamLevel") Integer teamLevel,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAppTeamRegisterStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAppTeamOrderStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAppTeamRechargeStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAdminTeamRegisterStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAdminTeamOrderStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    List<AppTeamLevelStats> selectAdminTeamRechargeStats(@Param("memberId") Long memberId,
            @Param("viewerDepth") int viewerDepth);

    int countByIdCard(@Param("idCard") String idCard, @Param("excludeMemberId") Long excludeMemberId);

    BizTeamTreeNode selectTeamTreeNode(Long memberId);

    List<BizTeamTreeNode> selectTeamTreeChildren(Long parentId);

    BizTeamTreeSummary selectTeamTreeSummary(Long memberId);
}
