package com.ruoyi.biz.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.biz.domain.BizLotteryRecord;

@Mapper
public interface BizLotteryRecordMapper
{
    List<BizLotteryRecord> selectRecordList(BizLotteryRecord query);

    int insertRecord(BizLotteryRecord record);

    Integer selectMaxDrawCount(@Param("activityId") Long activityId, @Param("memberId") Long memberId);
}
