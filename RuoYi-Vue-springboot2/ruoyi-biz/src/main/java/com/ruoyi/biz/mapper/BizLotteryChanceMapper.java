package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizLotteryChance;

@Mapper
public interface BizLotteryChanceMapper
{
    BizLotteryChance selectByActivityAndMember(@Param("activityId") Long activityId, @Param("memberId") Long memberId);

    List<BizLotteryChance> selectChanceList(BizLotteryChance query);

    int insertChance(BizLotteryChance chance);

    int addBalance(@Param("activityId") Long activityId, @Param("memberId") Long memberId, @Param("amount") int amount);

    int consumeBalance(@Param("activityId") Long activityId, @Param("memberId") Long memberId, @Param("amount") int amount);

    /** 抽奖失败退回：加回余额，并回滚累计消耗（不增加累计发放） */
    int refundBalance(@Param("activityId") Long activityId, @Param("memberId") Long memberId, @Param("amount") int amount);
}
