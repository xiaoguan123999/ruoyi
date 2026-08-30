package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizLevelRewardGrant;

public interface BizLevelRewardGrantMapper
{
    BizLevelRewardGrant selectGrantById(Long grantId);

    BizLevelRewardGrant selectByCycle(@Param("memberId") Long memberId, @Param("levelId") Long levelId,
            @Param("cycleKey") String cycleKey);

    int countActiveByMemberLevel(@Param("memberId") Long memberId, @Param("levelId") Long levelId);

    List<BizLevelRewardGrant> selectByMemberAndLevel(@Param("memberId") Long memberId, @Param("levelId") Long levelId);

    List<BizLevelRewardGrant> selectGrantList(BizLevelRewardGrant grant);

    int insertGrant(BizLevelRewardGrant grant);

    int updateGrant(BizLevelRewardGrant grant);
}
