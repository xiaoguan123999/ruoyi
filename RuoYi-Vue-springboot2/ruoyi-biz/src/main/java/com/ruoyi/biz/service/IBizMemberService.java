package com.ruoyi.biz.service;

import java.util.List;
import com.ruoyi.biz.api.AppTeamData;
import com.ruoyi.biz.api.AppTeamLevelStats;
import com.ruoyi.biz.domain.AppKycBody;
import com.ruoyi.biz.domain.AppRegisterBody;
import com.ruoyi.biz.domain.BizMember;
import com.ruoyi.biz.domain.BizTeamRelationRow;
import com.ruoyi.biz.domain.BizTeamTreeNode;
import com.ruoyi.biz.domain.BizTeamTreeSummary;

public interface IBizMemberService
{
    BizMember selectMemberById(Long memberId);

    BizMember selectMemberByPhone(String phone);

    List<BizMember> selectMemberList(BizMember member);

    BizMember register(AppRegisterBody body);

    BizMember createRootMember(String phone, String password);

    void updateMember(BizMember member);

    void resetLoginPassword(Long memberId, String password);

    void resetPayPassword(Long memberId, String payPassword);

    void submitKyc(Long memberId, AppKycBody body);

    void changePassword(Long memberId, String oldPassword, String newPassword, String confirmPassword);

    void savePayPassword(Long memberId, String oldPayPassword, String newPayPassword, String confirmPassword);

    void assertPayPassword(Long memberId, String payPassword);

    List<BizMember> selectTeamMembers(Long memberId, Integer teamLevel);

    AppTeamData getAppTeam(Long memberId);

    List<AppTeamLevelStats> getAdminTeamLevels(Long memberId);

    BizTeamTreeNode selectTeamTreeRoot(String keyword);

    List<BizTeamTreeNode> selectTeamTreeChildren(Long memberId);

    BizTeamTreeSummary selectTeamTreeSummary(Long memberId);

    List<BizTeamRelationRow> selectRecommendRelation(String keyword);

    void refreshLevel(Long memberId);

    void refreshLevelAndUplines(Long memberId);

    int refreshAllLevels();
}
