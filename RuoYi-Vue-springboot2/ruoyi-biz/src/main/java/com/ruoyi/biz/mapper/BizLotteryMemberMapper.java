package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizLotteryMember;

@Mapper
public interface BizLotteryMemberMapper
{
    BizLotteryMember selectLotteryMember(@Param("activityId") Long activityId, @Param("memberId") Long memberId);

    int upsertLotteryMember(BizLotteryMember member);
}
