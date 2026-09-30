package com.ruoyi.biz.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import com.ruoyi.biz.domain.BizLotteryActivity;

@Mapper
public interface BizLotteryActivityMapper
{
    BizLotteryActivity selectLotteryActivityById(Long activityId);

    /** 当前启用且未结束的活动（未到开始时间也返回，抽奖时再拦截） */
    BizLotteryActivity selectCurrentActiveActivity();

    /** 系统唯一抽奖活动（不限状态，取最早一条） */
    BizLotteryActivity selectSoleLotteryActivity();

    int countLotteryActivity();

    List<BizLotteryActivity> selectLotteryActivityList(BizLotteryActivity query);

    int insertLotteryActivity(BizLotteryActivity activity);

    int updateLotteryActivity(BizLotteryActivity activity);

    int deleteLotteryActivityByIds(Long[] activityIds);
}
