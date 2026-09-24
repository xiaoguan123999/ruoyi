package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryActivity;

@Mapper
public interface BizLotteryActivityMapper
{
    BizLotteryActivity selectLotteryActivityById(Long activityId);

    /** 当前启用且在有效期内的活动（取一条） */
    BizLotteryActivity selectCurrentActiveActivity();

    /** 系统唯一抽奖活动（不限状态，取最早一条） */
    BizLotteryActivity selectSoleLotteryActivity();

    int countLotteryActivity();

    List<BizLotteryActivity> selectLotteryActivityList(BizLotteryActivity query);

    int insertLotteryActivity(BizLotteryActivity activity);

    int updateLotteryActivity(BizLotteryActivity activity);

    int deleteLotteryActivityByIds(Long[] activityIds);
}
