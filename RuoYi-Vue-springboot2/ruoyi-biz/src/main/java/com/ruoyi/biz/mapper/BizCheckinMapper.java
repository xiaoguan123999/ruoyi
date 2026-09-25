package com.ruoyi.biz.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizCheckin;

@Mapper
public interface BizCheckinMapper
{
    BizCheckin selectByMemberAndDate(@Param("memberId") Long memberId, @Param("checkinDate") Date checkinDate);

    List<BizCheckin> selectCheckinList(BizCheckin checkin);

    List<Date> selectDatesByMemberId(@Param("memberId") Long memberId);

    int insertCheckin(BizCheckin checkin);

    int countByMemberId(@Param("memberId") Long memberId);

    /**
     * 连续签到天数（ streak 以今天或昨天为终点）
     */
    int countConsecutiveDays(@Param("memberId") Long memberId);
}
